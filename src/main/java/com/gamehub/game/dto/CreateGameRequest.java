package com.gamehub.game.dto;

import com.gamehub.domain.game.GameType;
import jakarta.validation.constraints.*;

public class CreateGameRequest {
    @NotBlank
    private String name;

    @NotBlank
    @Size(max = 100)
    private String slug;

    @Size(max = 250)
    private String description;

    @NotNull
    @Min(2)
    private Integer minPlayers;
    @NotNull
    @Min(2)
    private Integer maxPlayers;

    @NotNull
    private GameType gameType;
    private String thumbnailUrl;


    public CreateGameRequest(){}

    public CreateGameRequest(
            String name,
            String slug,
            String description,
            Integer minPlayers,
            Integer maxPlayers,
            GameType gameType,
            String thumbnailUrl
    ){
        this.name=name;
        this.slug=slug;
        this.description=description;
        this.minPlayers=minPlayers;
        this.maxPlayers=maxPlayers;
        this.gameType=gameType;
        this.thumbnailUrl=thumbnailUrl;
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

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
