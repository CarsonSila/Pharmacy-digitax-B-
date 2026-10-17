package com.example.hethongbanthuoc.feature_auth.register;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.feature_auth.login.LoginActivity;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtPassword;
    private Button btnSignUp;
    private ImageView imgTogglePassword;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        imgTogglePassword = findViewById(R.id.ivTogglePassword);

        // Ẩn/hiện mật khẩu
        imgTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                imgTogglePassword.setImageResource(R.drawable.un_lock);
                isPasswordVisible = false;
            } else {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                imgTogglePassword.setImageResource(R.drawable.eye);
                isPasswordVisible = true;
            }
            edtPassword.setSelection(edtPassword.length());
        });

        btnSignUp.setOnClickListener(v -> registerUser());

        TextView tvSingup = findViewById(R.id.tvSingup);
        tvSingup.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void registerUser() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        //  Kiểm tra rỗng
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra định dạng email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chỉ chấp nhận email có đuôi @gmail.com
        if (!email.toLowerCase().endsWith("@gmail.com")) {
            Toast.makeText(this, "Chỉ chấp nhận email có đuôi @gmail.com", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra độ dài mật khẩu
        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mặc định role là User (maVaiTro = "2")
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", email);
        requestBody.put("password", password);
        requestBody.put("hoTen", name);
        requestBody.put("maVaiTro", "2");

        API_AuthService apiService = API_RetrofitClient
                .getRetrofitInstance(this)
                .create(API_AuthService.class);

        Call<Map<String, String>> call = apiService.register(requestBody);

        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String msg = response.body().get("message");
                    Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                    finish();
                } else if (response.code() == 400) {
                    Toast.makeText(RegisterActivity.this, "Email đã tồn tại. Vui lòng dùng email khác.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Không thể kết nối đến máy chủ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
