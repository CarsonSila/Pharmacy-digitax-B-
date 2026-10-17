package com.example.hethongbanthuoc.feature_auth.register;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.feature_auth.forgot_password.ForgotPasswwordActivity;
import com.example.hethongbanthuoc.feature_auth.forgot_password.ResetPasswordActivity;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OTPActivity extends AppCompatActivity {

    // B1: Khai báo các view và biến
    private TextView txtEmail, txtResend, txtDescription;
    private EditText otp1, otp2, otp3, otp4, otp5, otp6;
    private Button btnVerify;
    private CountDownTimer countDownTimer;
    private String email; // email nhận từ ForgotPassword
    private int resendCount = 0; // đếm số lần resend OTP

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        // B2: Ánh xạ view
        txtEmail = findViewById(R.id.txtEmail);
        txtDescription = findViewById(R.id.txtDescription);
        txtResend = findViewById(R.id.txtResend);
        btnVerify = findViewById(R.id.btnVerify);

        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);

        // B3: Nút quay lại ForgotPassword
        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> {
            startActivity(new Intent(OTPActivity.this, ForgotPasswwordActivity.class));
            finish();
        });

        // B4: Nhận email từ ForgotPassword và hiển thị ẩn bớt để bảo mật
        email = getIntent().getStringExtra("email");
        if (email != null) {
            txtEmail.setText(maskEmail(email));
            txtDescription.setText("Vui lòng nhập mã xác thực được gửi đến " + maskEmail(email));
        }

        // B5: Thiết lập các ô OTP chỉ nhập số, 1 ký tự mỗi ô
        setupOtpFields();

        // B6: Bắt đầu đếm ngược resend OTP
        startResendCountdown();

        // B7: Sự kiện bấm nút resend
        txtResend.setOnClickListener(v -> {
            if (txtResend.isEnabled()) {
                if (resendCount >= 3) {
                    Toast.makeText(this, "Bạn đã vượt quá số lần gửi OTP", Toast.LENGTH_SHORT).show();
                    return;
                }
                resendOtp(email);
            }
        });

        // B8: Sự kiện bấm nút verify OTP
        btnVerify.setOnClickListener(v -> verifyOtpInput());
    }

    // Chức năng: Thiết lập các ô OTP chỉ nhập số, tối đa 1 ký tự
    private void setupOtpFields() {
        EditText[] otps = {otp1, otp2, otp3, otp4, otp5, otp6};
        for (EditText otp : otps) {
            otp.setInputType(InputType.TYPE_CLASS_NUMBER);
            otp.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        }
    }

    // Chức năng: Kiểm tra các ô OTP và gọi API verify
    private void verifyOtpInput() {
        // Bắt lỗi email
        if (email == null || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy giá trị OTP từ 6 ô
        String[] otpValues = {otp1.getText().toString().trim(), otp2.getText().toString().trim(),
                otp3.getText().toString().trim(), otp4.getText().toString().trim(),
                otp5.getText().toString().trim(), otp6.getText().toString().trim()};

        // Bắt lỗi nếu ô nào trống
        for (String s : otpValues) {
            if (s.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ 6 số OTP", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // B9: Gộp OTP và gọi API verify
        String otp = String.join("", otpValues);
        verifyOtp(email, otp);
    }

    // Chức năng: Bắt đầu đếm ngược resend 60 giây
    private void startResendCountdown() {
        txtResend.setEnabled(false);
        txtResend.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                txtResend.setText("Gửi lại sau (" + millisUntilFinished / 1000 + "s)");
            }

            @Override
            public void onFinish() {
                txtResend.setText("Bạn không nhận được mã? Gửi lại");
                txtResend.setEnabled(true);
                txtResend.setTextColor(ContextCompat.getColor(OTPActivity.this, android.R.color.holo_blue_light));
            }
        }.start();
    }

    // Chức năng: Gửi lại OTP
    private void resendOtp(String email) {
        // Bắt lỗi email
        if (email == null || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tăng số lần gửi
        resendCount++;

        // Gọi API gửi lại OTP
        Map<String, String> body = new HashMap<>();
        body.put("email", email);

        API_AuthService apiService = API_RetrofitClient.getRetrofitInstance(this).create(API_AuthService.class);
        apiService.forgotPassword(body).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(OTPActivity.this, "Mã OTP mới đã được gửi", Toast.LENGTH_SHORT).show();
                    startResendCountdown();
                } else {
                    Toast.makeText(OTPActivity.this, "Lỗi khi gửi lại OTP", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(OTPActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Chức năng: Gọi API verify OTP
    private void verifyOtp(String email, String otp) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("otp", otp);

        API_AuthService apiService = API_RetrofitClient.getRetrofitInstance(this).create(API_AuthService.class);
        apiService.verifyOtp(body).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(OTPActivity.this, "Xác thực OTP thành công", Toast.LENGTH_SHORT).show();

                    // B10: Nếu OTP đúng, chuyển sang ResetPasswordActivity
                    Intent intent = new Intent(OTPActivity.this, ResetPasswordActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(OTPActivity.this, "OTP không hợp lệ hoặc đã hết hạn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(OTPActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Chức năng: Ẩn bớt email để bảo mật
    private String maskEmail(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex <= 2) return email;
        String prefix = email.substring(0, 2);
        String domain = email.substring(atIndex);
        return prefix + "*****" + domain;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy đếm ngược khi Activity bị hủy
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
