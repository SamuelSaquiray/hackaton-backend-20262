package com.tuckersoft.branchengine.controller;

import com.tuckersoft.branchengine.dto.PlaythroughDtos.*;
import com.tuckersoft.branchengine.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/playthroughs")
public class PlaythroughController {

    private final PlaythroughService service;
    private final UserService users;

    public PlaythroughController(
            PlaythroughService service,
            UserService users) {
        this.service = service;
        this.users = users;
    }

    @PostMapping
    public ResponseEntity<Response> create(
            @Valid @RequestBody CreateRequest r,
            org.springframework.security.core.Authentication a) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(r, users.current(a.getName())));
    }

    @GetMapping
    public java.util.List<Response> mine(
            org.springframework.security.core.Authentication a) {

        var user = users.current(a.getName());

        if (user.getRole().equals("ROLE_ADMIN")) {
            return service.all();
        }

        return service.mine(user);
    }

    @GetMapping("/{id}")
    public Response get(
            @PathVariable Long id,
            org.springframework.security.core.Authentication a) {

        var u = users.current(a.getName());

        return service.dto(
                service.get(
                        id,
                        u,
                        u.getRole().equals("ROLE_ADMIN")
                )
        );
    }

    @GetMapping("/{id}/path")
    public PathResponse path(
            @PathVariable Long id,
            org.springframework.security.core.Authentication a) {

        var u = users.current(a.getName());

        return service.path(
                id,
                u,
                u.getRole().equals("ROLE_ADMIN")
        );
    }
}