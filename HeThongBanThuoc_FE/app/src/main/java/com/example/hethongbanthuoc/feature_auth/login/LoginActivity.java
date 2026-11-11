package com.example.hethongbanthuoc.feature_auth.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.core.utils.TokenManager;
import com.example.hethongbanthuoc.feature_auth.forgot_password.ForgotPasswwordActivity;
import com.example.hethongbanthuoc.feature_auth.register.RegisterActivity;
import com.example.hethongbanthuoc.feature_home.Admin_HomePageActivity;
import com.example.hethongbanthuoc.feature_home.Main_HomePageActivity;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private CheckBox cbRememberMe;
    private ImageView ivTogglePassword;
    private boolean isPasswordVisible = false;
    private API_AuthService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail         = findViewById(R.id.edtEmail);
        edtPassword      = findViewById(R.id.edtPassword);
        btnLogin         = findViewById(R.id.btnLogin);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        cbRememberMe     = findViewById(R.id.cbRememberMe);

        apiService = API_RetrofitClient.getRetrofitInstance(this).create(API_AuthService.class);

        // Khôi phục email nếu RememberMe bật
        if (TokenManager.getRememberMe(this)) {
            edtEmail.setText(TokenManager.getSavedEmail(this));
            cbRememberMe.setChecked(true);

            // FIX #23: thử tự động đăng nhập lại bằng refreshToken thay vì mật khẩu
            String refreshToken = TokenManager.getRefreshToken(this);
            if (refreshToken != null && !refreshToken.isEmpty()) {
                tryRefreshToken(refreshToken);
            }
        }

        // Ẩn/hiện mật khẩu
        ivTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.un_lock);
            } else {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.eye);
            }
            isPasswordVisible = !isPasswordVisible;
            edtPassword.setSelection(edtPassword.length());
        });

        findViewById(R.id.tvLogin).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        findViewById(R.id.tvForgotPassword).setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswwordActivity.class)));

        btnLogin.setOnClickListener(v -> loginUser());
    }

    /**
     * FIX #23: Tự động làm mới token khi khởi động (thay vì dùng mật khẩu đã lưu).
     */
    private void tryRefreshToken(String refreshToken) {
        Map<String, String> body = new HashMap<>();
        body.put("refreshToken", refreshToken);

        apiService.refresh(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> respBody = response.body();
                    String newToken        = (String) respBody.get("token");
                    String newRefreshToken = (String) respBody.get("refreshToken");

                    TokenManager.saveToken(LoginActivity.this, newToken);
                    if (newRefreshToken != null) {
                        TokenManager.saveRefreshToken(LoginActivity.this, newRefreshToken);
                    }

                    navigateToHome();
                } else {
                    // Refresh token hết hạn → xoá phiên, yêu cầu đăng nhập lại
                    TokenManager.clearSession(LoginActivity.this);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                // Lỗi mạng — không xoá phiên, chờ user đăng nhập thủ công
            }
        });
    }

    private void loginUser() {
        String email    = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", email);
        requestBody.put("password", password);

        apiService.login(requestBody).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    String token        = (String) body.get("token");
                    String refreshToken = (String) body.get("refreshToken");
                    String role         = (String) body.get("role");
                    Object userIdObj    = body.get("userId");

                    TokenManager.saveToken(LoginActivity.this, token);
                    TokenManager.saveRole(LoginActivity.this, role);

                    // FIX #23: Lưu refreshToken để tự động đăng nhập lại
                    if (refreshToken != null) {
                        TokenManager.saveRefreshToken(LoginActivity.this, refreshToken);
                    }

                    // FIX #3: saveRememberMe không còn nhận password
                    TokenManager.saveRememberMe(LoginActivity.this, cbRememberMe.isChecked(), email);

                    if (userIdObj != null) {
                        try {
                            int userId = (int) Double.parseDouble(userIdObj.toString());
                            TokenManager.saveUserId(LoginActivity.this, userId);
                        } catch (Exception ignored) {}
                    }

                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                    navigateToHome();
                } else {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Không thể kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToHome() {
        String role = TokenManager.getRole(this);
        Intent intent = "ADMIN".equalsIgnoreCase(role)
                ? new Intent(this, Admin_HomePageActivity.class)
                : new Intent(this, Main_HomePageActivity.class);
        startActivity(intent);
        finish();
    }
}
