package com.example.hethongbanthuoc.feature_onboarding;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.feature_auth.login.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Delay 2.5 giây rồi chuyển màn
        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            boolean isFirstRun = prefs.getBoolean("first_run", true);

            if (isFirstRun) {
                // Lần đầu mở app → đi tới Onboarding
                startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
            } else {
                // Không phải lần đầu → đi thẳng Login
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }

            finish(); // đóng Splash để không quay lại được
        }, 2500);
    }
}