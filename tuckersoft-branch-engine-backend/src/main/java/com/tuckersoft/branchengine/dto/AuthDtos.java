package com.tuckersoft.branchengine.dto;
import jakarta.validation.constraints.*;
public final class AuthDtos {
    private AuthDtos(){}
    public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min=6) String password,
        @NotBlank @Size(min=3,max=60) String displayName,
        String role
    ) {}
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record AuthResponse(String token,String type,String email,String displayName,String role) {}
}
