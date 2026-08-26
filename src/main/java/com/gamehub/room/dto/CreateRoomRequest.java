package com.gamehub.room.dto;

import jakarta.validation.constraints.NotNull;

public class CreateRoomRequest {
    @NotNull
    private Long gameId;

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }
}
