package com.srf.bluetoothchat.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.srf.bluetoothchat.R;
import com.srf.bluetoothchat.activity.MainActivity;
import com.srf.bluetoothchat.api.ChatApi;
import com.srf.bluetoothchat.model.LoginInfo;
import com.srf.bluetoothchat.model.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText edtUsername, edtPassword;
    private Button btnLogin;
    private TextView txtForgotPass;
    private ImageButton imgHidePasss;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;
    int count=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtForgotPass = findViewById(R.id.txtForgotPass);
        imgHidePasss = findViewById(R.id.imgHidePass);

        checkBluetoothPermissions();

        // Nhan nut dang nhap
        btnLogin.setOnClickListener(v -> {
            String username = edtUsername.getText().toString();
            String password = edtPassword.getText().toString();
            loginAccount(username, password);
        });

        // Hien thi/ an mat khau
        imgHidePasss.setOnClickListener(v -> {
            count++;
            String pass = edtPassword.getText().toString();
            if(count%2 == 1){ // Hien thi mat khau
                if(!pass.isEmpty()){
                    edtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    imgHidePasss.setImageResource(R.drawable.visibility_off);
                }
            }else{ // An mat khau
                if(!pass.isEmpty()){
                    edtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD  | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    imgHidePasss.setImageResource(R.drawable.visibility);
                }
            }
        });

        // An ban phim ao khi nhan vi tri bat ki tren man hinh
        edtPassword.setOnTouchListener((v, event) -> {
            hideKeyboard();
            return false;
        });
    }

    // Luu ten nguoi dung va mat khau
    private void saveInfo(String username, String password){
        SharedPreferences sharedPreferences = getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("USERNAME", username);
        editor.putString("PASSWORD", password);
        editor.apply();
    }

    // Kiem tra tu dong dang nhap tai khoan
    private void autoLogin(){
        SharedPreferences sharedPreferences = getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("USERNAME","");
        String password = sharedPreferences.getString("PASSWORD","");
        if(!username.isEmpty() && !password.isEmpty()){
            loginAccount(username, password);
        }
    }

    // Dang nhap tai khoan
    private void loginAccount(String username, String password) {
        LoginInfo requestBody = new LoginInfo(username,password);

        Call<LoginResponse> call = ChatApi.RetrofitClient.instance.login(requestBody);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if(response.isSuccessful()){
                    SharedPreferences sharedPreferences = getSharedPreferences("LOGIN_DATA", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username",username);
                    editor.putString("password",password);
                    editor.apply();

                    saveInfo(username,password);
                    assert response.body() != null;
                    int userId = response.body().getUser_id();
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("USERNAME", username);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(LoginActivity.this, "Tên tài khoản hoặc mật khẩu không chính xác", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable throwable) {
                Toast.makeText(LoginActivity.this, "Có lỗi trong quá trình xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                Log.e("LoginActivity", "Failed to connect to server");
            }
        });
    }

    // kiem tra quyen su dung bluetooth
//    private void checkBluetoothPermission() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_BLUETOOTH_CONNECT);
//        }
//    }

    private void checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.BLUETOOTH_CONNECT
                }, REQUEST_BLUETOOTH_PERMISSIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, R.string.bluetooth_permission, Toast.LENGTH_SHORT).show();
            }
        }
    }

    //An ban phim ao
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }
}