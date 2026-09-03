package com.gamehub.game.deception.domain;

public class NightActionEntry {
    private NightAction action;
    private Long targetUserId;

    public NightActionEntry(NightAction action, Long targetUserId) {
        this.action = action;
        this.targetUserId = targetUserId;
    }

    public NightAction getAction() {
        return action;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }
}
