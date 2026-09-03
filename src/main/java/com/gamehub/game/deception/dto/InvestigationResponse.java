package com.gamehub.game.deception.dto;

import com.gamehub.game.deception.domain.DeceptionRole;

import java.util.UUID;

public class InvestigationResponse {

    private UUID sessionId;
    private Long investigatedUserId;
    private DeceptionRole role;

    public InvestigationResponse(
            UUID sessionId,
            Long investigatedUserId,
            DeceptionRole role
    ) {
        this.sessionId = sessionId;
        this.investigatedUserId = investigatedUserId;
        this.role = role;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public Long getInvestigatedUserId() {
        return investigatedUserId;
    }

    public DeceptionRole getRole() {
        return role;
    }
}