package com.example.hethongbanthuoc.feature_auth.change_password;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.Editable;
import android.text.InputType;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    // 1/ Khai báo UI Components
    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private ImageView ivCurrentPasswordIcon, ivNewPasswordIcon, ivConfirmPasswordIcon;
    private MaterialButton btnChangePassword, btnCancel;
    private android.view.View strengthBar1, strengthBar2, strengthBar3, strengthBar4;

    // 2/ Khai báo API Service
    private API_AuthService apiAuthService;

    // 3/ Trạng thái hiển thị mật khẩu
    private boolean isCurrentVisible = false, isNewVisible = false, isConfirmVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // 4/ Ánh xạ tất cả view
        mapViews();

        // 5/ Khởi tạo Retrofit và API Service
        apiAuthService = API_RetrofitClient.getRetrofitInstance(this).create(API_AuthService.class);

        // 6/ Thiết lập toggle hiển thị mật khẩu
        setupTogglePassword();

        // 7/ Cập nhật thanh đánh giá độ mạnh mật khẩu realtime
        etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateStrengthBar(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        // 8/ Click đổi mật khẩu
        btnChangePassword.setOnClickListener(v -> handleChangePassword());

        // 9/ Click hủy
        btnCancel.setOnClickListener(v -> finish());
    }

    // 4/ mapViews - ánh xạ view
    private void mapViews() {
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        ivCurrentPasswordIcon = findViewById(R.id.ivCurrentPasswordIcon);
        ivNewPasswordIcon = findViewById(R.id.ivNewPasswordIcon);
        ivConfirmPasswordIcon = findViewById(R.id.ivConfirmPasswordIcon);

        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnCancel = findViewById(R.id.btnCancel);

        strengthBar1 = findViewById(R.id.strengthBar1);
        strengthBar2 = findViewById(R.id.strengthBar2);
        strengthBar3 = findViewById(R.id.strengthBar3);
        strengthBar4 = findViewById(R.id.strengthBar4);
    }

    // 6/ setupTogglePassword - ẩn/hiện mật khẩu
    private void setupTogglePassword() {
        ivCurrentPasswordIcon.setOnClickListener(v -> togglePassword(etCurrentPassword, ivCurrentPasswordIcon, "current"));
        ivNewPasswordIcon.setOnClickListener(v -> togglePassword(etNewPassword, ivNewPasswordIcon, "new"));
        ivConfirmPasswordIcon.setOnClickListener(v -> togglePassword(etConfirmPassword, ivConfirmPasswordIcon, "confirm"));
    }

    private void togglePassword(TextInputEditText et, ImageView iv, String type) {
        switch (type) {
            case "current": isCurrentVisible = toggleVisibility(et, iv, isCurrentVisible); break;
            case "new": isNewVisible = toggleVisibility(et, iv, isNewVisible); break;
            case "confirm": isConfirmVisible = toggleVisibility(et, iv, isConfirmVisible); break;
        }
    }

    private boolean toggleVisibility(TextInputEditText et, ImageView iv, boolean isVisible) {
        if (isVisible) {
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            iv.setImageResource(R.drawable.un_lock);
        } else {
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            iv.setImageResource(R.drawable.eye);
        }
        et.setSelection(et.length());
        return !isVisible;
    }

    // 7/ updateStrengthBar - cập nhật thanh đánh giá mật khẩu
    private void updateStrengthBar(String password) {
        int passed = 0;
        if (password.matches(".*[A-Z].*")) passed++;
        if (password.matches(".*[a-z].*")) passed++;
        if (password.matches(".*\\d.*")) passed++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) passed++;

        int colorActive = getColor(R.color.teal_700);
        int colorInactive = getColor(R.color.gray);

        strengthBar1.setBackgroundColor(passed >= 1 ? colorActive : colorInactive);
        strengthBar2.setBackgroundColor(passed >= 2 ? colorActive : colorInactive);
        strengthBar3.setBackgroundColor(passed >= 3 ? colorActive : colorInactive);
        strengthBar4.setBackgroundColor(passed >= 4 ? colorActive : colorInactive);
    }


    // 8 handleChangePassword - kiểm tra các trường và gọi API
    private void handleChangePassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Bắt lỗi 1: Trường rỗng
        if (TextUtils.isEmpty(currentPassword)) { Toast.makeText(this, "Vui lòng nhập mật khẩu hiện tại", Toast.LENGTH_SHORT).show(); return; }
        if (TextUtils.isEmpty(newPassword)) { Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show(); return; }
        if (TextUtils.isEmpty(confirmPassword)) { Toast.makeText(this, "Vui lòng nhập mật khẩu xác nhận", Toast.LENGTH_SHORT).show(); return; }

        // Bắt lỗi 2: Mật khẩu mới khác mật khẩu hiện tại
        if (currentPassword.equals(newPassword)) { Toast.makeText(this, "Mật khẩu mới phải khác mật khẩu hiện tại", Toast.LENGTH_SHORT).show(); return; }

        // /Bắt lỗi 3: Confirm password phải trùng
        if (!newPassword.equals(confirmPassword)) { Toast.makeText(this, "Mật khẩu xác nhận không trùng khớp", Toast.LENGTH_SHORT).show(); return; }

        // Bắt lỗi 4: Kiểm tra độ mạnh mật khẩu bằng API
        Map<String, String> body = new HashMap<>();
        body.put("password", newPassword);

        apiAuthService.checkPasswordStrength(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // Lấy các trường chi tiết
                    boolean length = Boolean.TRUE.equals(response.body().get("length"));
                    boolean uppercase = Boolean.TRUE.equals(response.body().get("uppercase"));
                    boolean lowercase = Boolean.TRUE.equals(response.body().get("lowercase"));
                    boolean number = Boolean.TRUE.equals(response.body().get("number"));
                    boolean specialChar = Boolean.TRUE.equals(response.body().get("specialChar"));
                    Object strengthObj = response.body().get("strength");
                    String strength = strengthObj != null ? strengthObj.toString() : "Yếu";

                    // Cập nhật thanh strength bar dựa vào chi tiết từ server
                    updateStrengthBarDetail(length, uppercase, lowercase, number, specialChar);

                    // Thông báo nếu mật khẩu yếu hoặc trung bình
                    if (!strength.equalsIgnoreCase("Mạnh")) {
                        Toast.makeText(ChangePasswordActivity.this, "Mật khẩu chưa đủ mạnh: " + strength, Toast.LENGTH_LONG).show();
                        return;
                    }

                    // 10 Gọi API đổi mật khẩu nếu mật khẩu mạnh/
                    submitChangePassword(currentPassword, newPassword, confirmPassword);

                } else {
                    Toast.makeText(ChangePasswordActivity.this, "Không thể kiểm tra độ mạnh mật khẩu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ChangePasswordActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Cập nhật thanh strength bar dựa vào từng điều kiện từ server
    private void updateStrengthBarDetail(boolean length, boolean uppercase, boolean lowercase, boolean number, boolean specialChar) {
        int colorActive = getColor(R.color.teal_700);
        int colorInactive = getColor(R.color.gray);

        strengthBar1.setBackgroundColor(length ? colorActive : colorInactive);
        strengthBar2.setBackgroundColor(uppercase ? colorActive : colorInactive);
        strengthBar3.setBackgroundColor(lowercase ? colorActive : colorInactive);
        strengthBar4.setBackgroundColor(number && specialChar ? colorActive : colorInactive);
    }

    // 10 submitChangePassword - gọi API đổi mật khẩu
    private void submitChangePassword(String current, String newPass, String confirm) {
        Map<String, String> request = new HashMap<>();
        request.put("currentPassword", current);
        request.put("newPassword", newPass);
        request.put("confirmPassword", confirm);

        apiAuthService.changePassword(request).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body().get("message") != null ? response.body().get("message") : "Không xác định";
                    Toast.makeText(ChangePasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                    if ("Đổi mật khẩu thành công".equalsIgnoreCase(message)) finish();
                } else {
                    Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(ChangePasswordActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
