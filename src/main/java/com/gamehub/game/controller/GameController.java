package com.gamehub.game.controller;

import com.gamehub.game.dto.CreateGameRequest;
import com.gamehub.game.dto.GameResponse;
import com.gamehub.game.service.GameService;
import com.gamehub.mainDto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/games")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService){
        this.gameService=gameService;
    }

    @PostMapping("/")
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

}
