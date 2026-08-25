package com.gamehub.game.controller;

import com.gamehub.game.dto.CreateGameRequest;
import com.gamehub.game.dto.GameResponse;
import com.gamehub.game.dto.UpdateGameRequest;
import com.gamehub.game.service.GameService;
import com.gamehub.mainDto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/games")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService){
        this.gameService=gameService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GameResponse>> createGame(
            @Valid @RequestBody CreateGameRequest request
    ){
        GameResponse response = gameService.createGame(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(
                        "Game Created Successfully",
                        response
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<GameResponse>> getAllGames(){

        return ResponseEntity.ok(gameService.getAllGames());
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameResponse> getGameById(
            @PathVariable Long gameId
    ) {
        return ResponseEntity.ok(gameService.getGameById(gameId));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<GameResponse> getGameBySlug(
            @PathVariable String slug
    ) {
        return ResponseEntity.ok(gameService.getGameBySlug(slug));
    }

    @PutMapping("/{gameId}")
    public ResponseEntity<ApiResponse<GameResponse>> updateGame(
            @PathVariable Long gameId,
            @Valid @RequestBody UpdateGameRequest request){
            return ResponseEntity.ok(new ApiResponse<>(
                    "Game Updated Successfully!",
                    gameService.updateGame(gameId,request)
            ));
    }

    @DeleteMapping("/{gameId}")
    public ResponseEntity<ApiResponse<Void>> deleteGame(
            @PathVariable Long gameId
    ){
        gameService.deleteGame(gameId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(
                "Game Deleted!",
                null
        ));
    }

}
