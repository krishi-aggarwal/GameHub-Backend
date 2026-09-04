
        package com.gamehub.game.deception.controller;

import com.gamehub.domain.user.User;
import com.gamehub.game.deception.dto.ChatMessageRequest;
import com.gamehub.game.deception.service.GameChatService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
public class GameChatController {

    private final GameChatService gameChatService;

    public GameChatController(
            GameChatService gameChatService
    ) {
        this.gameChatService = gameChatService;
    }

    /**
     * Receives a chat message from a connected player.
     *
     * Client sends:
     *
     * /app/game/{sessionId}/chat
     *
     * Server broadcasts through:
     *
     * /topic/game/{sessionId}
     */
    @MessageMapping("/game/{sessionId}/chat")
    public void sendMessage(
            @DestinationVariable UUID sessionId,
            ChatMessageRequest request,
            Principal principal
    ) {

        // Spring Security's Principal is normally an Authentication.
        Authentication authentication =
                (Authentication) principal;

        // Our application stores User as the authenticated principal.
        User user =
                (User) authentication.getPrincipal();

        gameChatService.sendMessage(
                sessionId,
                user,
                request
        );
    }
}

