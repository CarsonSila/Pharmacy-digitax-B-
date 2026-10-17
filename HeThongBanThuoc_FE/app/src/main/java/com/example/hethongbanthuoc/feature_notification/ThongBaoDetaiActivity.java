package com.example.hethongbanthuoc.feature_notification;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.feature_notification.models.model_ThongBao;
import com.google.android.material.appbar.AppBarLayout;

public class ThongBaoDetaiActivity extends AppCompatActivity {

    private AppBarLayout appBarLayout;
    private ImageView btnBack;
    private ImageView ivIcon;
    private TextView tvTieuDe, tvLoaiThongBao, tvThoiGian, tvNoiDung, tvNgayTao, tvTrangThai;

    private model_ThongBao thongBao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_bao_detai);

        // Nhận dữ liệu từ Intent
        thongBao = (model_ThongBao) getIntent().getSerializableExtra("thongBao");

        if (thongBao == null) {
            finish();
            return;
        }

        initViews();
        displayData();
    }

    private void initViews() {
        appBarLayout = findViewById(R.id.appBarLayout);
        ivIcon = findViewById(R.id.ivIcon);
        tvTieuDe = findViewById(R.id.tvTieuDe);
        tvLoaiThongBao = findViewById(R.id.tvLoaiThongBao);
        tvThoiGian = findViewById(R.id.tvThoiGian);
        tvNoiDung = findViewById(R.id.tvNoiDung);
        tvNgayTao = findViewById(R.id.tvNgayTao);
        tvTrangThai = findViewById(R.id.tvTrangThai);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v-> getOnBackPressedDispatcher().onBackPressed());
    }

    private void displayData() {
        // Hiển thị dữ liệu
        tvTieuDe.setText(thongBao.getTieuDe());
        tvNoiDung.setText(thongBao.getNoiDung());
        tvLoaiThongBao.setText(thongBao.getLoaiThongBao());
        tvThoiGian.setText(thongBao.getTimeAgo());
        tvNgayTao.setText("Ngày tạo: " + thongBao.getFormattedDate());
        tvTrangThai.setText(thongBao.getTrangThai());

        // Set icon
        ivIcon.setImageResource(thongBao.getIconResource());
        int color = ContextCompat.getColor(this, thongBao.getColorResource());
        ivIcon.setColorFilter(color);

        // Set màu trạng thái
        if (thongBao.isChuaDoc()) {
            tvTrangThai.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
        } else {
            tvTrangThai.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
        }
    }
}