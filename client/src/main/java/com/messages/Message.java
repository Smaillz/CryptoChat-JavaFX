package com.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class Message implements Serializable {

    private String name;
    private MessageType type;
    private String msg;
    private int count;
    private ArrayList<User> list;
    private ArrayList<User> users;
    private Status status;
    private byte[] voiceMsg;
    private String picture;
    private String room;
    private String key;

    public Message() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getOnlineCount(){
        return this.count;
    }

    public void setOnlineCount(int count){
        this.count = count;
    }

    public ArrayList<User> getUserlist() {
        return list;
    }

    public void setUserlist(Collection<User> collection) {
        this.list = new ArrayList<>(collection);
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public byte[] getVoiceMsg() {
        return voiceMsg;
    }

    public void setVoiceMsg(byte[] voiceMsg) {
        this.voiceMsg = voiceMsg;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
