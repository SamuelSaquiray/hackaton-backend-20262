package com.tuckersoft.branchengine.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuckersoft.branchengine.dto.ErrorResponse;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Instant;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtFilter jwtFilter,
            ObjectMapper mapper
    ) throws Exception {

        http
                .csrf(c -> c.disable())

                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(a -> a

                        // Registro y login son públicos
                        .requestMatchers(
                                "/api/v1/auth/register",
                                "/api/v1/auth/login"
                        ).permitAll()

                        // Cada usuario puede consultar su propia cuenta
                        .requestMatchers("/api/v1/users/me")
                        .authenticated()

                        // Listado y gestión de usuarios: solamente ADMIN
                        .requestMatchers("/api/v1/users/**")
                        .hasRole("ADMIN")

                        // Creación de nodos: solamente ADMIN
                        // Solo POST /nodes requiere ADMIN.
                        .requestMatchers(HttpMethod.POST, "/api/v1/nodes")
                        .hasRole("ADMIN")

                        // GET de nodos: cualquier usuario autenticado.
                        .requestMatchers(HttpMethod.GET, "/api/v1/nodes/**")
                        .authenticated()

                        // Todo lo demás requiere autenticación
                        .anyRequest()
                        .authenticated()
                )

                .exceptionHandling(e ->
                        e
                                .authenticationEntryPoint((req, res, ex) ->
                                        write(
                                                res,
                                                mapper,
                                                401,
                                                "UNAUTHORIZED",
                                                "Authentication required",
                                                req
                                        )
                                )

                                .accessDeniedHandler((req, res, ex) ->
                                        write(
                                                res,
                                                mapper,
                                                403,
                                                "FORBIDDEN",
                                                "Access denied",
                                                req
                                        )
                                )
                )

                .addFilterBefore(
                        (Filter) jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    private static void write(
            HttpServletResponse res,
            ObjectMapper mapper,
            int status,
            String type,
            String msg,
            HttpServletRequest req
    ) throws java.io.IOException {

        res.setStatus(status);
        res.setContentType("application/json");

        mapper.writeValue(
                res.getWriter(),
                new ErrorResponse(
                        type,
                        msg,
                        Instant.now(),
                        req.getRequestURI()
                )
        );
    }
}
