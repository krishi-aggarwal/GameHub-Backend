package com.gamehub.game.deception.service;

import com.gamehub.domain.game.Game;
import com.gamehub.domain.game.GameType;
import com.gamehub.domain.room.GameRoom;
import com.gamehub.domain.user.User;
import com.gamehub.game.deception.domain.DeceptionGameConfig;
import com.gamehub.game.deception.domain.DeceptionRole;
import com.gamehub.game.deception.domain.GamePlayer;
import com.gamehub.game.deception.domain.GameResult;
import com.gamehub.game.deception.domain.GameSession;
import com.gamehub.repository.GameRoomRepository;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class WinConditionServiceTest {

    @Test
    void innocentsWinWhenTheLastPredatorLeaves() {
        User host = new User("host", "host@example.test", "hash", "Host", User.Role.USER);
        GameRoom room = GameRoom.create(
                "ROOM01",
                Game.create("Deception", "deception", "", 4, 15, GameType.PARTY, null),
                host,
                15
        );
        GameSession session = new GameSession(room, new DeceptionGameConfig(Set.of()));
        GamePlayer predator = player(1L, DeceptionRole.PREDATOR);
        session.addPlayer(predator);
        session.addPlayer(player(2L, DeceptionRole.INNOCENT));
        session.addPlayer(player(3L, DeceptionRole.INNOCENT));

        session.markPlayerLeft(predator.getUserId());

        boolean finished = new WinConditionService(mock(GameRoomRepository.class))
                .checkWinCondition(session);

        assertTrue(finished);
        assertEquals(GameResult.INNOCENTS_WIN, session.getGameResult());
        assertFalse(predator.isAlive());
        assertTrue(predator.isLeft());
    }

    private GamePlayer player(Long userId, DeceptionRole role) {
        return new GamePlayer(userId, "player" + userId, "Player " + userId, null, role, true);
    }
}