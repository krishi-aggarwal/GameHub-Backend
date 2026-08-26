package com.gamehub.repository;

import com.gamehub.domain.room.GameRoom;
import com.gamehub.domain.room.RoomPlayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomPlayerRepository extends JpaRepository<RoomPlayer,Long> {

}
