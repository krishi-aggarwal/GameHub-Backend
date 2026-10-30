package com.gamehub.game.fishpond.controller;

import com.gamehub.domain.user.User;
import com.gamehub.game.fishpond.dto.FishpondDtos.ChatRequest;
import com.gamehub.game.fishpond.dto.FishpondDtos.GameState;
import com.gamehub.game.fishpond.dto.FishpondDtos.NavigationRequest;
import com.gamehub.game.fishpond.dto.FishpondDtos.StartResponse;
import com.gamehub.game.fishpond.dto.FishpondDtos.SubmissionRequest;
import com.gamehub.game.fishpond.service.FishpondGameService;
import com.gamehub.mainDto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fishpond/rooms")
public class FishpondController {

    private final FishpondGameService fishpondGameService;

    public FishpondController(FishpondGameService fishpondGameService) {
        this.fishpondGameService = fishpondGameService;
    }

    @PostMapping("/{roomCode}/start")
    public ResponseEntity<ApiResponse<StartResponse>> start(
            @PathVariable String roomCode,
            Authentication authentication
    ) {
        StartResponse response = fishpondGameService.start(
                roomCode,
                (User) authentication.getPrincipal()
        );
        return ResponseEntity.ok(new ApiResponse<>("Fishpond started", response));
    }

    @GetMapping("/{roomCode}/state")
    public ResponseEntity<ApiResponse<GameState>> getState(
            @PathVariable String roomCode,
            Authentication authentication
    ) {
        GameState response = fishpondGameService.getState(
                roomCode,
                (User) authentication.getPrincipal()
        );
        return ResponseEntity.ok(new ApiResponse<>("Fishpond state", response));
    }

    @PostMapping("/{roomCode}/submit")
    public ResponseEntity<ApiResponse<GameState>> submit(
            @PathVariable String roomCode,
            @RequestBody SubmissionRequest request,
            Authentication authentication
    ) {
        GameState response = fishpondGameService.submit(
                roomCode,
                (User) authentication.getPrincipal(),
                request == null ? null : request.message()
        );
        return ResponseEntity.ok(new ApiResponse<>("Message submitted", response));
    }

    @PostMapping("/{roomCode}/navigate")
    public ResponseEntity<ApiResponse<GameState>> navigate(
            @PathVariable String roomCode,
            @RequestBody NavigationRequest request,
            Authentication authentication
    ) {
        GameState response = fishpondGameService.navigate(
                roomCode,
                (User) authentication.getPrincipal(),
                request == null ? null : request.action()
        );
        return ResponseEntity.ok(new ApiResponse<>("Fishpond message changed", response));
    }

    @PostMapping("/{roomCode}/reveal-authors")
    public ResponseEntity<ApiResponse<GameState>> revealAuthors(
            @PathVariable String roomCode,
            Authentication authentication
    ) {
        GameState response = fishpondGameService.revealAuthors(
                roomCode,
                (User) authentication.getPrincipal()
        );
        return ResponseEntity.ok(new ApiResponse<>("Authors revealed", response));
    }

    @PostMapping("/{roomCode}/chat")
    public ResponseEntity<ApiResponse<GameState>> sendChat(
            @PathVariable String roomCode,
            @RequestBody ChatRequest request,
            Authentication authentication
    ) {
        GameState response = fishpondGameService.sendChat(
                roomCode,
                (User) authentication.getPrincipal(),
                request == null ? null : request.message()
        );
        return ResponseEntity.ok(new ApiResponse<>("Chat sent", response));
    }
}