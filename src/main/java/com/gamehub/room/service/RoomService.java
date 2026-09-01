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
import com.gamehub.room.dto.*;
import com.gamehub.room.exception.*;
import jakarta.transaction.Transactional;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;

    public RoomService(
            GameRoomRepository gameRoomRepository,
            GameRepository gameRepository,
            RoomPlayerRepository roomPlayerRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.gameRoomRepository = gameRoomRepository;
        this.gameRepository = gameRepository;
        this.roomPlayerRepository = roomPlayerRepository;
        this.messagingTemplate=messagingTemplate;
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

        RoomEvent event = new RoomEvent(
                "PLAYER_JOINED",
                response
        );

        messagingTemplate.convertAndSend(
                "/topic/rooms/"+roomCode,
                event
        );
        return response;
    }

    @Transactional
    public void leaveRoom(String roomCode){

        boolean roomDeleted = false;
        GameRoom gameRoom = gameRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(()-> new RoomNotExistsException("Room not found"));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        RoomPlayer roomPlayer = roomPlayerRepository.findByRoomAndUser(gameRoom,user)
                .orElseThrow(()-> new PlayerNotInRoomException("you are not in the room"));

        roomPlayerRepository.delete(roomPlayer);

        if(user.getUserId().equals(gameRoom.getHost().getUserId())){

            int remainingPlayers = roomPlayerRepository.countByRoom_RoomCode(roomCode);
            if(remainingPlayers > 0){
                RoomPlayer earliestRoomPlayer = roomPlayerRepository.findFirstByRoomOrderByJoinedAtAsc(gameRoom)
                        .orElseThrow(()-> new PlayerNotInRoomException("Something went wrong"));

                gameRoom.changeHost(earliestRoomPlayer.getUser());
            }
            else{
                gameRoomRepository.delete(gameRoom);
                roomDeleted = true;
            }
        }

        if(!roomDeleted){
            RoomResponse roomResponse = new RoomResponse();

            roomResponse.setGameId(gameRoom.getGame().getGameId());
            roomResponse.setRoomCode(gameRoom.getRoomCode());
            roomResponse.setRoomStatus(gameRoom.getRoomStatus());
            roomResponse.setGameName(gameRoom.getGame().getName());
            roomResponse.setMaxPlayers(gameRoom.getMaxPlayers());
            roomResponse.setHostUsername(gameRoom.getHost().getUsername());
            roomResponse.setRoomId(gameRoom.getRoomId());
            roomResponse.setPlayerCount(roomPlayerRepository.countByRoom_RoomCode(gameRoom.getRoomCode()));
            roomResponse.setPlayers(toRoomPlayerResponseList(roomPlayerRepository.findByRoom(gameRoom)));

            RoomEvent event = new RoomEvent(
                    "PLAYER_LEFT",
                    roomResponse
            );

            messagingTemplate.convertAndSend(
                    "/topic/rooms/"+roomCode,
                    event
            );
        }
        else{
            RoomEvent event = new RoomEvent(
                    "ROOM_CLOSED",
                    null
            );

            messagingTemplate.convertAndSend(
                    "/topic/rooms/"+roomCode,
                    event
            );
        }

    }

    public List<RoomSummaryResponse> getAllRooms() {

        List<RoomSummaryResponse> responses = new ArrayList<>();

        List<GameRoom> gameRoomList =
                gameRoomRepository.findAvailableRooms(RoomStatus.WAITING);

        for (GameRoom gameRoom : gameRoomList) {

            RoomSummaryResponse response = new RoomSummaryResponse();

            response.setGameId(gameRoom.getGame().getGameId());
            response.setRoomCode(gameRoom.getRoomCode());
            response.setRoomId(gameRoom.getRoomId());
            response.setRoomStatus(gameRoom.getRoomStatus());
            response.setGameName(gameRoom.getGame().getName());
            response.setHostUsername(gameRoom.getHost().getUsername());
            response.setMaxPlayers(gameRoom.getMaxPlayers());
            response.setPlayerCount(roomPlayerRepository.countByRoom_RoomCode(gameRoom.getRoomCode()));

            responses.add(response);
        }

        return responses;
    }

    public RoomResponse getRoom(String roomCode){
        GameRoom gameRoom = gameRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(()-> new RoomNotExistsException("Room not Found!"));

        RoomResponse roomResponse = new RoomResponse();
        roomResponse.setRoomId(gameRoom.getRoomId());
        roomResponse.setGameId(gameRoom.getGame().getGameId());
        roomResponse.setRoomStatus(gameRoom.getRoomStatus());
        roomResponse.setPlayerCount(roomPlayerRepository.countByRoom_RoomCode(roomCode));
        roomResponse.setRoomCode(gameRoom.getRoomCode());
        roomResponse.setHostUsername(gameRoom.getHost().getUsername());
        roomResponse.setMaxPlayers(gameRoom.getMaxPlayers());
        roomResponse.setGameName(gameRoom.getGame().getName());
        roomResponse.setPlayers(toRoomPlayerResponseList(roomPlayerRepository.findByRoom(gameRoom)));

        return roomResponse;
    }

}
