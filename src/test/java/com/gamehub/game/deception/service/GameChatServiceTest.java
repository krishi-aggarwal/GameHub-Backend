package com.gamehub.game.deception.service;

import com.gamehub.domain.user.User;
import com.gamehub.game.deception.domain.DeceptionGameConfig;
import com.gamehub.game.deception.domain.DeceptionRole;
import com.gamehub.game.deception.domain.GameEventType;
import com.gamehub.game.deception.domain.GamePhase;
import com.gamehub.game.deception.domain.GamePlayer;
import com.gamehub.game.deception.domain.GameSession;
import com.gamehub.game.deception.dto.ChatMessageRequest;
import com.gamehub.game.exception.GameRuleViolationException;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class GameChatServiceTest {

    @Test
    void broadcastsChatOnlyDuringDay() {
        User user = user();
        GameSession session = new GameSession(null, new DeceptionGameConfig(Set.of()));
        session.setGamePhase(GamePhase.DAY);
        session.addPlayer(new GamePlayer(
                1L,
                user.getUsername(),
                user.getDisplayName(),
                null,
                DeceptionRole.INNOCENT,
                true
        ));

        GameSessionRegistry registry = mock(GameSessionRegistry.class);
        GameEventService events = mock(GameEventService.class);
        UUID sessionId = UUID.randomUUID();
        org.mockito.Mockito.when(registry.retrieveSession(sessionId)).thenReturn(session);

        ChatMessageRequest request = new ChatMessageRequest();
        request.setMessage("  hello everyone  ");
        new GameChatService(registry, events).sendMessage(sessionId, user, request);

        var event = org.mockito.ArgumentCaptor.forClass(Object.class);
        verify(events).publish(eq(session), eq(GameEventType.CHAT_MESSAGE), event.capture());
        assertEquals("hello everyone", ((com.gamehub.game.deception.dto.ChatMessageResponse) event.getValue()).getMessage());
    }

    @Test
    void rejectsChatOutsideDay() {
        User user = user();
        GameSession session = new GameSession(null, new DeceptionGameConfig(Set.of()));
        session.addPlayer(new GamePlayer(
                1L,
                user.getUsername(),
                user.getDisplayName(),
                null,
                DeceptionRole.INNOCENT,
                true
        ));

        GameSessionRegistry registry = mock(GameSessionRegistry.class);
        UUID sessionId = UUID.randomUUID();
        org.mockito.Mockito.when(registry.retrieveSession(sessionId)).thenReturn(session);
        ChatMessageRequest request = new ChatMessageRequest();
        request.setMessage("hello");

        assertThrows(
                IllegalArgumentException.class,
                () -> new GameChatService(registry, mock(GameEventService.class))
                        .sendMessage(sessionId, user, request)
        );
    }

    private User user() {
        User user = mock(User.class);
        when(user.getUserId()).thenReturn(1L);
        when(user.getUsername()).thenReturn("chat-player");
        when(user.getDisplayName()).thenReturn("Chat Player");
        when(user.getRole()).thenReturn(User.Role.USER);
        return user;
    }
}