package com.gamehub.game.deception.domain;

public class GamePlayer {

    private Long userId;
    private String username;
    private String displayName;
    private String avatarUrl;

    private DeceptionRole role;

    private boolean isAlive;
    private boolean left;


    public GamePlayer(
            Long userId,
            String username,
            String displayName,
            String avatarUrl,
            DeceptionRole role,
            boolean isAlive
    ) {
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.isAlive = isAlive;
    }


    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public DeceptionRole getRole() {
        return role;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public boolean isLeft() {
        return left;
    }


    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public void leaveGame() {
        left = true;
        isAlive = false;
    }

    public void assignRole(DeceptionRole role) {
        this.role = role;
    }
}