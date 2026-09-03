package com.gamehub.game.deception.domain;

public class VoteEntry {

    private Long voterUserId;
    private Long targetUserId;

    public VoteEntry(
            Long voterUserId,
            Long targetUserId
    ) {
        this.voterUserId = voterUserId;
        this.targetUserId = targetUserId;
    }

    public Long getVoterUserId() {
        return voterUserId;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }
}