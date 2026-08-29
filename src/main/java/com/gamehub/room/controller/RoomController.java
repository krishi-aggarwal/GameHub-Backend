package com.gamehub.room.controller;

import com.gamehub.mainDto.ApiResponse;
import com.gamehub.room.dto.CreateRoomRequest;
import com.gamehub.room.dto.RoomResponse;
import com.gamehub.room.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService){
        this.roomService=roomService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(
           @Valid @RequestBody CreateRoomRequest request
    ){
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Room created Successfully!",
                        roomService.createRoom(request)
                )
        );
    }

    @PostMapping("/{roomCode}/join")
    public ResponseEntity<ApiResponse<RoomResponse>> joinRoom(
            @PathVariable String roomCode
    ){
        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Joined Room!",
                        roomService.joinRoom(roomCode)
                )
        );
    }

    @DeleteMapping("/{roomCode}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveRoom(
        @PathVariable String roomCode
    ){
        roomService.leaveRoom(roomCode);
        return ResponseEntity.ok(
                new ApiResponse<>("Leaved Room!",null)
        );
    }

}
