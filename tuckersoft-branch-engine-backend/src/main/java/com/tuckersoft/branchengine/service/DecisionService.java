package com.tuckersoft.branchengine.service;

import com.tuckersoft.branchengine.dto.DecisionDtos.*;
import com.tuckersoft.branchengine.entity.*;
import com.tuckersoft.branchengine.exception.AppException;
import com.tuckersoft.branchengine.repository.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Instant;
import java.util.Set;

@Service
public class DecisionService {

    private final DecisionRepository repo;
    private final PlaythroughRepository plays;
    private final StoryNodeRepository nodes;
    private final ApplicationEventPublisher publisher;

    public DecisionService(
            DecisionRepository repo,
            PlaythroughRepository plays,
            StoryNodeRepository nodes,
            ApplicationEventPublisher publisher) {

        this.repo = repo;
        this.plays = plays;
        this.nodes = nodes;
        this.publisher = publisher;
    }

    @Transactional
    public Response decide(
            CreateRequest r,
            User requester,
            boolean admin,
            boolean simulateFailure) {

        Playthrough p = plays.findById(r.playthroughId())
                .orElseThrow(() ->
                        new AppException(
                                HttpStatus.NOT_FOUND,
                                "PLAYTHROUGH_NOT_FOUND",
                                "Playthrough not found"
                        )
                );

        // Solo el dueño puede crear decisiones.
        // El administrador NO puede decidir sobre partidas ajenas.
        if (!p.getUser().getId().equals(requester.getId())) {
            throw new AppException(
                    HttpStatus.FORBIDDEN,
                    "FORBIDDEN",
                    "Only the owner can decide"
            );
        }

        if ("FINALIZADA".equals(p.getStatus())) {
            throw new AppException(
                    HttpStatus.CONFLICT,
                    "PLAYTHROUGH_FINISHED",
                    "Playthrough is already finished"
            );
        }

        if (!Set.of(
                "LEVE",
                "MODERADO",
                "GRAVE",
                "CRITICO"
        ).contains(r.impactLevel())) {

            throw new AppException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_IMPACT",
                    "Invalid impactLevel"
            );
        }

        String text = normalize(r.rawInput());
        String branch = classify(text);

        String unit = switch (branch) {
            case "OBEDIENCIA" ->
                    "Mesa de Guion";

            case "REBELDIA" ->
                    "Control de Continuidad";

            case "SOSPECHA" ->
                    "Oficina de Seguridad";

            case "RUPTURA_CUARTA_PARED" ->
                    "Departamento Netflix";

            default ->
                    "Archivo de Errores";
        };

        String outcome = switch (branch) {
            case "OBEDIENCIA" ->
                    "ADVANCE_MAIN_PATH";

            case "REBELDIA" ->
                    "FORK_TIMELINE";

            case "SOSPECHA" ->
                    "INJECT_WHITE_BEAR_SYMBOL";

            case "RUPTURA_CUARTA_PARED" ->
                    "BREAK_FOURTH_WALL";

            default ->
                    "DISCARD_INPUT";
        };

        Decision d = new Decision();

        d.setPlaythrough(p);
        d.setNode(p.getCurrentNode());
        d.setRawInput(r.rawInput());
        d.setBranchType(branch);
        d.setImpactLevel(r.impactLevel());
        d.setHandlerUnit(unit);
        d.setOutcomeCode(outcome);

        Instant now = Instant.now();

        d.setCreatedAt(now);
        d.setUpdatedAt(now);

        /*
         * ENTRADA_CORRUPTA:
         * - resolvedNodeCode = null
         * - status = ERROR
         * - no mueve el nodo
         * - no publica evento
         */
        if ("ENTRADA_CORRUPTA".equals(branch)) {

            d.setResolvedNodeCode(null);
            d.setStatus("ERROR");

            repo.save(d);

            return dto(d);
        }

        /*
         * Estadísticas:
         *
         * LEVE     -> lucidity -5   / control +5
         * MODERADO -> lucidity -15  / control +10
         * GRAVE    -> lucidity -30  / control +20
         * CRITICO  -> lucidity -40  / control +45
         */

        int luc = p.getLucidity();
        int ctrl = p.getControlLevel();

        int deltaLucidity = switch (r.impactLevel()) {
            case "LEVE" ->
                    5;

            case "MODERADO" ->
                    15;

            case "GRAVE" ->
                    30;

            case "CRITICO" ->
                    40;

            default ->
                    0;
        };

