package com.gamehub.room.service;

import com.gamehub.domain.game.Game;
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
import com.gamehub.room.dto.RoomResponse;
import jakarta.transaction.Transactional;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
        roomPlayerRepository.save(roomPlayer);
        RoomResponse response = new RoomResponse();

        response.setRoomId(savedGameRoom.getRoomId());
        response.setRoomCode(savedGameRoom.getRoomCode());
        response.setGameId(savedGameRoom.getGame().getGameId());
        response.setGameName(savedGameRoom.getGame().getName());
        response.setHostUsername(savedGameRoom.getHost().getUsername());
        response.setRoomStatus(savedGameRoom.getRoomStatus());
        response.setMaxPlayers(savedGameRoom.getMaxPlayers());
        response.setPlayerCount(1);

        return response;
    }
}
