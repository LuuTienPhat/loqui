package com.example.loqui.data.model;

import java.io.Serializable;

public class Recipient implements Serializable {
    private Room room;
    //    private List<User> participants;
    private User user;
    private String nickname;

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Recipient))
            return false;

        Recipient mdc = (Recipient) obj;
        return mdc.room.getId().equals(room.getId());
//                && mdc.age.equals(age);
    }

    //    public List<User> getParticipants() {
//        return participants;
//    }
//
//    public void setParticipants(List<User> participants) {
//        this.participants = participants;
//    }
}
