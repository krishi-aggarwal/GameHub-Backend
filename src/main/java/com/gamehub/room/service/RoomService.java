package com.gamehub.room.service;

import com.gamehub.domain.game.Game;
import com.gamehub.domain.game.GameStatus;
import com.gamehub.domain.room.GameRoom;
import com.gamehub.domain.room.RoomPlayer;
import com.gamehub.domain.room.RoomStatus;
import com.gamehub.domain.user.User;
import com.gamehub.game.exception.GameExistsException;
import com.gamehub.game.exception.GameNotExistsException;
import com.gamehub.repository.GameRepository;
import com.gamehub.repository.GameRoomRepository;
import com.gamehub.repository.RoomPlayerRepository;
import com.gamehub.room.dto.CreateRoomRequest;
import com.gamehub.room.dto.RoomPlayerResponse;
import com.gamehub.room.dto.RoomResponse;
import com.gamehub.room.exception.*;
import jakarta.transaction.Transactional;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RoomService {
    private final GameRoomRepository gameRoomRepository;
    private final GameRepository gameRepository;
    private final RoomPlayerRepository roomPlayerRepository;

    public RoomService(
            GameRoomRepository gameRoomRepository,
            GameRepository gameRepository,
            RoomPlayerRepository roomPlayerRepository
    ) {
        this.gameRoomRepository = gameRoomRepository;
        this.gameRepository = gameRepository;
        this.roomPlayerRepository = roomPlayerRepository;
    }

    private String generateRoomCode() {

        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        while (true) {

            StringBuilder code = new StringBuilder();

            for (int i = 0; i < 6; i++) {
                int index = (int) (Math.random() * characters.length());
                code.append(characters.charAt(index));
            }

            String roomCode = code.toString();

            if (!gameRoomRepository.existsByRoomCode(roomCode)) {
                return roomCode;
            }
        }
    }

    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request){
        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(()->
                        new GameNotExistsException("Game not found!"));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User host = (User) authentication.getPrincipal();

        String roomCode = generateRoomCode();

        GameRoom gameRoom = GameRoom.create(roomCode,game,host,game.getMaxPlayers());

        GameRoom savedGameRoom = gameRoomRepository.save(gameRoom);

        RoomPlayer roomPlayer = RoomPlayer.create(savedGameRoom , host);
        RoomPlayer savedRoomPlayer = roomPlayerRepository.save(roomPlayer);

        RoomPlayerResponse roomPlayerResponse = new RoomPlayerResponse();

        roomPlayerResponse.setUsername(host.getUsername());
        roomPlayerResponse.setJoinedAt(savedRoomPlayer.getJoinedAt());

        RoomResponse response = new RoomResponse();

        response.setRoomId(savedGameRoom.getRoomId());
        response.setRoomCode(savedGameRoom.getRoomCode());
        response.setGameId(savedGameRoom.getGame().getGameId());
        response.setGameName(savedGameRoom.getGame().getName());
        response.setHostUsername(savedGameRoom.getHost().getUsername());
        response.setRoomStatus(savedGameRoom.getRoomStatus());
        response.setMaxPlayers(savedGameRoom.getMaxPlayers());
        response.setPlayerCount(1);
        response.setPlayers(toRoomPlayerResponseList(roomPlayerRepository.findByRoom(gameRoom)));

        return response;
    }

    public List<RoomPlayerResponse> toRoomPlayerResponseList (List<RoomPlayer> roomPlayers){
        List<RoomPlayerResponse> responses = new ArrayList<>();

        for(RoomPlayer r : roomPlayers){
            RoomPlayerResponse res = new RoomPlayerResponse();
            res.setUsername(r.getUser().getUsername());
            res.setJoinedAt(r.getJoinedAt());
            responses.add(res);
        }

        return responses;
    }

    @Transactional
    public RoomResponse joinRoom(String roomCode){
        GameRoom gameRoom = gameRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(()->new RoomNotExistsException("Room not Exists"));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(gameRoom.getRoomStatus() != RoomStatus.WAITING)
            throw new RoomNotWaitingException(("Cannot Join Room, Room may got started"));

        int currentPlayers = roomPlayerRepository.countByRoom_RoomCode(gameRoom.getRoomCode());

        if((gameRoom.getMaxPlayers() <= currentPlayers)){
            throw new RoomFullException("Room is Full!");
        }

        if(roomPlayerRepository.existsByRoomAndUser(gameRoom,user)){
            throw new PlayerAlreadyInRoomException("you have already joined this room");
        }

        RoomPlayer roomPlayer = RoomPlayer.create(
                gameRoom , user
        );
        RoomPlayer savedRoomPlayer = roomPlayerRepository.save(roomPlayer);

        RoomResponse response = new RoomResponse();
        response.setRoomId(gameRoom.getRoomId());
        response.setRoomCode(gameRoom.getRoomCode());
        response.setGameId(gameRoom.getGame().getGameId());
        response.setGameName(gameRoom.getGame().getName());
        response.setHostUsername(gameRoom.getHost().getUsername());
        response.setRoomStatus(gameRoom.getRoomStatus());
        response.setMaxPlayers(gameRoom.getMaxPlayers());
        response.setPlayerCount(currentPlayers+1);
        response.setPlayers(toRoomPlayerResponseList(roomPlayerRepository.findByRoom(gameRoom)));

        return response;
    }

    public void leaveRoom(String roomCode){
        GameRoom gameRoom = gameRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(()-> new RoomNotExistsException("Room not found"));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        RoomPlayer roomPlayer = roomPlayerRepository.findByRoomAndUser(gameRoom,user)
                .orElseThrow(()-> new PlayerNotInRoomException("you are not in the room"));

        roomPlayerRepository.delete(roomPlayer);





    }
}
