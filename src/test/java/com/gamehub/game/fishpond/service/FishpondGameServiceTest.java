package com.gamehub.game.fishpond.service;

import com.gamehub.domain.game.Game;
import com.gamehub.domain.game.GameType;
import com.gamehub.domain.room.GameRoom;
import com.gamehub.domain.room.RoomPlayer;
import com.gamehub.domain.user.User;
import com.gamehub.game.exception.UnauthorizedException;
import com.gamehub.game.fishpond.dto.FishpondDtos.GameState;
import com.gamehub.repository.GameRoomRepository;
import com.gamehub.repository.RoomPlayerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FishpondGameServiceTest {

    @Test
    void authorsStayHiddenUntilHostRevealsAfterLastMessage() {
        User host = user(1L, "host");
        User playerTwo = user(2L, "player-two");
        User playerThree = user(3L, "player-three");
        GameRoom room = GameRoom.create(
                "POND01",
                Game.create("Fishpond", "fishpond", "", 3, 12, GameType.PARTY, null),
                host,
                12
        );
        List<RoomPlayer> roomPlayers = List.of(
                RoomPlayer.create(room, host),
                RoomPlayer.create(room, playerTwo),
                RoomPlayer.create(room, playerThree)
        );

        GameRoomRepository gameRoomRepository = mock(GameRoomRepository.class);
        RoomPlayerRepository roomPlayerRepository = mock(RoomPlayerRepository.class);
        when(gameRoomRepository.findByRoomCode("POND01"))
                .thenReturn(Optional.of(room));
        when(roomPlayerRepository.findByRoom(room)).thenReturn(roomPlayers);
        when(roomPlayerRepository.existsByRoomAndUser(room, host)).thenReturn(true);

        FishpondGameService service = new FishpondGameService(
                gameRoomRepository,
                roomPlayerRepository,
                mock(SimpMessagingTemplate.class)
        );
        service.start("POND01", host);
        service.submit("POND01", host, "A thoughtful note");
        service.submit("POND01", playerTwo, "A funny note");
        GameState reading = service.submit("POND01", playerThree, "A kind note");

        assertEquals("READING", reading.phase());
        assertNull(reading.currentMessage().authorName());
        assertTrue(reading.messages().isEmpty());
        assertFalse(reading.canRevealAuthors());
        assertThrows(
                UnauthorizedException.class,
                () -> service.navigate("POND01", playerTwo, "NEXT")
        );

        service.navigate("POND01", host, "NEXT");
        reading = service.navigate("POND01", host, "NEXT");
        assertTrue(reading.canRevealAuthors());

        GameState revealed = service.revealAuthors("POND01", host);
        assertEquals("COMPLETE", revealed.phase());
        assertTrue(revealed.authorsRevealed());
        assertEquals(3, revealed.messages().size());
        assertTrue(revealed.messages().stream().allMatch(item -> item.authorName() != null));

        GameState hidden = service.revealAuthors("POND01", host);
        assertEquals("READING", hidden.phase());
        assertFalse(hidden.authorsRevealed());
        assertTrue(hidden.messages().isEmpty());
        assertNull(hidden.currentMessage().authorName());
    }

    @Test
    void departedPlayerCanRejoinWhileSubmissionsAreOpen() {
        User host = user(11L, "host");
        User playerTwo = user(12L, "player-two");
        User playerThree = user(13L, "player-three");
        GameRoom room = room("POND02", host);
        FishpondGameService service = service(room, host, playerTwo, playerThree);

        service.start("POND02", host);
        service.playerLeft(room, playerThree, null);

        assertTrue(service.canRejoin("POND02", playerThree.getUserId()));
        service.playerRejoined("POND02", playerThree);

        GameState state = service.getState("POND02", playerThree);
        assertFalse(state.players().stream()
                .filter(player -> player.userId().equals(playerThree.getUserId()))
                .findFirst()
                .orElseThrow()
                .left());
    }

    @Test
    void departedPlayerCannotRejoinAfterReadingStarts() {
        User host = user(21L, "host");
        User playerTwo = user(22L, "player-two");
        User playerThree = user(23L, "player-three");
        GameRoom room = room("POND03", host);
        FishpondGameService service = service(room, host, playerTwo, playerThree);

        service.start("POND03", host);
        service.submit("POND03", host, "A note");
        service.submit("POND03", playerTwo, "Another note");
        service.playerLeft(room, playerThree, null);

        assertFalse(service.canRejoin("POND03", playerThree.getUserId()));
        assertThrows(
                com.gamehub.game.exception.UnauthorizedException.class,
                () -> service.getState("POND03", playerThree)
        );
    }

    private GameRoom room(String roomCode, User host) {
        return GameRoom.create(
                roomCode,
                Game.create("Fishpond", "fishpond", "", 3, 12, GameType.PARTY, null),
                host,
                12
        );
    }

    private FishpondGameService service(GameRoom room, User... users) {
        List<RoomPlayer> roomPlayers = java.util.Arrays.stream(users)
                .map(user -> RoomPlayer.create(room, user))
                .toList();
        GameRoomRepository gameRoomRepository = mock(GameRoomRepository.class);
        RoomPlayerRepository roomPlayerRepository = mock(RoomPlayerRepository.class);
        when(gameRoomRepository.findByRoomCode(room.getRoomCode()))
                .thenReturn(Optional.of(room));
        when(roomPlayerRepository.findByRoom(room)).thenReturn(roomPlayers);
        when(roomPlayerRepository.existsByRoomAndUser(room, users[0])).thenReturn(true);
        return new FishpondGameService(
                gameRoomRepository,
                roomPlayerRepository,
                mock(SimpMessagingTemplate.class)
        );
    }

    private User user(Long id, String username) {
        User user = mock(User.class);
        when(user.getUserId()).thenReturn(id);
        when(user.getUsername()).thenReturn(username);
        when(user.getDisplayName()).thenReturn(username);
        when(user.getRole()).thenReturn(User.Role.USER);
        return user;
    }
}