package com.tuckersoft.branchengine.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, unique=true)
    private String email;
    @Column(nullable=false)
    private String password;
    @Column(nullable=false)
    private String displayName;
    @Column(nullable=false)
    private String role;
    @Column(nullable=false)
    private Instant createdAt;

    public Long getId(){return id;}
    public String getEmail(){return email;}
    public void setEmail(String v){email=v;}
    public String getPassword(){return password;}
    public void setPassword(String v){password=v;}
    public String getDisplayName(){return displayName;}
    public void setDisplayName(String v){displayName=v;}
    public String getRole(){return role;}
    public void setRole(String v){role=v;}
    public Instant getCreatedAt(){return createdAt;}
    public void setCreatedAt(Instant v){createdAt=v;}
}
