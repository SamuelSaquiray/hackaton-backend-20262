package com.tuckersoft.branchengine.controller;
import com.tuckersoft.branchengine.dto.UserDtos.*;
import com.tuckersoft.branchengine.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/users")
public class UserController {
    private final UserService users;
    public UserController(UserService users){this.users=users;}
    @GetMapping("/me") public Response me(org.springframework.security.core.Authentication a){return users.dto(users.current(a.getName()));}
    @GetMapping @PreAuthorize("hasRole('ADMIN')") public java.util.List<Response> all(){return users.all();}
    @PatchMapping("/{id}/role") @PreAuthorize("hasRole('ADMIN')")
    public Response role(@PathVariable Long id,@Valid @RequestBody RoleRequest r,org.springframework.security.core.Authentication a){
        var admin=users.current(a.getName());return users.dto(users.changeRole(id,r.role(),admin));
    }
}
