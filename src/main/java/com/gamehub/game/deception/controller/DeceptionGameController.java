package com.gamehub.game.deception.controller;

import com.gamehub.domain.user.User;
import com.gamehub.game.deception.domain.GameSession;
import com.gamehub.game.deception.domain.NightAction;
import com.gamehub.game.deception.dto.GameStateResponse;
import com.gamehub.game.deception.dto.NightActionRequest;
import com.gamehub.game.deception.dto.StartGameRequest;
import com.gamehub.game.deception.dto.StartGameResponse;
import com.gamehub.game.deception.service.DeceptionGameService;
import com.gamehub.game.deception.service.GameSessionRegistry;
import com.gamehub.game.deception.service.NightActionService;
import com.gamehub.game.exception.GameSessionNotFoundException;
import com.gamehub.mainDto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/deception/rooms")
public class DeceptionGameController {
    private final DeceptionGameService deceptionGameService;
    private final GameSessionRegistry gameSessionRegistry;
    private final NightActionService nightActionService;

    public DeceptionGameController(DeceptionGameService deceptionGameService,
                                   GameSessionRegistry gameSessionRegistry,
                                   NightActionService nightActionService){
        this.deceptionGameService = deceptionGameService;
        this.gameSessionRegistry=gameSessionRegistry;
        this.nightActionService=nightActionService;
    }

    @PostMapping("/{roomCode}/start")
    public ResponseEntity<ApiResponse<StartGameResponse>> startGame(
            @PathVariable String roomCode,
            Authentication authentication,
            @RequestBody StartGameRequest request
            ){
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(
                new ApiResponse(
                        "Game Started",
                        deceptionGameService.startGame(roomCode,user,request)
                )
        );
    }

    @GetMapping("/{sessionId}/state")
    public ResponseEntity<ApiResponse<GameStateResponse>> getGameState(
            @PathVariable UUID sessionId,
            Authentication authentication
    ) {

        // Get the currently authenticated user from JWT/Spring Security.
        User user = (User) authentication.getPrincipal();

        GameStateResponse response =
                deceptionGameService.getGameState(sessionId, user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Game State",
                        response
                )
        );
    }

    @PostMapping("/{sessionId}/night/{action}")
    public ResponseEntity<ApiResponse<String>> performNightAction(
            @PathVariable UUID sessionId,
            @PathVariable NightAction action,
            Authentication authentication,
            @RequestBody NightActionRequest request
    ) {

        User user = (User) authentication.getPrincipal();

        // Find the active game session.
        GameSession gameSession =
                gameSessionRegistry.retrieveSession(sessionId);

        if (gameSession == null) {
            throw new GameSessionNotFoundException(
                    "Game Session not found"
            );
        }

        // Perform validation + action.
        nightActionService.performAction(
                gameSession,
                user,
                action,
                request
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Night action submitted",
                        "SUCCESS"
                )
        );
    }


}
