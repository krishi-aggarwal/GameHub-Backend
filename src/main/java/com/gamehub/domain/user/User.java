package com.gamehub.domain.user;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.Instant;


@Entity
@Table(name = "users")


public class User {

    public enum Role{
        ADMIN,
        USER
    }


    @Id // primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false , updatable = false)
    private Long userId;

    //@Size(max = 25) // application validation
    @Column(nullable = false , unique = true , length = 25) // length => db varchar(25)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false , unique = true , length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(length = 30)
    private String displayName;

    @Column(nullable = true , length = 500)
    private String avatarUrl;

    @PrePersist
    private void onCreate(){
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }
    @Column(nullable = false , updatable = false)
    private Instant createdAt;

    @PreUpdate
    private void onUpdate(){
        updatedAt = Instant.now();
    }
    @Column(nullable = false)
    private Instant updatedAt;

    protected User(){

    }


    public User(String username , String email , String passwordHash , String displayName , Role role){
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash)
    {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }





}
