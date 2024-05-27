package com.srf.bluetoothchat.model;

public class AppConfig {
    private static final String BASE_URL = "http://nhatkydientu.vn:5000";

    private AppConfig(){

    }

    public static String getBaseUrl(){
        return BASE_URL;
    }
}
