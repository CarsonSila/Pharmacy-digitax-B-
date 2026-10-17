package com.example.hethongbanthuoc.feature_auth.forgot_password;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.feature_auth.register.OTPActivity;
import com.example.hethongbanthuoc.feature_auth.login.LoginActivity;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswwordActivity extends AppCompatActivity {

    private EditText edtEmailPhone; // Trường nhập email
    private Button btnSendRequest;   // Nút gửi OTP

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_passwword);

        // Bước 1: Ánh xạ view
        edtEmailPhone = findViewById(R.id.edtEmailPhone);
        btnSendRequest = findViewById(R.id.btnSendRequest);
        ImageView ivBack = findViewById(R.id.ivBack);

        // Bước 2: Quay lại Login
        ivBack.setOnClickListener(v -> {
            startActivity(new Intent(ForgotPasswwordActivity.this, LoginActivity.class));
            finish(); // đóng màn hình hiện tại
        });

        // Bước 3: Bấm nút gửi OTP
        btnSendRequest.setOnClickListener(v -> {
            String email = edtEmailPhone.getText().toString().trim();

            // Nhóm kiểm tra lỗi nhập liệu
            // 3.1: Kiểm tra rỗng
            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3.2: Kiểm tra định dạng email hợp lệ
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3.3: Kiểm tra đuôi email @gmail.com (nếu yêu cầu)
            if (!email.toLowerCase().endsWith("@gmail.com")) {
                Toast.makeText(this, "Chỉ chấp nhận email có đuôi @gmail.com", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3.4: Gọi API gửi OTP
            sendOtp(email);
        });
    }

    // NHÓM 4: Gửi OTP qua API
    private void sendOtp(String email) {
        // Chuẩn bị dữ liệu gửi
        Map<String, String> body = new HashMap<>();
        body.put("email", email);

        // Tạo instance API
        API_AuthService apiService = API_RetrofitClient
                .getRetrofitInstance(this)
                .create(API_AuthService.class);

        // Bước 4: Gọi API
        apiService.forgotPassword(body).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                // 4.1: Xử lý thành công
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ForgotPasswwordActivity.this,
                            response.body().get("message"), Toast.LENGTH_SHORT).show();

                    // 4.2: Chuyển sang màn hình OTP
                    Intent intent = new Intent(ForgotPasswwordActivity.this, OTPActivity.class);
                    intent.putExtra("email", email); // truyền email để xác thực OTP
                    startActivity(intent);
                    finish();
                } else {
                    // 4.3: Xử lý lỗi server
                    Toast.makeText(ForgotPasswwordActivity.this,
                            "Email không tồn tại hoặc lỗi server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                // 4.4: Xử lý lỗi kết nối
                Toast.makeText(ForgotPasswwordActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
