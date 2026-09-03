package com.gamehub.repository;

import com.gamehub.domain.room.GameRoom;
import com.gamehub.domain.room.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GameRoomRepository extends JpaRepository<GameRoom,Long> {


    Optional<GameRoom> findByRoomCode(String roomCode);
    boolean existsByRoomCode(String roomCode);


    @Query("""
    SELECT room
    FROM GameRoom room
    LEFT JOIN room.players player
    WHERE room.roomStatus = :status
    GROUP BY room
    HAVING COUNT(player) < room.maxPlayers
""")

    List<GameRoom> findAvailableRooms(RoomStatus status);
}
