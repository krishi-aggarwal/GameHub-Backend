package com.gamehub.room.dto;

import com.gamehub.domain.room.RoomPlayer;
import jakarta.persistence.PrePersist;

import java.time.Instant;

public class RoomPlayerResponse {
    private String username;


    private Instant joinedAt;

    public RoomPlayerResponse(){

    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }
}
