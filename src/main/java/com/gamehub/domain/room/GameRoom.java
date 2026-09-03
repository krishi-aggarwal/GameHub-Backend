package com.gamehub.domain.room;

import com.gamehub.domain.game.Game;
import com.gamehub.domain.user.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "game_room")
public class GameRoom {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    @Column(nullable = false, unique = true, length = 10)
    private String roomCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id" , nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id" , nullable = false)
    private User host;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus roomStatus;

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public void setRoomStatus(RoomStatus roomStatus) {
        this.roomStatus = roomStatus;
    }

    private int maxPlayers;

    @OneToMany(mappedBy = "room")
    private List<RoomPlayer> players = new ArrayList<>();

    @PrePersist
    private void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }
    private Instant createdAt;

    public Long getRoomId() {
        return roomId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public Game getGame() {
        return game;
    }

    public RoomStatus getRoomStatus() {
        return roomStatus;
    }

    public User getHost() {
        return host;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public List<RoomPlayer> getPlayers() {
        return players;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = Instant.now();
    }
    private Instant updatedAt;

    protected GameRoom() {
    }

    public static GameRoom create(
            String roomCode,
            Game game,
            User host,
            int maxPlayers
    ) {
        GameRoom room = new GameRoom();

        room.roomCode = roomCode;
        room.game = game;
        room.host = host;
        room.maxPlayers = maxPlayers;
        room.roomStatus = RoomStatus.WAITING;

        return room;
    }

    public void changeHost(User user) {
        this.host = user;
    }
}
