//package com.gamehub.game.deception.service;
//
//import com.gamehub.game.deception.domain.GameEventType;
//import com.gamehub.game.deception.domain.GameSession;
//import com.gamehub.game.deception.dto.GameEvent;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//public class GameEventService {
//
//    private final SimpMessagingTemplate messagingTemplate;
//
//    public GameEventService(
//            SimpMessagingTemplate messagingTemplate
//    ) {
//        this.messagingTemplate = messagingTemplate;
//    }
//
//    /**
//     * Sends an event to everyone watching this game session.
//     */
//    public void publish(
//            GameSession gameSession,
//            GameEventType type,
//            Object data
//    ) {
//
//        GameEvent event =
//                new GameEvent(
//                        type,
//                        gameSession.getSessionId(),
//                        data
//                );
//
//        messagingTemplate.convertAndSend(
//                "/topic/game/" +
//                        gameSession.getSessionId(),
//                event
//        );
//    }
//}


        package com.gamehub.game.deception.service;

import com.gamehub.game.deception.domain.GameEventType;
import com.gamehub.game.deception.domain.GameSession;
import com.gamehub.game.deception.dto.GameEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class GameEventService {

    private final SimpMessagingTemplate messagingTemplate;

    public GameEventService(
            SimpMessagingTemplate messagingTemplate
    ) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Sends an event to everyone watching
     * this game session.
     */
    public void publish(
            GameSession gameSession,
            GameEventType type,
            Object data
    ) {

        GameEvent event =
                new GameEvent(
                        type,
                        gameSession.getSessionId(),
                        data
                );

        /*
         * Game events.
         *
         * Used by players who are already
         * inside the game.
         */
        messagingTemplate.convertAndSend(
                "/topic/game/" +
                        gameSession
                                .getSessionId(),
                event
        );

        /*
         * GAME_STARTED must also be sent
         * to the room lobby.
         *
         * Players are subscribed to the
         * room topic while they are still
         * in the lobby.
         */
        if (type ==
                GameEventType.GAME_STARTED) {

            messagingTemplate.convertAndSend(
                    "/topic/room/" +
                            gameSession
                                    .getGameRoom()
                                    .getRoomCode(),
                    event
            );
        }
    }
}

