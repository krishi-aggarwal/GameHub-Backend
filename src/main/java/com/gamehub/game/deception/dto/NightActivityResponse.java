package com.gamehub.game.deception.dto;

/**
 * Public-safe information about what is happening during the night.
 *
 * IMPORTANT:
 * This response must NEVER contain:
 * - target user ID
 * - target username
 * - role of the player performing the action
 * - investigation result
 *
 * It is only for observer/dead-player UI.
 */
public class NightActivityResponse {

    private String message;

    public NightActivityResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}