package com.tuckersoft.branchengine.service;

import com.tuckersoft.branchengine.dto.*;
import com.tuckersoft.branchengine.entity.User;
import com.tuckersoft.branchengine.exception.AppException;
import com.tuckersoft.branchengine.repository.UserRepository;
import com.tuckersoft.branchengine.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public UserService(
            UserRepository repo,
            PasswordEncoder encoder,
            JwtService jwt
    ) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest r) {

        if (repo.existsByEmail(r.email())) {
            throw new AppException(
                    HttpStatus.CONFLICT,
                    "EMAIL_EXISTS",
                    "Email already registered"
            );
        }

        User u = new User();

        u.setEmail(r.email());
        u.setPassword(encoder.encode(r.password()));
        u.setDisplayName(r.displayName());

        // El usuario NO puede elegir su propio rol durante el registro.
        u.setRole("ROLE_USER");

        u.setCreatedAt(Instant.now());

        repo.save(u);

        return auth(u);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest r) {

        /*
         * No diferenciamos entre:
         * - email inexistente
         * - contraseña incorrecta
         *
         * Ambos casos devuelven HTTP 401.
         */
        User u = repo.findByEmail(r.email())
                .orElseThrow(() -> new AppException(
                        HttpStatus.UNAUTHORIZED,
                        "UNAUTHORIZED",
                        "Invalid credentials"
                ));

        if (!encoder.matches(r.password(), u.getPassword())) {
            throw new AppException(
                    HttpStatus.UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "Invalid credentials"
            );
        }

        return auth(u);
    }

    private AuthDtos.AuthResponse auth(User u) {

        return new AuthDtos.AuthResponse(
                jwt.generate(u.getEmail(), u.getRole()),
                "Bearer",
                u.getEmail(),
                u.getDisplayName(),
                u.getRole()
        );
    }

    public User current(String email) {

        return repo.findByEmail(email)
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND,
                        "USER_NOT_FOUND",
                        "User not found"
                ));
    }

    public UserDtos.Response dto(User u) {

        return new UserDtos.Response(
                u.getId(),
                u.getEmail(),
                u.getDisplayName(),
                u.getRole(),
                u.getCreatedAt()
        );
    }

    public List<UserDtos.Response> all() {

        return repo.findAll()
                .stream()
                .map(this::dto)
                .toList();
    }

    public User changeRole(Long id, String role, User admin) {

        if (!role.equals("ROLE_USER") && !role.equals("ROLE_ADMIN")) {
            throw new AppException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_ROLE",
                    "Invalid role"
            );
        }

        if (admin.getId().equals(id)) {
            throw new AppException(
                    HttpStatus.BAD_REQUEST,
                    "SELF_ROLE_CHANGE",
                    "An administrator cannot change their own role"
            );
        }

        User u = repo.findById(id)
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND,
                        "USER_NOT_FOUND",
                        "User not found"
                ));

        u.setRole(role);

        return repo.save(u);
    }
}

