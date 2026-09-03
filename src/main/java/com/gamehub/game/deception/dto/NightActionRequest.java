package com.gamehub.game.deception.dto;

public class NightActionRequest {
    // User ID of the player being targeted.
    private Long targetUserId;

    public NightActionRequest() {
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }
}
