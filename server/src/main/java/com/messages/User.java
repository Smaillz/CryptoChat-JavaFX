package com.messages;

import java.io.Serializable;

public class User implements Serializable {

    private String name;
    private String picture;
    private Status status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return this.picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        User user = (User) o;

        return this.name.equals(user.name) && this.picture.equals(user.picture) && this.status == user.status;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + picture.hashCode();
        result = 31 * result + status.hashCode();
        return result;
    }
}
