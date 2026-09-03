package com.gamehub.game.deception.service;


import com.gamehub.domain.room.GameRoom;
import com.gamehub.domain.room.RoomPlayer;
import com.gamehub.domain.room.RoomStatus;
import com.gamehub.domain.user.User;
import com.gamehub.game.deception.domain.GamePlayer;
import com.gamehub.game.deception.domain.GameSession;
import com.gamehub.game.exception.InsufficientPlayersException;
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

    public DeceptionGameService(
            GameRoomRepository gameRoomRepository,
            RoomPlayerRepository roomPlayerRepository
    ){
        this.gameRoomRepository=gameRoomRepository;
        this.roomPlayerRepository = roomPlayerRepository;
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

    public void startGame(String roomCode , User user){

        //check room
        GameRoom gameRoom = gameRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(()-> new RoomNotExistsException("Room not Found!"));

        //check host
        if(!gameRoom.getHost().getUserId().equals(user.getUserId())){
            throw new UnauthorizedException("Unauthorized Access");
        }

        //check room-status
        if(gameRoom.getRoomStatus()!= RoomStatus.WAITING){
            throw new RoomNotWaitingException("Room may got started!");
        }

        //check min players to start the game
        if((roomPlayerRepository.countByRoom_RoomCode(gameRoom.getRoomCode()) < 5)){
            throw new InsufficientPlayersException("Need Minimum 5 Players to Start the Game!");
        }

        GameSession gameSession = new GameSession(gameRoom);

        for(GamePlayer gamePlayer : toGamePlayerList(roomPlayerRepository.findByRoom(gameRoom))){
            gameSession.addPlayer(gamePlayer);
        }




    }

}
