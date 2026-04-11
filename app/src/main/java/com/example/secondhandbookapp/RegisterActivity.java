package com.example.secondhandbookapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    EditText etRegUsername, etRegPassword;
    Button btnRegisterSubmit;

    // TODO: 這裡的 IP 必須跟你剛才 MainActivity 用的一模一樣！
    private static final String API_URL = "http://10.0.2.2:3000/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegUsername = findViewById(R.id.etRegUsername);
        etRegPassword = findViewById(R.id.etRegPassword);
        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);

        btnRegisterSubmit.setOnClickListener(v -> {
            String inputUser = etRegUsername.getText().toString().trim();
            String inputPass = etRegPassword.getText().toString().trim();

            if(inputUser.isEmpty() || inputPass.isEmpty()){
                Toast.makeText(this, "帳號與密碼不能為空", Toast.LENGTH_SHORT).show();
                return;
            }
            registerViaAPI(inputUser, inputPass);
        });
    }

    private void registerViaAPI(String username, String password) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                String jsonInputString = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";

                try(OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int code = conn.getResponseCode();

                runOnUiThread(() -> {
                    if (code == 200) {
                        Toast.makeText(RegisterActivity.this, "註冊成功！請登入", Toast.LENGTH_SHORT).show();
                        finish(); // 關閉註冊畫面，回到登入頁
                    } else if (code == 409) {
                        Toast.makeText(RegisterActivity.this, "此帳號已被使用，請換一個", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "註冊失敗，錯誤碼：" + code, Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "連線失敗", Toast.LENGTH_SHORT).show());
            }
        });
    }
}