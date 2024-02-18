package com.app.voicechat;

import java.io.Serializable;

public class User implements Serializable {

    private String username;
    private String email;
    private String userId;


    public User(){

    }

    public User(String username, String email, String userId) {
        this.username = username;
        this.email = email;
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getUserId() {
        return userId;
    }
}
