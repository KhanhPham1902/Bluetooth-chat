package com.srf.bluetoothchat.api;


import com.srf.bluetoothchat.model.AppConfig;
import com.srf.bluetoothchat.model.LoginInfo;
import com.srf.bluetoothchat.model.LoginResponse;
import com.srf.bluetoothchat.model.MessageData;
import com.srf.bluetoothchat.model.TimeRange;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ChatApi {

    @POST("send-message")
    Call<Void> sendMessage(
            @Header("username") String username,
            @Header("password") String password,
            @Body MessageData messageData
    );

    @POST("user/messages")
    Call<ResponseBody> getUserMessages(
            @Header("username") String username,
            @Header("password") String password,
            @Body TimeRange timeRange
    );

    @POST("login")
    Call<LoginResponse> login(
            @Body LoginInfo loginInfo
    );

    public class RetrofitClient{
        private static final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConfig.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        public static final ChatApi instance = retrofit.create(ChatApi.class);
    }
}
