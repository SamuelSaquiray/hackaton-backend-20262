package com.tuckersoft.branchengine.dto;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
public final class UserDtos {
    private UserDtos(){}
    public record Response(Long id,String email,String displayName,String role,Instant createdAt){}
    public record RoleRequest(@NotBlank String role){}
}
