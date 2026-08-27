package com.gamehub.domain.room;

import com.gamehub.domain.user.User;
import com.gamehub.room.dto.RoomResponse;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "room_player")
public class RoomPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomPlayerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id" , nullable = false)
    private GameRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id" , nullable = false)
    private User user;

    @PrePersist
    private void onCreate() {
        joinedAt = Instant.now();
    }
    @Column(nullable = false)
    private Instant joinedAt;

    public GameRoom getRoom() {
        return room;
    }

    public User getUser() {
        return user;
    }

    public Long getRoomPlayerId() {
        return roomPlayerId;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public static RoomPlayer create(GameRoom gameRoom, User user){
        RoomPlayer roomPLayer = new RoomPlayer();
        roomPLayer.room = gameRoom;
        roomPLayer.user = user;
        return roomPLayer;
    }

}
