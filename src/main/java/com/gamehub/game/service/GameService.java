package com.gamehub.game.service;

import com.gamehub.domain.game.Game;

import com.gamehub.domain.game.GameStatus;
import com.gamehub.domain.game.GameType;
import com.gamehub.domain.user.User;
import com.gamehub.game.dto.CreateGameRequest;
import com.gamehub.game.dto.GameResponse;
import com.gamehub.game.dto.UpdateGameRequest;
import com.gamehub.game.exception.GameExistsException;
import com.gamehub.game.exception.GameNotExistsException;
import com.gamehub.game.exception.InvalidPlayerRangeException;
import com.gamehub.repository.GameRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class GameService {
    private final GameRepository gameRepository;

    private GameService(GameRepository gameRepository){
        this.gameRepository = gameRepository;
    }

    public GameResponse toResponse(Game game){
        GameResponse res = new GameResponse();
        res.setName(game.getName());
        res.setGameId(game.getGameId());
        res.setDescription(game.getDescription());
        res.setGameType(game.getGameType());
        res.setSlug(game.getSlug());
        res.setMinPlayers(game.getMinPlayers());
        res.setMaxPlayers(game.getMaxPlayers());
        res.setStatus(game.getStatus());
        res.setThumbnailUrl(game.getThumbnailUrl());
        res.setCreatedAt(game.getCreatedAt());
        res.setUpdatedAt(game.getUpdatedAt());
        return res;
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

        return toResponse(savedGame);
    }

    public List<GameResponse> getAllGames(){

        List<Game> games = gameRepository.findAll();

        List<GameResponse> response = new ArrayList<>();

        for(Game game : games){
            response.add(toResponse(game));
        }

        return response;
    }

    public GameResponse getGameById(Long gameId) {

        Optional<Game> gameOptional = gameRepository.findById(gameId);

        if (gameOptional.isEmpty()) {
            throw new GameNotExistsException("Game not found!");
        }

        return toResponse(gameOptional.get());
    }

//    public GameResponse getGameById(Long gameId) {
//
//        Game game = gameRepository.findById(gameId)
//                .orElseThrow(() ->
//                        new GameNotExistsException("Game not found!")
//                );
//
//        return toResponse(game);
//    }

    public GameResponse getGameBySlug(String slug){
        Game game = gameRepository.findBySlug(slug)
                .orElseThrow(()->new GameNotExistsException("Game not Found!"));
        return toResponse(game);
    }


    public GameResponse updateGame(
            Long gameId,
            UpdateGameRequest request
    ) {

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() ->
                        new GameNotExistsException(
                                "Game not Found!"
                        )
                );

        /*
         * Keep existing values when a field
         * is not provided.
         */
        String name =
                request.getName() != null
                        ? request.getName()
                        : game.getName();

        String slug =
                request.getSlug() != null
                        ? request.getSlug()
                        : game.getSlug();

        String description =
                request.getDescription() != null
                        ? request.getDescription()
                        : game.getDescription();

        int minPlayers =
                request.getMinPlayers() != null
                        ? request.getMinPlayers()
                        : game.getMinPlayers();

        int maxPlayers =
                request.getMaxPlayers() != null
                        ? request.getMaxPlayers()
                        : game.getMaxPlayers();

        GameType gameType =
                request.getGameType() != null
                        ? request.getGameType()
                        : game.getGameType();

        GameStatus status =
                request.getStatus() != null
                        ? request.getStatus()
                        : game.getStatus();

        String thumbnailUrl =
                request.getThumbnailUrl() != null
                        ? request.getThumbnailUrl()
                        : game.getThumbnailUrl();

        // Validate final player range
        if (minPlayers > maxPlayers) {
            throw new InvalidPlayerRangeException(
                    "Minimum players cannot be greater than maximum players"
            );
        }

        // Check slug only when changing it
        if (!slug.equals(game.getSlug())
                && gameRepository.existsBySlug(slug)) {

            throw new GameExistsException(
                    "Game with this slug already Exists!"
            );
        }

        game.updateDetails(
                name,
                slug,
                description,
                minPlayers,
                maxPlayers,
                gameType,
                thumbnailUrl,
                status
        );

        Game updatedGame =
                gameRepository.save(game);

        return toResponse(updatedGame);
    }



    public void deleteGame(Long gameId) {
        if (!gameRepository.existsById(gameId)) {
            throw new GameNotExistsException("Game not Found!");
        }

        gameRepository.deleteById(gameId);
    }



}
