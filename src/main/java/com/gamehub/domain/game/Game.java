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

    public Long getGameId() {
        return gameId;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public GameType getGameType() {
        return gameType;
    }

    public GameStatus getStatus() {
        return status;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    protected Game(){

    }

    public static Game create(
            String name,
            String slug,
            String description,
            Integer minPlayers,
            Integer maxPlayers,
            GameType gameType,
            String thumbnailUrl
    ) {
        Game g = new Game();
        g.name=name;
        g.slug=slug;
        g.description=description;
        g.minPlayers=minPlayers;
        g.maxPlayers=maxPlayers;
        g.gameType=gameType;
        g.thumbnailUrl=thumbnailUrl;
        g.status = GameStatus.COMING_SOON;

        return g;
    }

    public void updateDetails(
            String name,
            String slug,
            String description,
            int minPlayers,
            int maxPlayers,
            GameType gameType,
            String thumbnailUrl,
            GameStatus status
    ) {
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.gameType = gameType;
        this.thumbnailUrl = thumbnailUrl;
        this.status = status;
    }
}
