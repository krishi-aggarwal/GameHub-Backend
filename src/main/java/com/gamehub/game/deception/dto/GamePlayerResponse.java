package com.gamehub.game.deception.dto;

public class GamePlayerResponse {

    private Long userId;
    private String displayName;
    private boolean alive;

    public GamePlayerResponse(Long userId, String displayName, boolean alive) {
        this.userId = userId;
        this.displayName = displayName;
        this.alive = alive;
    }

    public Long getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isAlive() {
        return alive;
    }
}