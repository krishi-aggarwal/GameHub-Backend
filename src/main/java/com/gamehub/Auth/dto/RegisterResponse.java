package com.gamehub.Auth.dto;

import com.gamehub.domain.user.User;

import java.time.Instant;

public class RegisterResponse {
    private Long userId;
    private String username;
    private String email;
    private String displayName;
    private User.Role role;
    private Instant createdAt;

    public RegisterResponse() {
    }

    public RegisterResponse(
            Long userId,
            String username,
            String email,
            String displayName,
            User.Role role,
            Instant createdAt
    ) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public User.Role getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
