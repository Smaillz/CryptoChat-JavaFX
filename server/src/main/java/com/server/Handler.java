package com.server;

import com.entity.Room;
import com.exception.DuplicateUsernameException;
import com.messages.Message;
import com.messages.MessageType;
import com.messages.Status;
import com.messages.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

public class Handler implements Runnable {
    private String name;
    private String roomName;
    private Socket socket;
    private Logger logger = LoggerFactory.getLogger(Handler.class);
    private User user;

    Handler(Socket socket) throws IOException {
        this.socket = socket;
    }

    public void run() {
        logger.info("Thread is running");
        try (
                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream())
        ) {
            Message inputObject = (Message) input.readObject();
            initConnectionUser(inputObject, output);
            sendConnected();

            while (socket.isConnected()) {
                inputObject = (Message) input.readObject();
                if (inputObject != null) {
                    switch (inputObject.getType()) {
                        case USER:
                            write(inputObject,true);
                            break;
                        case VOICE:
                            write(inputObject,true);
                            break;
                        case STATUS:
                            changeStatus(inputObject);
                            break;
                    }
                }
            }
        } catch (IOException | DuplicateUsernameException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            closeConnections();
            try {
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void initConnectionUser(Message msg, ObjectOutputStream output) throws DuplicateUsernameException {
        logger.info(msg.getName() + " is trying to connect");
        User newUser = new User();
        newUser.setName(msg.getName());
        newUser.setStatus(Status.ONLINE);
        newUser.setPicture(msg.getPicture());

        if (!Server.rooms.containsKey(msg.getRoom())) {
            this.name = msg.getName();
            this.roomName = msg.getRoom();
            this.user = newUser;
            Room newRoom = new Room();
            newRoom.setRoomName(this.roomName);
            newRoom.setRoomKey(msg.getKey());
            newRoom.getUsersInRoom().put(this.user, output);
            Server.rooms.put(this.roomName, newRoom);
        } else {
            HashMap<User, ObjectOutputStream> usersList = Server.rooms.get(msg.getRoom()).getUsersInRoom();
            if (!usersList.keySet().contains(newUser)) {
                this.name = msg.getName();
                this.roomName = msg.getRoom();
                this.user = newUser;
                usersList.put(this.user, output);
            } else {
                throw new DuplicateUsernameException(this.name + " is already connected");
            }
        }
    }

    private void sendConnected() throws IOException {
        Message msg = new Message();
        msg.setName(this.user.getName());
        msg.setPicture(this.user.getPicture());
        msg.setType(MessageType.CONNECTED);
        write(msg,false);
    }

    private void changeStatus(Message inputmsg) throws IOException {
        logger.debug(inputmsg.getName() + " has changed status to  " + inputmsg.getStatus());
        ObjectOutputStream temp = Server.rooms.get(this.roomName).getUsersInRoom().get(this.user);
        Server.rooms.get(this.roomName).getUsersInRoom().remove(this.user);
        Message msg = new Message();
        msg.setName(this.name);
        msg.setType(MessageType.STATUS);
        this.user.setStatus(inputmsg.getStatus());
        Server.rooms.get(this.roomName).getUsersInRoom().put(this.user,temp);
        write(msg,false);
    }

    private void removeFromList() throws IOException {
        Message msg = new Message();
        msg.setType(MessageType.DISCONNECTED);
        msg.setUserlist(Server.rooms.get(this.roomName).getUsersInRoom().keySet());
        write(msg,false);
    }

    private void write(Message msg, boolean flag) throws IOException {
        HashMap<User, ObjectOutputStream> usersList = Server.rooms.get(this.roomName).getUsersInRoom();
        for (ObjectOutputStream writer : usersList.values()) {
            if(flag && usersList.get(this.user) == writer){
                continue;
            }else{
                msg.setUserlist(usersList.keySet());
                msg.setUsers(msg.getUserlist());
                msg.setOnlineCount(usersList.size());
                try {
                    writer.writeObject(msg);
                    writer.reset();
                } catch (Exception ex) {
                    closeConnections();
                }
            }
        }
    }

    private synchronized void closeConnections() {
        logger.info("User " + this.name + "has been disconnected!");
        Room room = Server.rooms.get(this.roomName);
        try {
            room.getUsersInRoom().remove(this.user);
            if (room.getUsersInRoom().size() == 0) {
                Server.rooms.remove(this.roomName);
            } else {
                removeFromList();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}