
        package com.gamehub.game.dto;

import com.gamehub.domain.game.GameStatus;
import com.gamehub.domain.game.GameType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class UpdateGameRequest {

    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String slug;

    @Size(max = 250)
    private String description;

    @Min(2)
    private Integer minPlayers;

    @Min(2)
    private Integer maxPlayers;

    private GameType gameType;

    private GameStatus status;

    private String thumbnailUrl;

    public UpdateGameRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(Integer minPlayers) {
        this.minPlayers = minPlayers;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}

