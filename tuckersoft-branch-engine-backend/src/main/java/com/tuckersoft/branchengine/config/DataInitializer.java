package com.tuckersoft.branchengine.config;

import com.tuckersoft.branchengine.entity.User;
import com.tuckersoft.branchengine.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.Instant;

@Configuration
public class DataInitializer {
    @Bean CommandLineRunner init(UserRepository repo,PasswordEncoder encoder,
        @Value("${app.admin.display-name}") String name,@Value("${app.admin.email}") String email,@Value("${app.admin.password}") String password){
        return args->{
            if(repo.findByEmail(email).isEmpty()){
                User u=new User();u.setDisplayName(name);u.setEmail(email);u.setPassword(encoder.encode(password));u.setRole("ROLE_ADMIN");u.setCreatedAt(Instant.now());repo.save(u);
            }
        };
    }
}
