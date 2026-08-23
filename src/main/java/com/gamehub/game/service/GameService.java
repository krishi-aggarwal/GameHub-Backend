package com.gamehub.game.service;

import com.gamehub.domain.game.Game;

import com.gamehub.domain.user.User;
import com.gamehub.game.dto.CreateGameRequest;
import com.gamehub.game.dto.GameResponse;
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



}
