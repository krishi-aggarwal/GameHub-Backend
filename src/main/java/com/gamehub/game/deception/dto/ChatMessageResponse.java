
package com.gamehub.game.deception.dto;

public class ChatMessageResponse {

    private Long userId;
    private String username;
    private String displayName;
    private String avatarUrl;
    private String message;

    public ChatMessageResponse(
            Long userId,
            String username,
            String displayName,
            String avatarUrl,
            String message
    ) {
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.message = message;
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

    public String getMessage() {
        return message;
    }
}

