package com.srf.bluetoothchat.model;

public class TimeRange {
    private String firstTime;
    private String lastTime;

    public TimeRange(String firstTime, String lastTime) {
        this.firstTime = firstTime;
        this.lastTime = lastTime;
    }

    public String getFirstTime() {
        return firstTime;
    }

    public void setFirstTime(String firstTime) {
        this.firstTime = firstTime;
    }

    public String getLastTime() {
        return lastTime;
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }
}
