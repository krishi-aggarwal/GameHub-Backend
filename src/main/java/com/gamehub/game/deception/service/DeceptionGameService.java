package com.gamehub.game.deception.service;


import com.gamehub.domain.room.GameRoom;
import com.gamehub.domain.room.RoomPlayer;
import com.gamehub.domain.room.RoomStatus;
import com.gamehub.domain.user.User;
import com.gamehub.game.deception.domain.*;
import com.gamehub.game.deception.dto.*;
import com.gamehub.game.exception.GameSessionNotFoundException;
import com.gamehub.game.exception.InsufficientPlayersException;
import com.gamehub.game.exception.InvalidPlayerCountException;
import com.gamehub.game.exception.UnauthorizedException;
import com.gamehub.repository.GameRoomRepository;
import com.gamehub.repository.RoomPlayerRepository;
import com.gamehub.repository.UserRepository;
import com.gamehub.room.exception.RoomNotExistsException;
import com.gamehub.room.exception.RoomNotWaitingException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DeceptionGameService {
    private final GameRoomRepository gameRoomRepository;
    private final RoomPlayerRepository roomPlayerRepository;
    private final RoleAssignmentService roleAssignmentService;
    private final GameSessionRegistry gameSessionRegistry;
    private final UserRepository userRepository;
    private final GameEventService gameEventService;

    public DeceptionGameService(
            GameRoomRepository gameRoomRepository,
            RoomPlayerRepository roomPlayerRepository,
            RoleAssignmentService roleAssignmentService,
            GameSessionRegistry gameSessionRegistry,
            UserRepository userRepository,
            GameEventService gameEventService
    ){
        this.gameRoomRepository=gameRoomRepository;
        this.roomPlayerRepository = roomPlayerRepository;
        this.roleAssignmentService=roleAssignmentService;
        this.gameSessionRegistry=gameSessionRegistry;
        this.userRepository = userRepository;
        this.gameEventService = gameEventService;
    }


    public List<GamePlayer> toGamePlayerList(
            List<RoomPlayer> roomPlayerList
    ) {

        List<GamePlayer> gamePlayerList = new ArrayList<>();

        for (RoomPlayer roomPlayer : roomPlayerList) {

            User user = roomPlayer.getUser();

            GamePlayer gamePlayer = new GamePlayer(
                    user.getUserId(),
                    user.getUsername(),
                    user.getDisplayName(),
                    user.getAvatarUrl(),
                    null,
                    true
            );

            gamePlayerList.add(gamePlayer);
        }

        return gamePlayerList;
    }

    public StartGameResponse startGame(String roomCode,
                                       User user,
                                       StartGameRequest request
                          ){

        //find room
        GameRoom gameRoom = gameRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(()-> new RoomNotExistsException("Room not Found!"));

        //check host
        if(!gameRoom.getHost().getUserId().equals(user.getUserId())){
            throw new UnauthorizedException("Unauthorized Access");
        }

        //check room-status as waiting
        if(gameRoom.getRoomStatus()!= RoomStatus.WAITING){
            throw new RoomNotWaitingException("Room may got started!");
        }



        //check min players to start the game
        int playerCount = roomPlayerRepository.countByRoom_RoomCode(gameRoom.getRoomCode());
        if((playerCount < 4)){
            throw new InsufficientPlayersException("Need Minimum 4 Players to Start the Game!");
        }

        if((playerCount > 15)){
            throw new InvalidPlayerCountException("Maximum Players Limit is 15");
        }

        DeceptionGameConfig deceptionGameConfig = new DeceptionGameConfig(request.getSelectedRoles());
        //create game session
        GameSession gameSession = new GameSession(gameRoom,deceptionGameConfig);

        //convert roomplayers to gameplayers
        List<GamePlayer> gamePlayerList = toGamePlayerList(roomPlayerRepository.findByRoom(gameRoom));

        for(GamePlayer gamePlayer : gamePlayerList){
            gameSession.addPlayer(gamePlayer);
        }


        List<DeceptionRole> deceptionRoleList = roleAssignmentService.calculateRoles(gamePlayerList,deceptionGameConfig);


        roleAssignmentService.allocateRoles(deceptionRoleList,gamePlayerList);
        gameSessionRegistry.storeSession(gameSession);

        gameRoom.setRoomStatus(RoomStatus.IN_PROGRESS);
        gameRoomRepository.save(gameRoom);

// Notify all players that the game has started.
        gameEventService.publish(
                gameSession,
                GameEventType.GAME_STARTED,
                null
        );

// The game starts in NIGHT phase.
        gameEventService.publish(
                gameSession,
                GameEventType.NIGHT_STARTED,
                gameSession.getRoundNumber()
        );

        return new StartGameResponse(
                gameSession.getSessionId()
        );
    }




    @Transactional(readOnly = true)
    public GameStateResponse getGameState(
            UUID sessionId,
            User user
    ) {

        GameSession gameSession =
                gameSessionRegistry.retrieveSession(
                        sessionId
                );

        if (gameSession == null) {

            throw new GameSessionNotFoundException(
                    "Game Session not found"
            );
        }

        if (user == null ||
                user.getUserId() == null) {

            throw new UnauthorizedException(
                    "Authenticated user not found"
            );
        }

        /*
         * Find current player using userId.
         */
        GamePlayer currentPlayer =
                gameSession
                        .getPlayers()
                        .get(user.getUserId());

        if (currentPlayer == null) {

            throw new UnauthorizedException(
                    "You are not a player in this game"
            );
        }

        /*
         * Current player must already have
         * a role because roles are assigned
         * before the session is stored.
         */
        if (currentPlayer.getRole() == null) {

            throw new RuntimeException(
                    "Player role has not been assigned"
            );
        }

        /*
         * Build player list.
         */
        List<GamePlayerResponse> players =
                new ArrayList<>();

        for (GamePlayer player :
                gameSession
                        .getPlayers()
                        .values()) {

            if (player == null) {
                continue;
            }

            players.add(
                    new GamePlayerResponse(
                            player.getUserId(),
                            player.getDisplayName(),
                            player.getAvatarUrl(),
                            player.isAlive()
                    )
            );
        }

        /*
         * Host information comes from the
         * runtime GameSession snapshot.
         */
        Long hostUserId =
                gameSession.getHostUserId();

        String hostUsername =
                gameSession.getHostUsername();

        if (hostUserId == null) {

            throw new RuntimeException(
                    "Game session host not found"
            );
        }

        boolean isHost =
                hostUserId.equals(
                        user.getUserId()
                );

        return new GameStateResponse(
                gameSession.getSessionId(),
                gameSession.getGamePhase(),
                gameSession.getRoundNumber(),
                currentPlayer.getRole(),
                players,
                user.getUsername(),
                isHost,
                hostUserId,
                hostUsername
        );
    }









    public GameResultResponse getGameResult(
            UUID sessionId
    ) {

        GameSession gameSession =
                gameSessionRegistry.retrieveSession(sessionId);

        if (gameSession == null) {
            throw new GameSessionNotFoundException(
                    "Game Session not found"
            );
        }

        if (gameSession.getGamePhase()
                != GamePhase.GAME_OVER) {

            throw new RuntimeException(
                    "Game is not over yet"
            );
        }

        return new GameResultResponse(
                sessionId,
                gameSession.getGameResult()
        );
    }

    public DayStateResponse getDayState(UUID sessionId) {

        GameSession gameSession =
                gameSessionRegistry.retrieveSession(sessionId);

        if (gameSession == null) {
            throw new GameSessionNotFoundException(
                    "Game Session not found"
            );
        }

        if (gameSession.getGamePhase()
                != GamePhase.DAY) {

            throw new RuntimeException(
                    "Game is not currently in DAY phase"
            );
        }

        return new DayStateResponse(
                gameSession.getSessionId(),
                gameSession.getGamePhase(),
                gameSession.getRoundNumber(),
                gameSession.getEliminatedUserId()
        );
    }

    public InvestigationResponse getInvestigationResult(
            UUID sessionId,
            User user
    ) {

        GameSession gameSession =
                gameSessionRegistry.retrieveSession(sessionId);

        if (gameSession == null) {
            throw new GameSessionNotFoundException(
                    "Game Session not found"
            );
        }

        GamePlayer player =
                gameSession.getPlayers()
                        .get(user.getUserId());

        if (player == null) {
            throw new UnauthorizedException(
                    "You are not a player in this game"
            );
        }

        if (player.getRole()
                != DeceptionRole.DETECTIVE) {

            throw new UnauthorizedException(
                    "Only the Detective can view investigation results"
            );
        }

        if (gameSession.getInvestigatedUserId() == null) {
            throw new RuntimeException(
                    "No investigation result available"
            );
        }

        return new InvestigationResponse(
                sessionId,
                gameSession.getInvestigatedUserId(),
                gameSession.getInvestigationResult()
        );
    }

}
