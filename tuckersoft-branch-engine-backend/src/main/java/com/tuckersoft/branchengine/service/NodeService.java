package com.tuckersoft.branchengine.service;

import com.tuckersoft.branchengine.dto.NodeDtos.*;
import com.tuckersoft.branchengine.entity.StoryNode;
import com.tuckersoft.branchengine.exception.AppException;
import com.tuckersoft.branchengine.repository.StoryNodeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class NodeService {

    private final StoryNodeRepository repo;

    public NodeService(StoryNodeRepository repo) {
        this.repo = repo;
    }

    public NodeResponse create(CreateNodeRequest r) {
        if (repo.existsByNodeCode(r.nodeCode())) {
            throw new AppException(
                    HttpStatus.CONFLICT,
                    "NODE_EXISTS",
                    "nodeCode already exists"
            );
        }

        StoryNode n = new StoryNode();
        n.setNodeCode(r.nodeCode());
        n.setTitle(r.title());
        n.setSceneText(r.sceneText());
        n.setBranchCapacity(r.branchCapacity());
        n.setCurrentBranches(0);
        n.setPrimaryBranchCode(r.primaryBranchCode());
        n.setGlitchBranchCode(r.glitchBranchCode());
        n.setCreatedAt(Instant.now());

        return dto(repo.save(n));
    }

    public NodeResponse get(Long id) {
        return dto(
                repo.findById(id).orElseThrow(() ->
                        new AppException(
                                HttpStatus.NOT_FOUND,
                                "NODE_NOT_FOUND",
                                "Node not found"
                        )
                )
        );
    }

    // GET /api/v1/nodes -> devuelve directamente un array
    public List<NodeResponse> all() {
        return repo.findAll()
                .stream()
                .map(this::dto)
                .toList();
    }

    // Se mantiene por si alguna otra parte del proyecto lo utiliza
    public PageResponse<NodeResponse> page(int page, int size) {
        var p = repo.findAll(
                org.springframework.data.domain.PageRequest.of(
                        Math.max(0, page - 1),
                        size
                )
        );

        return new PageResponse<>(
                p.getContent().stream().map(this::dto).toList(),
                p.getTotalElements(),
                p.getTotalPages(),
                page,
                size
        );
    }

    public StoryNode byCode(String code) {
        return repo.findByNodeCode(code)
                .orElseThrow(() ->
                        new AppException(
                                HttpStatus.NOT_FOUND,
                                "NODE_NOT_FOUND",
                                "Start node not found"
                        )
                );
    }

    public StoryNode findNullable(String code) {
        return code == null
                ? null
                : repo.findByNodeCode(code).orElse(null);
    }

    public NodeResponse dto(StoryNode n) {
        return new NodeResponse(
                n.getId(),
                n.getNodeCode(),
                n.getTitle(),
                n.getSceneText(),
                n.getBranchCapacity(),
                n.getCurrentBranches(),
                n.getPrimaryBranchCode(),
                n.getGlitchBranchCode(),
                n.getCreatedAt()
        );
    }
}