        int deltaControl = switch (r.impactLevel()) {
            case "LEVE" ->
                    5;

            case "MODERADO" ->
                    10;

            case "GRAVE" ->
                    20;

            case "CRITICO" ->
                    45;

            default ->
                    0;
        };

        p.setLucidity(
                Math.max(0, luc - deltaLucidity)
        );

        p.setControlLevel(
                Math.min(100, ctrl + deltaControl)
        );

        /*
         * Resolver nodo destino.
         *
         * RUPTURA_CUARTA_PARED -> glitchBranchCode
         * CRITICO              -> glitchBranchCode
         * cualquier otro       -> primaryBranchCode
         */
        String dest;

        if ("RUPTURA_CUARTA_PARED".equals(branch)
                || "CRITICO".equals(r.impactLevel())) {

            dest = p.getCurrentNode().getGlitchBranchCode();

        } else {

            dest = p.getCurrentNode().getPrimaryBranchCode();
        }

        // Se guarda el código aunque el nodo destino no exista.
        d.setResolvedNodeCode(dest);

        /*
         * Resolver estado final.
         *
         * 1. controlLevel >= 100
         * 2. lucidity <= 0
         * 3. destino null/inexistente
         * 4. partida activa
         */
        if (p.getControlLevel() >= 100) {

            p.setStatus("FINALIZADA");
            p.setEndingCode("ENDING_PAC_SYMBOL");

        } else if (p.getLucidity() <= 0) {

            p.setStatus("FINALIZADA");
            p.setEndingCode("ENDING_WHITE_BEAR");

        } else if (dest == null
                || nodes.findByNodeCode(dest).isEmpty()) {

            p.setStatus("FINALIZADA");
            p.setEndingCode("ENDING_NETFLIX_CUT");

        } else {

            p.setStatus("ACTIVA");

            p.setCurrentNode(
                    nodes.findByNodeCode(dest)
                            .orElseThrow()
            );
        }

        p.setUpdatedAt(Instant.now());

        plays.save(p);

        /*
         * Guardar decisión inicialmente como REGISTRADA.
         */
        d.setStatus("REGISTRADA");

        repo.save(d);

        /*
         * Publicar evento para procesamiento asíncrono.
         *
         * IMPORTANTE:
         * Este método está dentro de @Transactional.
         * El @TransactionalEventListener(AFTER_COMMIT)
         * de BranchNotificationListener se ejecutará
         * después de que esta transacción haga COMMIT.
         */
        System.out.println(
                ">>> PUBLICANDO EVENTO DecisionCommittedEvent: "
                        + "decisionId=" + d.getId()
                        + " simulateFailure=" + simulateFailure
        );

        publisher.publishEvent(
                new DecisionCommittedEvent(
                        d.getId(),
                        simulateFailure
                )
        );

        return dto(d);
    }

    public String normalize(String s) {

        return Normalizer
                .normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }

    public String classify(String text) {

        // Regla 1: entrada corrupta
        if (!text.matches(".*[a-z].*")) {
            return "ENTRADA_CORRUPTA";
        }

        /*
         * Regla 2: ruptura de cuarta pared.
         *
         * Tiene precedencia sobre REBELDIA porque
         * "destruye la camara", por ejemplo, debe
         * clasificarse como RUPTURA_CUARTA_PARED.
         */
        if (text.contains("netflix")
                || text.contains("camara")
                || text.contains("espectador")
                || text.contains("videojuego")) {

            return "RUPTURA_CUARTA_PARED";
        }

        // Regla 3: sospecha
        if (text.contains("vigilan")
                || text.contains("simbolo")
                || text.contains("conspiracion")) {

            return "SOSPECHA";
        }

        // Regla 4: rebeldía
        if (text.contains("rechaza")
                || text.contains("destruye")
                || text.contains("desobedece")
                || text.contains("renuncia")) {

            return "REBELDIA";
        }

        // Regla 5: obediencia
        return "OBEDIENCIA";
    }

    public Response dto(Decision d) {

        var p = d.getPlaythrough();

        return new Response(
                d.getId(),
                p.getId(),
                p.getPlayerTag(),
                d.getNode().getNodeCode(),
                d.getResolvedNodeCode(),
                d.getRawInput(),
                d.getBranchType(),
                d.getImpactLevel(),
                d.getHandlerUnit(),
                d.getOutcomeCode(),
                d.getStatus(),
                p.getStatus(),
                p.getLucidity(),
                p.getControlLevel(),
                p.getEndingCode(),
                d.getCreatedAt(),
                d.getUpdatedAt()
        );
    }
}