package com.bookmyseat.model;

import com.bookmyseat.model.enums.Role;
import java.time.LocalDateTime;

public class User {
    private int userId;
    private String name;
    private String email;
    private String passwordHash;
    private String phone;
    private Role role;
    private LocalDateTime createdAt;

    public User() {}

    public User(int userId, String name, String email, String passwordHash,
                String phone, Role role, LocalDateTime createdAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.role = role;
        this.createdAt = createdAt;
    }

    public int getUserId()              { return userId; }
    public void setUserId(int v)        { this.userId = v; }
    public String getName()             { return name; }
    public void setName(String v)       { this.name = v; }
    public String getEmail()            { return email; }
    public void setEmail(String v)      { this.email = v; }
    public String getPasswordHash()     { return passwordHash; }
    public void setPasswordHash(String v){ this.passwordHash = v; }
    public String getPhone()            { return phone; }
    public void setPhone(String v)      { this.phone = v; }
    public Role getRole()               { return role; }
    public void setRole(Role v)         { this.role = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v){ this.createdAt = v; }

    @Override
    public String toString() {
        return "[" + userId + "] " + name + " <" + email + "> (" + role + ")";
    }
}
