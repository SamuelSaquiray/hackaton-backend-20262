package com.tuckersoft.branchengine.controller;

import com.tuckersoft.branchengine.dto.NodeDtos.*;
import com.tuckersoft.branchengine.service.NodeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/nodes")
public class NodeController {

    private final NodeService nodes;

    public NodeController(NodeService nodes) {
        this.nodes = nodes;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NodeResponse> create(
            @Valid @RequestBody CreateNodeRequest r
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nodes.create(r));
    }

    @GetMapping
    public java.util.List<NodeResponse> all() {
        return nodes.all();
    }

    @GetMapping("/{id}")
    public NodeResponse get(@PathVariable Long id) {
        return nodes.get(id);
    }
}
