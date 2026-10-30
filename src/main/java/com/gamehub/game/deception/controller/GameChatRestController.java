package com.gamehub.game.deception.controller;

import com.gamehub.domain.user.User;
import com.gamehub.game.deception.dto.ChatMessageRequest;
import com.gamehub.game.deception.service.GameChatService;
import com.gamehub.mainDto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/deception/rooms")
public class GameChatRestController {

    private final GameChatService gameChatService;

    public GameChatRestController(GameChatService gameChatService) {
        this.gameChatService = gameChatService;
    }

    @PostMapping("/{sessionId}/chat")
    public ResponseEntity<ApiResponse<Void>> sendMessage(
            @PathVariable UUID sessionId,
            @Valid @RequestBody ChatMessageRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        gameChatService.sendMessage(sessionId, user, request);
        return ResponseEntity.ok(new ApiResponse<>("Message sent", null));
    }
}