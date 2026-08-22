package com.gamehub.user.dto;

import com.gamehub.domain.user.User;

public class UserProfileResponse {
    private long userId;
    private String username;
    private String email;
    private String displayName;
    private String avatarUrl;
    private User.Role role;


    public UserProfileResponse(){

    }

    public UserProfileResponse(
            long userId,
            String username,
            String email,
            String displayName,
            String avatarUrl,
            User.Role role
    ){
        this.userId=userId;
        this.username=username;
        this.email=email;
        this.displayName=displayName;
        this.avatarUrl=avatarUrl;
        this.role=role;
    }
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public User.Role getRole() {
        return role;
    }

    public void setRole(User.Role role) {
        this.role = role;
    }
}
