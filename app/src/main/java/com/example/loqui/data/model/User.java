package com.example.loqui.data.model;

import java.io.Serializable;

public class User implements Serializable {
    private String id, firstname, lastname, email, token, image;

    public User() {
    }

    public User(String id, String firstname, String lastname, String email, String token, String image) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.token = token;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(getFirstname());
        sb.append(" ");
        sb.append(getLastname());
        return sb.toString();
    }
}
