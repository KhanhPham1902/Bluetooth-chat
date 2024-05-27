package com.srf.bluetoothchat.model;

public class LoginResponse {
    private String message;
    private int user_id;

    public LoginResponse(String message, int user_id) {
        this.message = message;
        this.user_id = user_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}
