package com.tuckersoft.branchengine.security;

import com.tuckersoft.branchengine.entity.User;
import com.tuckersoft.branchengine.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwt;
    private final UserRepository userRepository;

    public JwtFilter(
            JwtService jwt,
            UserRepository userRepository
    ) {
        this.jwt = jwt;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain
    ) throws ServletException, IOException {

        String header = req.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {

            try {
                String token = header.substring(7);

                String email = jwt.parse(token).getSubject();

                User user = userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException("User not found: " + email)
                        );

                String role = user.getRole();

                var authorities = List.of(
                        new SimpleGrantedAuthority(role)
                );

                var authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                authorities
                        );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);

            } catch (Exception e) {
                System.out.println("JWT FILTER ERROR: " + e.getMessage());
                e.printStackTrace();
            }
        }

        chain.doFilter(req, res);
    }
}