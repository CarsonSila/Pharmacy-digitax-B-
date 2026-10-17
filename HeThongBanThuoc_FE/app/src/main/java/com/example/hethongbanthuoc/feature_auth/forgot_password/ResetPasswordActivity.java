package com.example.hethongbanthuoc.feature_auth.forgot_password;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.feature_auth.login.LoginActivity;
import com.example.hethongbanthuoc.feature_auth.register.OTPActivity;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextView txtEmail;
    private EditText edtNewPassword, edtConfirmPassword;
    private ImageView ivToggleNewPass, ivToggleConfirmPass, ivBack;
    private Button btnSendRequest;
    private boolean isNewPassVisible = false, isConfirmPassVisible = false;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // 1. Ánh xạ view
        txtEmail = findViewById(R.id.txtEmail);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        ivToggleNewPass = findViewById(R.id.ivTogglePassword);
        ivToggleConfirmPass = findViewById(R.id.ivToggleConfirmPassword);
        ivBack = findViewById(R.id.ivBack);
        btnSendRequest = findViewById(R.id.btnSendRequest);

        // 2. Nhận email từ OTPActivity
        email = getIntent().getStringExtra("email");
        if (email != null) {
            txtEmail.setText(maskEmail(email));
        }

        // 3. Nút back → quay về OTP
        ivBack.setOnClickListener(v -> {
            Intent intent = new Intent(ResetPasswordActivity.this, OTPActivity.class);
            startActivity(intent);
            finish();
        });

        // 4. Toggle hiển thị mật khẩu mới
        ivToggleNewPass.setOnClickListener(v -> togglePasswordVisibility(edtNewPassword, ivToggleNewPass, isNewPassVisible, true));

        // 5. Toggle hiển thị confirm mật khẩu
        ivToggleConfirmPass.setOnClickListener(v -> togglePasswordVisibility(edtConfirmPassword, ivToggleConfirmPass, isConfirmPassVisible, false));

        // 6. Submit cập nhật mật khẩu
        btnSendRequest.setOnClickListener(v -> validateAndUpdatePassword());
    }

    // 7. Toggle hiển thị mật khẩu
    private void togglePasswordVisibility(EditText edt, ImageView ivToggle, boolean isVisible, boolean isNewPassword) {
        if (isVisible) {
            edt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivToggle.setImageResource(R.drawable.un_lock);
        } else {
            edt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivToggle.setImageResource(R.drawable.eye);
        }
        edt.setSelection(edt.getText().length());
        if (isNewPassword) isNewPassVisible = !isNewPassVisible;
        else isConfirmPassVisible = !isConfirmPassVisible;
    }

    // 8. Kiểm tra lỗi đầu vào & gọi API cập nhật
    private void validateAndUpdatePassword() {
        String newPass = edtNewPassword.getText().toString().trim();
        String confirmPass = edtConfirmPassword.getText().toString().trim();

        // 8.1 Email hợp lệ
        if (email == null || email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // 8.2 Mật khẩu rỗng
        if (newPass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
            return;
        }

        // 8.3 Mật khẩu tối thiểu 6 ký tự
        if (newPass.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        // 8.4 Xác nhận mật khẩu rỗng
        if (confirmPass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu xác nhận", Toast.LENGTH_SHORT).show();
            return;
        }

        // 8.5 Kiểm tra trùng khớp
        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Mật khẩu xác nhận không trùng khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        // 8.6 Kiểm tra độ mạnh mật khẩu bằng API
        checkPasswordStrength(email, newPass);
    }

    // 9. API kiểm tra độ mạnh mật khẩu
    private void checkPasswordStrength(String email, String newPass) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", newPass);

        API_AuthService apiService = API_RetrofitClient.getRetrofitInstance(this).create(API_AuthService.class);
        apiService.checkPasswordStrength(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Boolean isStrong = (Boolean) response.body().get("strong");
                    String message = (String) response.body().get("message");

                    if (isStrong != null && isStrong) {
                        // Nếu mật khẩu mạnh → cập nhật
                        updatePassword(email, newPass);
                    } else {
                        // Thông báo mật khẩu yếu
                        Toast.makeText(ResetPasswordActivity.this, message != null ? message : "Mật khẩu yếu, vui lòng chọn mật khẩu khác", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "Lỗi kiểm tra mật khẩu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ResetPasswordActivity.this, "Không thể kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 10. API cập nhật mật khẩu
    private void updatePassword(String email, String newPass) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("newPassword", newPass);

        API_AuthService apiService = API_RetrofitClient.getRetrofitInstance(this).create(API_AuthService.class);
        apiService.resetPassword(body).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ResetPasswordActivity.this, "Cập nhật mật khẩu thành công", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "Cập nhật mật khẩu thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(ResetPasswordActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 11. Ẩn bớt email
    private String maskEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex <= 2) return email;
        String prefix = email.substring(0, 2);
        String domain = email.substring(atIndex);
        return prefix + "*****" + domain;
    }
}
