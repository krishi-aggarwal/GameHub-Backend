package com.gamehub.game.deception.dto;

public class VoteRequest {

    private Long targetUserId;

    public VoteRequest() {
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }
}