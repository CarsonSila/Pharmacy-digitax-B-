package com.example.hethongbanthuoc.feature_chat.advise;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.feature_chat.chat.ChatActivity;
import com.google.android.material.card.MaterialCardView;

public class AdviseActivity extends AppCompatActivity {

    private ImageView btnClose;
    private MaterialCardView btnChat, btnCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advise);

        // Ánh xạ view
        btnClose = findViewById(R.id.btnClose);
        btnChat = findViewById(R.id.btnChat);
        btnCall = findViewById(R.id.btnCall);

        // Đóng dialog
        btnClose.setOnClickListener(v -> finish());

        // Nhắn tin
        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(AdviseActivity.this, ChatActivity.class);
            startActivity(intent);
        });

        // Gọi tổng đài
        btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:18006928"));
            startActivity(intent);
        });
    }
}
