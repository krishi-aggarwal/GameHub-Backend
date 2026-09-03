package com.gamehub.game.deception.service;


import com.gamehub.domain.room.GameRoom;
import com.gamehub.domain.room.RoomPlayer;
import com.gamehub.domain.room.RoomStatus;
import com.gamehub.domain.user.User;
import com.gamehub.game.deception.domain.DeceptionGameConfig;
import com.gamehub.game.deception.domain.DeceptionRole;
import com.gamehub.game.deception.domain.GamePlayer;
import com.gamehub.game.deception.domain.GameSession;
import com.gamehub.game.deception.dto.GamePlayerResponse;
import com.gamehub.game.deception.dto.GameStateResponse;
import com.gamehub.game.deception.dto.StartGameRequest;
import com.gamehub.game.deception.dto.StartGameResponse;
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

    public DeceptionGameService(
            GameRoomRepository gameRoomRepository,
            RoomPlayerRepository roomPlayerRepository,
            RoleAssignmentService roleAssignmentService,
            GameSessionRegistry gameSessionRegistry,
            UserRepository userRepository
    ){
        this.gameRoomRepository=gameRoomRepository;
        this.roomPlayerRepository = roomPlayerRepository;
        this.roleAssignmentService=roleAssignmentService;
        this.gameSessionRegistry=gameSessionRegistry;
        this.userRepository = userRepository;
    }


    public List<GamePlayer> toGamePlayerList (List<RoomPlayer> roomPlayerList){
        List<GamePlayer> gamePlayerList = new ArrayList<>();
        for(RoomPlayer roomPlayer : roomPlayerList){
            GamePlayer gamePlayer = new GamePlayer(
                    roomPlayer.getUser(),
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
        return new StartGameResponse(gameSession.getSessionId());
    }

    public GameStateResponse getGameState(UUID sessionId, User user) {

        // 1. Find the active game session from our in-memory registry.
        GameSession gameSession = gameSessionRegistry.retrieveSession(sessionId);

        if (gameSession == null) {
            throw new GameSessionNotFoundException(
                    "Game Session not found"
            );
        }

        // 2. Find the requesting player inside this game session.
        GamePlayer currentPlayer = gameSession.getPlayers().get(user.getUserId());

        if (currentPlayer == null) {
            throw new UnauthorizedException(
                    "You are not a player in this game"
            );
        }

        // 3. Build PUBLIC player information.
        // We deliberately don't include roles here.
        List<GamePlayerResponse> players = new ArrayList<>();

        for (GamePlayer player : gameSession.getPlayers().values()) {

            User playerUser = userRepository.findById(player.getUser().getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

// Build only PUBLIC player information.
// Role is intentionally NOT included.
            players.add(
                    new GamePlayerResponse(
                            playerUser.getUserId(),
                            playerUser.getDisplayName(),
                            player.isAlive()
                    )
            );
        }

        // 4. Return the complete state.
        // yourRole contains ONLY the requesting player's role.
        return new GameStateResponse(
                gameSession.getSessionId(),
                gameSession.getGamePhase(),
                gameSession.getRoundNumber(),
                currentPlayer.getRole(),
                players
        );
    }

}
