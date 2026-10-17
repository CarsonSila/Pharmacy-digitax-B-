package com.example.hethongbanthuoc.feature_profile.account;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.hethongbanthuoc.feature_common.EmptyStateActivity;
import com.example.hethongbanthuoc.feature_profile.personal.PersonalActivity;
import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.core.models.Model_User;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.core.utils.TokenManager;
import com.example.hethongbanthuoc.feature_auth.change_password.ChangePasswordActivity;
import com.example.hethongbanthuoc.feature_auth.login.LoginActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserPhone, tvPoints;
    private ImageView ivAvatar;
    private LinearLayout btnDangXuat;

    private LinearLayout btnThongTinCaNhan, btnQuanLyDiaChi, btnDoiMatKhau, btnDonThuoc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // Ánh xạ view
        tvUserName = findViewById(R.id.tvUserName);
        tvUserPhone = findViewById(R.id.tvUserPhone);
        tvPoints = findViewById(R.id.tvPoints);
        ivAvatar = findViewById(R.id.ivAvatar);
        btnDangXuat = findViewById(R.id.btnDangXuat);
        btnThongTinCaNhan = findViewById(R.id.btnThongTinCaNhan);
        btnQuanLyDiaChi = findViewById(R.id.btnQuanLyDiaChi);
        btnDoiMatKhau = findViewById(R.id.btnDoiMatKhau);
        btnDonThuoc = findViewById(R.id.btnDonThuoc);


        // Tải thông tin người dùng
        loadUserProfile();

        // 1. Thông tin cá nhân
        btnThongTinCaNhan.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, PersonalActivity.class);
            startActivity(intent);
        });

        // 2. Quản lý địa chỉ
        btnQuanLyDiaChi.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, EmptyStateActivity.class);
            startActivity(intent);
        });

        // 3. Đổi mật khẩu
        btnDoiMatKhau.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        /* 4. Đơn thuốc của tôi
        btnDonThuoc.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, DonThuocActivity.class);
            startActivity(intent);
        }); */

        // Xử lý đăng xuất
        btnDangXuat.setOnClickListener(v -> {
            new AlertDialog.Builder(AccountActivity.this)
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        // Chỉ xóa token, giữ email + mật khẩu nếu RememberMe tick
                        TokenManager.logout(AccountActivity.this);

                        Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Không", null)
                    .show();
        });
    }

    private void loadUserProfile() {
        String token = TokenManager.getToken(this);
        Log.d("PROFILE_DEBUG", "Token hiện tại: " + token);

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Token không tồn tại", Toast.LENGTH_SHORT).show();
            return;
        }

        API_AuthService apiService = API_RetrofitClient
                .getRetrofitInstance(this)
                .create(API_AuthService.class);

        Call<Model_User> call = apiService.getProfile("Bearer " + token);
        call.enqueue(new Callback<Model_User>() {
            @Override
            public void onResponse(Call<Model_User> call, Response<Model_User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Model_User user = response.body();

                    // Lấy dữ liệu
                    String hoTen = user.getHoTen();
                    String soDienThoai = user.getSoDienThoai();
                    int diemTichLuy = user.getDiemTichLuy();
                    String anhDaiDien = user.getAnhDaiDien();

                    // Hiển thị
                    tvUserName.setText((hoTen != null && !hoTen.isEmpty()) ? hoTen : "Người dùng");
                    tvUserPhone.setText((soDienThoai != null && !soDienThoai.isEmpty())
                            ? "SĐT: " + soDienThoai
                            : "Chưa cập nhật");
                    tvPoints.setText(String.valueOf(diemTichLuy));

                    // Hiển thị ảnh đại diện (nếu có)
                    if (anhDaiDien != null && !anhDaiDien.isEmpty()) {
                        Glide.with(AccountActivity.this)
                                .load(anhDaiDien)
                                .placeholder(R.drawable.profile)
                                .error(R.drawable.profile)
                                .circleCrop()
                                .into(ivAvatar);
                    } else {
                        ivAvatar.setImageResource(R.drawable.profile);
                    }

                } else {
                    Toast.makeText(AccountActivity.this, "Không lấy được thông tin người dùng", Toast.LENGTH_SHORT).show();
                    Log.e("PROFILE_DEBUG", "Response lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Model_User> call, Throwable t) {
                Log.e("PROFILE_DEBUG", "API call thất bại: " + t.getMessage(), t);
                Toast.makeText(AccountActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
