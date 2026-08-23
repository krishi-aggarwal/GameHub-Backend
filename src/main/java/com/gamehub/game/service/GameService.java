package com.gamehub.game.service;

import com.gamehub.domain.game.Game;

import com.gamehub.game.dto.CreateGameRequest;
import com.gamehub.game.dto.GameResponse;
import com.gamehub.game.exception.GameExistsException;
import com.gamehub.game.exception.InvalidPlayerRangeException;
import com.gamehub.repository.GameRepository;

import org.springframework.stereotype.Service;

@Service
public class GameService {
    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository){
        this.gameRepository = gameRepository;
    }

    public GameResponse createGame(CreateGameRequest request){
        if(gameRepository.existsBySlug(request.getSlug())){
            throw new GameExistsException("Game already Exists!");
        }

        if(request.getMinPlayers() > request.getMaxPlayers()){
            throw new InvalidPlayerRangeException("Minimum players cannot be greater than maximum players");
        }

        Game newGame = Game.create(
                request.getName(),
                request.getSlug(),
                request.getDescription(),
                request.getMinPlayers(),
                request.getMaxPlayers(),
                request.getGameType(),
                request.getThumbnailUrl()
        );

        Game savedGame = gameRepository.save(newGame);

        GameResponse gameResponse = new GameResponse();
        gameResponse.setGameId(savedGame.getGameId());
        gameResponse.setName(savedGame.getName());
        gameResponse.setDescription(savedGame.getDescription());
        gameResponse.setGameType(savedGame.getGameType());
        gameResponse.setSlug(savedGame.getSlug());
        gameResponse.setMinPlayers(savedGame.getMinPlayers());
        gameResponse.setMaxPlayers(savedGame.getMaxPlayers());
        gameResponse.setStatus(savedGame.getStatus());
        gameResponse.setThumbnailUrl(savedGame.getThumbnailUrl());
        gameResponse.setCreatedAt(savedGame.getCreatedAt());
        gameResponse.setUpdatedAt(savedGame.getUpdatedAt());

        return gameResponse;
    }
}
