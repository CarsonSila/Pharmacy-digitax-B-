package com.example.hethongbanthuoc.feature_profile.address;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.hethongbanthuoc.R;

public class InfoAddressActivity extends AppCompatActivity {

    private EditText edtName, edtPhone, edtAddress;
    private TextView tvAddressType;
    private SwitchCompat switchDefault;
    private ImageView btnBack;
    private Button btnEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_address);

        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        tvAddressType = findViewById(R.id.tvAddressType);
        switchDefault = findViewById(R.id.switchDefault);
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnSave); // Nút chỉnh sửa

        // Nhận dữ liệu từ Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String tenNguoiNhan = extras.getString("tenNguoiNhan", "");
            String soDienThoai = extras.getString("soDienThoai", "");
            String diaChiChiTiet = extras.getString("diaChiChiTiet", "");
            String phuongXa = extras.getString("phuongXa", "");
            String quanHuyen = extras.getString("quanHuyen", "");
            String tinhThanh = extras.getString("tinhThanh", "");
            String loaiDiaChi = extras.getString("loaiDiaChi", "Khác");
            boolean diaChiMacDinh = extras.getBoolean("diaChiMacDinh", false);

            edtName.setText(tenNguoiNhan);
            edtPhone.setText(soDienThoai);
            edtAddress.setText(diaChiChiTiet + ", " + phuongXa + ", " + quanHuyen + ", " + tinhThanh);
            tvAddressType.setText(loaiDiaChi);
            switchDefault.setChecked(diaChiMacDinh);
        }

        btnBack.setOnClickListener(v -> finish());

        // Nút chỉnh sửa → mở AddAndressActivity ở chế độ edit
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(InfoAddressActivity.this, AddAndressActivity.class);
            if (extras != null) {
                intent.putExtra("maDiaChi", extras.getInt("maDiaChi", -1));
                intent.putExtra("tenNguoiNhan", extras.getString("tenNguoiNhan", ""));
                intent.putExtra("soDienThoai", extras.getString("soDienThoai", ""));
                intent.putExtra("diaChiChiTiet", extras.getString("diaChiChiTiet", ""));
                intent.putExtra("phuongXa", extras.getString("phuongXa", ""));
                intent.putExtra("quanHuyen", extras.getString("quanHuyen", ""));
                intent.putExtra("tinhThanh", extras.getString("tinhThanh", ""));
                intent.putExtra("loaiDiaChi", extras.getString("loaiDiaChi", "Nhà"));
                intent.putExtra("diaChiMacDinh", extras.getBoolean("diaChiMacDinh", false));
            }
            intent.putExtra("isEditMode", true);
            startActivity(intent);
        });
    }
}
