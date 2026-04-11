package com.example.secondhandbookapp; // <-- 請改成你原本的第一行

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView; // 確保有匯入 TextView
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin;
    TextView btnGoToRegister; // 這裡改成 TextView 就不會閃退了

    // 模擬器請用 10.0.2.2
    private static final String API_URL = "http://10.0.2.2:3000/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. 綁定元件
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);

        // 2. 自動帶入上次登入成功的帳號
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String savedUser = sharedPreferences.getString("savedUsername", "");
        if (!savedUser.isEmpty()) {
            etUsername.setText(savedUser);
            Toast.makeText(this, "已自動帶入帳號", Toast.LENGTH_SHORT).show();
        }

        // 3. 登入按鈕邏輯
        btnLogin.setOnClickListener(v -> {
            String inputUser = etUsername.getText().toString().trim();
            String inputPass = etPassword.getText().toString().trim();

            if (inputUser.isEmpty() || inputPass.isEmpty()) {
                Toast.makeText(this, "請輸入帳號和密碼", Toast.LENGTH_SHORT).show();
                return;
            }
            loginViaAPI(inputUser, inputPass);
        });

        // 4. 跳轉到註冊頁面 (TextView 一樣可以設定點擊事件)
        btnGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginViaAPI(String username, String password) {
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

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int code = conn.getResponseCode();

                runOnUiThread(() -> {
                    if (code == 200) {
                        Toast.makeText(MainActivity.this, "登入成功！", Toast.LENGTH_SHORT).show();

                        // 儲存帳號
                        SharedPreferences.Editor editor = getSharedPreferences("UserSession", MODE_PRIVATE).edit();
                        editor.putString("savedUsername", username);
                        editor.apply();

                    } else if (code == 401) {
                        Toast.makeText(MainActivity.this, "登入失敗：帳號或密碼錯誤", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "伺服器錯誤：" + code, Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "連線失敗，請檢查網路", Toast.LENGTH_LONG).show());
            }
        });
    }
}