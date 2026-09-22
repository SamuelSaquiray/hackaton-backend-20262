package com.tuckersoft.branchengine.controller;

import com.tuckersoft.branchengine.dto.DecisionDtos;
import com.tuckersoft.branchengine.dto.DecisionDtos.*;
import com.tuckersoft.branchengine.entity.User;
import com.tuckersoft.branchengine.repository.*;
import com.tuckersoft.branchengine.service.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/decisions")
public class DecisionController {

    private final DecisionService service;
    private final UserService users;
    private final DecisionRepository decisions;
    private final RealityLogRepository logs;
    private final PlaythroughRepository plays;

    public DecisionController(
            DecisionService service,
            UserService users,
            DecisionRepository decisions,
            RealityLogRepository logs,
            PlaythroughRepository plays) {

        this.service = service;
        this.users = users;
        this.decisions = decisions;
        this.logs = logs;
        this.plays = plays;
    }

    @PostMapping
    public ResponseEntity<Response> create(
            @Valid @RequestBody CreateRequest r,
            @RequestHeader(
                    value = "X-Bandersnatch-Simulate",
                    defaultValue = ""
            ) String sim,
            org.springframework.security.core.Authentication a) {

        User u = users.current(a.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        service.decide(
                                r,
                                u,
                                false,
                                "MAIL_FAILURE".equals(sim)
                        )
                );
    }

    @GetMapping
    public DecisionDtos.PageResponse<Response> all(
            @RequestParam(required = false) String branchType,
            @RequestParam(required = false) String impactLevel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long playthroughId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            org.springframework.security.core.Authentication a) {

        User u = users.current(a.getName());

        var allowed = plays.findByUserOrderByCreatedAtDesc(u);

        var p = decisions.findByPlaythroughIn(
                allowed,
                org.springframework.data.domain.PageRequest.of(
                        Math.max(0, page - 1),
                        size
                )
        );

        var content = p.getContent()
                .stream()
                .filter(d ->
                        branchType == null
                                || d.getBranchType().equals(branchType))
                .filter(d ->
                        impactLevel == null
                                || d.getImpactLevel().equals(impactLevel))
                .filter(d ->
                        status == null
                                || d.getStatus().equals(status))
                .filter(d ->
                        playthroughId == null
                                || d.getPlaythrough().getId()
                                .equals(playthroughId))
                .map(service::dto)
                .toList();

        return new DecisionDtos.PageResponse<>(
                content,
                p.getTotalElements(),
                p.getTotalPages(),
                page,
                size
        );
    }

    @GetMapping("/{id}")
    public Response get(
            @PathVariable Long id,
            org.springframework.security.core.Authentication a) {

        User u = users.current(a.getName());

        var d = decisions.findById(id)
                .orElseThrow(() ->
                        new com.tuckersoft.branchengine.exception.AppException(
                                HttpStatus.NOT_FOUND,
                                "DECISION_NOT_FOUND",
                                "Decision not found"
                        )
                );

        if (!d.getPlaythrough().getUser().getId().equals(u.getId())
                && !u.getRole().equals("ROLE_ADMIN")) {

            throw new com.tuckersoft.branchengine.exception.AppException(
                    HttpStatus.FORBIDDEN,
                    "FORBIDDEN",
                    "Access denied"
            );
        }

        return service.dto(d);
    }

    @GetMapping("/{id}/reality-logs")
    public java.util.List<RealityLogResponse> logs(
            @PathVariable Long id,
            org.springframework.security.core.Authentication a) {

        User u = users.current(a.getName());

        var d = decisions.findById(id)
                .orElseThrow(() ->
                        new com.tuckersoft.branchengine.exception.AppException(
                                HttpStatus.NOT_FOUND,
                                "DECISION_NOT_FOUND",
                                "Decision not found"
                        )
                );

        if (!d.getPlaythrough().getUser().getId().equals(u.getId())
                && !u.getRole().equals("ROLE_ADMIN")) {

            throw new com.tuckersoft.branchengine.exception.AppException(
                    HttpStatus.FORBIDDEN,
                    "FORBIDDEN",
                    "Access denied"
            );
        }

        return logs.findByDecisionOrderByCreatedAtAsc(d)
                .stream()
                .map(x -> new RealityLogResponse(
                        x.getId(),
                        d.getId(),
                        x.getRecipientEmail(),
                        x.getSubject(),
                        x.getLogStatus(),
                        x.getErrorMessage(),
                        x.getSentAt(),
                        x.getCreatedAt()
                ))
                .toList();
    }
}