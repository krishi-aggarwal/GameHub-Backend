
package com.gamehub.game.deception.dto;

public class GamePlayerResponse {

    private Long userId;
    private String displayName;
    private String avatarUrl;
    private boolean alive;
    private boolean left;

    public GamePlayerResponse(
            Long userId,
            String displayName,
            String avatarUrl,
            boolean alive,
            boolean left
    ) {
        this.userId = userId;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.alive = alive;
        this.left = left;
    }

    public Long getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isLeft() {
        return left;
    }
}

