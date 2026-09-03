package com.gamehub.game.deception.service;


import com.gamehub.domain.room.GameRoom;
import com.gamehub.domain.room.RoomPlayer;
import com.gamehub.domain.room.RoomStatus;
import com.gamehub.domain.user.User;
import com.gamehub.game.deception.domain.DeceptionGameConfig;
import com.gamehub.game.deception.domain.DeceptionRole;
import com.gamehub.game.deception.domain.GamePlayer;
import com.gamehub.game.deception.domain.GameSession;
import com.gamehub.game.deception.dto.StartGameRequest;
import com.gamehub.game.deception.dto.StartGameResponse;
import com.gamehub.game.exception.InsufficientPlayersException;
import com.gamehub.game.exception.InvalidPlayerCountException;
import com.gamehub.game.exception.UnauthorizedException;
import com.gamehub.repository.GameRoomRepository;
import com.gamehub.repository.RoomPlayerRepository;
import com.gamehub.room.exception.RoomNotExistsException;
import com.gamehub.room.exception.RoomNotWaitingException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DeceptionGameService {
    private final GameRoomRepository gameRoomRepository;
    private final RoomPlayerRepository roomPlayerRepository;
    private final RoleAssignmentService roleAssignmentService;
    private final GameSessionRegistry gameSessionRegistry;

    public DeceptionGameService(
            GameRoomRepository gameRoomRepository,
            RoomPlayerRepository roomPlayerRepository,
            RoleAssignmentService roleAssignmentService,
            GameSessionRegistry gameSessionRegistry
    ){
        this.gameRoomRepository=gameRoomRepository;
        this.roomPlayerRepository = roomPlayerRepository;
        this.roleAssignmentService=roleAssignmentService;
        this.gameSessionRegistry=gameSessionRegistry;
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

}
