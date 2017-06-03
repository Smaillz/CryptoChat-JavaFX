package com.entity;

import com.messages.User;

import java.io.ObjectOutputStream;
import java.util.HashMap;

public class Room {
    private String roomName;
    private String roomKey;
    private HashMap<User, ObjectOutputStream> usersInRoom  = new HashMap<>();

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomKey() {
        return roomKey;
    }

    public void setRoomKey(String roomKey) {
        this.roomKey = roomKey;
    }

    public HashMap<User, ObjectOutputStream> getUsersInRoom() {
        return usersInRoom;
    }

}
