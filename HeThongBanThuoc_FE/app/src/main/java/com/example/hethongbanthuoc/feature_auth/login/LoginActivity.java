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

        // B1: Ánh xạ các view giao diện
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);

        // B2: Khởi tạo Retrofit API service
        apiService = API_RetrofitClient.getRetrofitInstance(this).create(API_AuthService.class);

        // B3: Kiểm tra trạng thái RememberMe và token cũ
        if (TokenManager.getRememberMe(this)) {
            // Gán lại thông tin đăng nhập đã lưu
            edtEmail.setText(TokenManager.getSavedEmail(this));
            edtPassword.setText(TokenManager.getSavedPassword(this));
            cbRememberMe.setChecked(true);

            // Kiểm tra token còn hợp lệ hay không
            String token = TokenManager.getToken(this);
            if (token != null && !token.isEmpty()) {
                validateToken(token);
            }
        }

        // B4: Xử lý ẩn/hiện mật khẩu
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

        // B5: Chuyển sang trang đăng ký
        findViewById(R.id.tvLogin).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        // B6: Chuyển sang trang quên mật khẩu
        findViewById(R.id.tvForgotPassword).setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswwordActivity.class)));

        // B7: Xử lý khi nhấn nút Đăng nhập
        btnLogin.setOnClickListener(v -> loginUser());
    }

    // Hàm kiểm tra token (auto-login)
    private void validateToken(String token) {
        // B8: Gửi yêu cầu xác thực token lên server
        apiService.validateToken("Bearer " + token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // B9: Token hợp lệ → chuyển vào Home
                if (response.isSuccessful()) {
                    String role = TokenManager.getRole(LoginActivity.this);
                    Intent intent;
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        intent = new Intent(LoginActivity.this, Admin_HomePageActivity.class);
                    } else {
                        intent = new Intent(LoginActivity.this, Main_HomePageActivity.class);
                    }
                    startActivity(intent);
                    finish();
                } else {
                    // B10: Token hết hạn → xóa phiên đăng nhập
                    TokenManager.clearSession(LoginActivity.this);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // B11: Xử lý khi lỗi kết nối API
                Toast.makeText(LoginActivity.this, "Lỗi kiểm tra token: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm xử lý đăng nhập
    private void loginUser() {
        // B12: Lấy dữ liệu từ EditText
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // B13: Kiểm tra dữ liệu hợp lệ
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        // B14. Kiểm tra định dạng email
        if (!email.matches("[a-zA-Z0-9._-]+@gmail\\.com")) {
            Toast.makeText(this, "Vui lòng nhập email hợp lệ (định dạng @gmail.com)", Toast.LENGTH_SHORT).show();
            return;
        }

        // B15: Chuẩn bị request body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", email);
        requestBody.put("password", password);

        // B16: Gửi yêu cầu đăng nhập lên server
        Call<Map<String, Object>> call = apiService.login(requestBody);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                // B16: Xử lý phản hồi từ server
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    String token = (String) body.get("token");
                    String role = (String) body.get("role");
                    Object userIdObj = body.get("userId");

                    // B17: Lưu token, vai trò và tùy chọn RememberMe
                    TokenManager.saveToken(LoginActivity.this, token);
                    TokenManager.saveRole(LoginActivity.this, role);
                    TokenManager.saveRememberMe(LoginActivity.this, cbRememberMe.isChecked(), email, password);

                    // B18: Lưu UserId (nếu có)
                    if (userIdObj != null) {
                        try {
                            int userId = (int) Double.parseDouble(userIdObj.toString());
                            TokenManager.saveUserId(LoginActivity.this, userId);
                        } catch (Exception ignored) {}
                    }

                    // B19: Chuyển hướng sau khi đăng nhập thành công
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                    Intent intent = "ADMIN".equalsIgnoreCase(role)
                            ? new Intent(LoginActivity.this, Admin_HomePageActivity.class)
                            : new Intent(LoginActivity.this, Main_HomePageActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // B20: Sai thông tin đăng nhập
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                // B21: Lỗi kết nối hoặc server không phản hồi
                Toast.makeText(LoginActivity.this, "Không thể kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
