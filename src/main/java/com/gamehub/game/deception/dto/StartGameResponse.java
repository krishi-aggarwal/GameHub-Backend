package com.gamehub.game.deception.dto;

import java.util.UUID;

public class StartGameResponse {
    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    private UUID sessionId;
    public StartGameResponse(UUID sessionId){
        this.sessionId=sessionId;
    }
}
