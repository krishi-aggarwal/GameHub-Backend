package com.gamehub.domain.game;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "game")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gameId;

    @Column(nullable = false)
    private String name;

    //api-name of game
    @Column(nullable = false, unique = true, length = 100)
    private String slug;


    @Column(length = 250)
    private String description;

    private int minPlayers;
    private int maxPlayers;

    @Enumerated(EnumType.STRING)
    private GameType gameType;

    @Enumerated(EnumType.STRING)
    private GameStatus status;
    private String thumbnailUrl;

    @PrePersist
    private void onCreate(){
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }
    @Column(nullable = false , updatable = false)
    private Instant createdAt;

    @PreUpdate
    private void onUpdate(){
        updatedAt = Instant.now();
    }
    @Column(nullable = false)
    private Instant updatedAt;
}
