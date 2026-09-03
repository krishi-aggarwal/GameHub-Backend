package com.gamehub.game.deception.dto;

import com.gamehub.game.deception.domain.DeceptionRole;

public class NightResultResponse {

    private Long eliminatedUserId;

    private Long investigatedUserId;

    private DeceptionRole investigationResult;

    public NightResultResponse(
            Long eliminatedUserId,
            Long investigatedUserId,
            DeceptionRole investigationResult
    ) {
        this.eliminatedUserId = eliminatedUserId;
        this.investigatedUserId = investigatedUserId;
        this.investigationResult = investigationResult;
    }

    public Long getEliminatedUserId() {
        return eliminatedUserId;
    }

    public Long getInvestigatedUserId() {
        return investigatedUserId;
    }

    public DeceptionRole getInvestigationResult() {
        return investigationResult;
    }
}