package com.tuckersoft.branchengine.service;

import com.tuckersoft.branchengine.dto.PlaythroughDtos.*;
import com.tuckersoft.branchengine.entity.*;
import com.tuckersoft.branchengine.exception.AppException;
import com.tuckersoft.branchengine.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlaythroughService {

    private final PlaythroughRepository repo;
    private final StoryNodeRepository nodes;
    private final DecisionRepository decisions;

    public PlaythroughService(
            PlaythroughRepository repo,
            StoryNodeRepository nodes,
            DecisionRepository decisions) {
        this.repo = repo;
        this.nodes = nodes;
        this.decisions = decisions;
    }

    public Response create(CreateRequest r, User user) {

        StoryNode node = nodes.findByNodeCode(r.startNodeCode())
                .orElseThrow(() ->
                        new AppException(
                                HttpStatus.NOT_FOUND,
                                "NODE_NOT_FOUND",
                                "Start node not found"
                        )
                );

        if (repo.existsByPlayerTag(r.playerTag())) {
            throw new AppException(
                    HttpStatus.CONFLICT,
                    "PLAYER_TAG_EXISTS",
                    "playerTag already exists"
            );
        }

        // Un nodo lleno debe responder 400
        if (node.getBranchCapacity() > 0
                && node.getCurrentBranches() >= node.getBranchCapacity()) {

            throw new AppException(
                    HttpStatus.BAD_REQUEST,
                    "NODE_FULL",
                    "Node capacity reached"
            );
        }

        Playthrough p = new Playthrough();

        p.setPlayerTag(r.playerTag());
        p.setUser(user);
        p.setStartNodeCode(node.getNodeCode());
        p.setCurrentNode(node);

        // Valores iniciales
        p.setLucidity(100);
        p.setControlLevel(0);
        p.setStatus("ACTIVA");
        p.setEndingCode(null);

        Instant now = Instant.now();
        p.setCreatedAt(now);
        p.setUpdatedAt(now);

        // Registrar que una rama del nodo está ocupada
        node.setCurrentBranches(node.getCurrentBranches() + 1);
        nodes.save(node);

        return dto(repo.save(p));
    }

    public List<Response> mine(User user) {
        return repo.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::dto)
                .toList();
    }
    public List<Response> all() {
        return repo.findAll()
                .stream()
                .map(this::dto)
                .toList();
    }
    public Playthrough get(Long id, User requester, boolean admin) {

        Playthrough p = repo.findById(id)
                .orElseThrow(() ->
                        new AppException(
                                HttpStatus.NOT_FOUND,
                                "PLAYTHROUGH_NOT_FOUND",
                                "Playthrough not found"
                        )
                );

        if (!admin && !p.getUser().getId().equals(requester.getId())) {
            throw new AppException(
                    HttpStatus.FORBIDDEN,
                    "FORBIDDEN",
                    "Playthrough belongs to another user"
            );
        }

        return p;
    }

    public Response dto(Playthrough p) {
        return new Response(
                p.getId(),
                p.getPlayerTag(),
                p.getUser().getEmail(),
                p.getStartNodeCode(),
                p.getCurrentNode().getNodeCode(),
                p.getLucidity(),
                p.getControlLevel(),
                p.getStatus(),
                p.getEndingCode(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    public PathResponse path(Long id, User u, boolean admin) {

        Playthrough p = get(id, u, admin);

        var steps = decisions
                .findByPlaythroughOrderByCreatedAtAsc(p)
                .stream()
                .filter(d -> d.getResolvedNodeCode() != null)
                .map(d -> new PathStep(
                        0,
                        d.getId(),
                        d.getNode().getNodeCode(),
                        d.getResolvedNodeCode(),
                        d.getBranchType(),
                        d.getImpactLevel(),
                        d.getCreatedAt()
                ))
                .toList();

        List<PathStep> fixed = new ArrayList<>();

        int i = 1;

        for (var s : steps) {
            fixed.add(new PathStep(
                    i++,
                    s.decisionId(),
                    s.fromNodeCode(),
                    s.toNodeCode(),
                    s.branchType(),
                    s.impactLevel(),
                    s.createdAt()
            ));
        }

        return new PathResponse(
                p.getId(),
                p.getPlayerTag(),
                p.getStatus(),
                p.getEndingCode(),
                p.getStartNodeCode(),
                p.getCurrentNode().getNodeCode(),
                fixed
        );
    }
}