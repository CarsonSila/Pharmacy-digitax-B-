package com.example.hethongbanthuoc.feature_common;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.hethongbanthuoc.R;

public class SuccessActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("SUCCESS_ACT", "SuccessActivity started");

        setContentView(R.layout.activity_success);

        // 3s tự động đóng
        new Handler().postDelayed(() -> {
            Log.d("SUCCESS_ACT", "Auto closing SuccessActivity");
            finish();
        }, 3000);
    }
}