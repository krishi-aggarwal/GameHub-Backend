package com.gamehub.room.service;

import com.gamehub.domain.game.Game;
import com.gamehub.domain.room.GameRoom;
import com.gamehub.game.exception.GameExistsException;
import com.gamehub.repository.GameRepository;
import com.gamehub.repository.GameRoomRepository;
import com.gamehub.room.dto.CreateRoomRequest;
import org.springframework.stereotype.Service;

@Service
public class RoomService {
    private final GameRoomRepository gameRoomRepository;
    private final GameRepository gameRepository;

    public RoomService(
            GameRoomRepository gameRoomRepository,
            GameRepository gameRepository
    ) {
        this.gameRoomRepository = gameRoomRepository;
        this.gameRepository = gameRepository;
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

    public GameRoom createRoom(CreateRoomRequest request){
        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(()->
                        new GameExistsException("Game not found!"));


    }
}
