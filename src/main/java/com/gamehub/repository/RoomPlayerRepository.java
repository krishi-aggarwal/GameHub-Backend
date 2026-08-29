package com.gamehub.repository;

import com.gamehub.domain.room.GameRoom;
import com.gamehub.domain.room.RoomPlayer;
import com.gamehub.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomPlayerRepository extends JpaRepository<RoomPlayer,Long> {

    int countByRoom_RoomCode(String roomCode);
    List<RoomPlayer> findByRoom(GameRoom gameRoom);
    boolean existsByRoomAndUser(GameRoom gameRoom , User user);
}

