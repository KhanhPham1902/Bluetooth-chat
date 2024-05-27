package com.srf.bluetoothchat.model;

public class MessageData {
    private String data;
    private String phone;
    private String time;

    public MessageData(String data, String phone, String time) {
        this.data = data;
        this.phone = phone;
        this.time = time;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
