package com.example.hethongbanthuoc.feature_profile.personal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.core.models.Model_User;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.core.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonalActivity extends AppCompatActivity {

    private ImageView imgAvatar, btnBack;
    private TextView tvName, tvEmail, tvPhone, tvGender, tvBirthday, tvChangeAvatar;
    private Button btnEdit;

    // Launcher mới thay cho startActivityForResult
    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

        // Ánh xạ View
        imgAvatar = findViewById(R.id.imgAvatar);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvGender = findViewById(R.id.tvGender);
        tvBirthday = findViewById(R.id.tvBirthday);
        tvChangeAvatar = findViewById(R.id.tvChangeAvatar);
        btnEdit = findViewById(R.id.btnEdit);
        btnBack = findViewById(R.id.btnBack);

        // Load thông tin người dùng
        loadUserProfile();

        // Khởi tạo launcher Activity Result
        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Model_User updatedUser = (Model_User) data.getSerializableExtra("updated_user");
                            if (updatedUser != null) {
                                fillUserData(updatedUser); // Cập nhật UI ngay
                            }
                        }
                    }
                }
        );

        // Xử lý khi nhấn “Chỉnh sửa thông tin”
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(PersonalActivity.this, EditProfileActivity.class);
            editProfileLauncher.launch(intent); // sử dụng launcher
        });

        // Xử lý khi nhấn “Thay đổi ảnh đại diện”
        tvChangeAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(PersonalActivity.this, EditProfileActivity.class);
            intent.putExtra("change_avatar", true);
            editProfileLauncher.launch(intent); // sử dụng launcher
        });

        // Sự kiện quay về màn hình account
        btnBack.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void loadUserProfile() {
        String token = TokenManager.getToken(this);
        Log.d("PROFILE_DEBUG", "Token hiện tại: " + token);

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy token đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        API_AuthService apiService = API_RetrofitClient
                .getRetrofitInstance(this)
                .create(API_AuthService.class);

        Call<Model_User> call = apiService.getProfile("Bearer " + token);
        call.enqueue(new Callback<Model_User>() {
            @Override
            public void onResponse(Call<Model_User> call, Response<Model_User> response) {
                Log.d("PROFILE_DEBUG", "HTTP CODE: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Model_User user = response.body();
                    fillUserData(user);
                } else {
                    try {
                        Log.e("PROFILE_DEBUG", "Error body: " +
                                (response.errorBody() != null ? response.errorBody().string() : "null"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(PersonalActivity.this, "Không lấy được thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Model_User> call, Throwable t) {
                Log.e("PROFILE_DEBUG", "API call failed: " + t.getMessage(), t);
                Toast.makeText(PersonalActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillUserData(Model_User user) {
        tvName.setText(user.getHoTen() != null ? user.getHoTen() : "");
        tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        tvPhone.setText(user.getSoDienThoai() != null ? user.getSoDienThoai() : "");
        tvGender.setText(user.getGioiTinh() != null ? user.getGioiTinh() : "");
        tvBirthday.setText(user.getNgaySinh() != null ? user.getNgaySinh() : "");

        if (user.getAnhDaiDien() != null && !user.getAnhDaiDien().isEmpty()) {
            Glide.with(this)
                    .load(user.getAnhDaiDien())
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.profile);
        }
    }
}
