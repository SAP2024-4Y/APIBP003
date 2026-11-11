package com.apibp.events.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role = "USER";

    public Long getId()               { return id; }
    public String getUsername()       { return username; }
    public String getPassword()       { return password; }
    public String getRole()           { return role; }
    public void setId(Long id)        { this.id = id; }
    public void setUsername(String u) { this.username = u; }
    public void setPassword(String p) { this.password = p; }
    public void setRole(String r)     { this.role = r; }
}
