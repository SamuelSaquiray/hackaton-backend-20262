package com.tuckersoft.branchengine.service;

import com.tuckersoft.branchengine.entity.*;
import com.tuckersoft.branchengine.repository.*;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.event.*;

import java.time.Instant;

@Component
public class BranchNotificationListener {

    private static final Logger log =
            LoggerFactory.getLogger(BranchNotificationListener.class);

    private final DecisionRepository decisions;
    private final RealityLogRepository logs;
    private final JavaMailSender mail;
    private final UserRepository users;

    public BranchNotificationListener(
            DecisionRepository decisions,
            RealityLogRepository logs,
            JavaMailSender mail,
            UserRepository users) {

        this.decisions = decisions;
        this.logs = logs;
        this.mail = mail;
        this.users = users;
    }

    @Async("branchExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alCommit(DecisionCommittedEvent event) {

        Decision d = decisions.findById(event.decisionId()).orElse(null);

        if (d == null) {
            return;
        }

        d.setStatus("PROCESANDO");
        d.setUpdatedAt(Instant.now());
        decisions.save(d);

        Playthrough p = d.getPlaythrough();
        User u = p.getUser();

        String recipientEmail = u.getEmail();

        String subject =
                "[TUCKERSOFT] "
                        + d.getBranchType()
                        + " en "
                        + p.getPlayerTag()
                        + " | Impacto "
                        + d.getImpactLevel();

        RealityLog rl = new RealityLog();

        rl.setDecision(d);
        rl.setRecipientEmail(recipientEmail);
        rl.setSubject(subject);
        rl.setCreatedAt(Instant.now());

        try {

            if (event.simulateMailFailure()) {
                throw new MessagingException("Simulated MAIL_FAILURE");
            }

            String body = body(d);

            SimpleMailMessage m = new SimpleMailMessage();
            m.setTo(recipientEmail);
            m.setSubject(subject);
            m.setText(body);

            mail.send(m);

            rl.setLogStatus("SENT");
            rl.setSentAt(Instant.now());

            d.setStatus("ESTABILIZADA");

        } catch (Exception ex) {

            rl.setLogStatus("FAILED");
            rl.setErrorMessage(ex.getMessage());

            d.setStatus("ERROR");

            log.error(
                    "Mail failure for decision {}",
                    d.getId(),
                    ex
            );
        }

        d.setUpdatedAt(Instant.now());

        decisions.save(d);
        logs.save(rl);

        log.info(
                "[BRANCH-LOG] Decision ID: {} | Player: {} | Branch: {} | Impact: {} | Unit: {} | Node: {} -> {} | Thread: {} | Status: {}",
                d.getId(),
                p.getPlayerTag(),
                d.getBranchType(),
                d.getImpactLevel(),
                d.getHandlerUnit(),
                d.getNode().getNodeCode(),
                d.getResolvedNodeCode(),
                Thread.currentThread().getName(),
                d.getStatus()
        );
    }

    private String body(Decision d) {

        Playthrough p = d.getPlaythrough();
        User u = p.getUser();

        return """
            Hola %s,

            Una partida de prueba acaba de ramificarse.

            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            Decision ID      : #%s
            Jugador          : %s
            Rama             : %s
            Impacto          : %s
            Departamento     : %s
            Consecuencia     : %s
            Nodo origen      : %s
            Nodo destino      : %s
            Estado partida   : %s
            Lucidez          : %s/100
            Nivel de control : %s/100
            Final            : %s
            Registrada       : %s
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

            Decisión original del jugador:
            "%s"

            — Tuckersoft Branch Engine, 1984
            """.formatted(
                u.getDisplayName(),
                d.getId(),
                p.getPlayerTag(),
                d.getBranchType(),
                d.getImpactLevel(),
                d.getHandlerUnit(),
                d.getOutcomeCode(),
                d.getNode().getNodeCode(),
                String.valueOf(d.getResolvedNodeCode()),
                p.getStatus(),
                p.getLucidity(),
                p.getControlLevel(),
                p.getEndingCode() == null ? "-" : p.getEndingCode(),
                d.getCreatedAt(),
                d.getRawInput()
        );
    }
}