package com.gamehub.room.dto;

public class RoomEvent {
    private String type;
    private RoomResponse room;

    public RoomEvent() {
    }

    public RoomEvent(String type, RoomResponse room) {
        this.type = type;
        this.room = room;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public RoomResponse getRoom() {
        return room;
    }

    public void setRoom(RoomResponse room) {
        this.room = room;
    }
}
