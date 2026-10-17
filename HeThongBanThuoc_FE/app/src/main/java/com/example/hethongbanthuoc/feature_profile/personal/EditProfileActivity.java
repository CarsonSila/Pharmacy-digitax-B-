package com.example.hethongbanthuoc.feature_profile.personal;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.hethongbanthuoc.feature_common.SuccessActivity;
import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.core.models.Model_User;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.core.utils.TokenManager;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    // 1. Khai báo View
    private ImageView imgAvatar, btnCalendar, btnBack;
    private TextView tvChangeAvatar;
    private EditText edtName, edtEmail, edtPhone, edtBirthday;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale, rbOther;
    private Button btnUpdate;
    private Uri selectedAvatarUri = null; // Nếu chọn avatar mới
    private String token; // Token đăng nhập
    private Model_User currentUser; // Lưu thông tin user hiện tại

    private static final int REQUEST_PICK_IMAGE = 1001;
    private static final int REQUEST_CAMERA = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // 2. Ánh xạ View
        imgAvatar = findViewById(R.id.imgAvatar);
        tvChangeAvatar = findViewById(R.id.tvChangeAvatar);
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtBirthday = findViewById(R.id.edtBirthday);
        rgGender = findViewById(R.id.rgGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        rbOther = findViewById(R.id.rbOther);
        btnCalendar = findViewById(R.id.btnCalendar);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish(); // đóng EditProfileActivity, quay về PersonalActivity
        });

        // 3. Lấy token đăng nhập
        token = TokenManager.getToken(this);

        // 4. Load dữ liệu người dùng từ API
        loadUserProfile();

        // 5. Xử lý chọn ngày sinh
        btnCalendar.setOnClickListener(v -> showDatePicker());

        // 6. Thay đổi avatar với AlertDialog lựa chọn
        tvChangeAvatar.setOnClickListener(v -> showAvatarOptions());

        // 7. Cập nhật thông tin người dùng
        btnUpdate.setOnClickListener(v -> checkAndUpdateProfile());
    }

    // 4. Load dữ liệu user từ API
    private void loadUserProfile() {
        API_AuthService apiService = API_RetrofitClient
                .getRetrofitInstance(this)
                .create(API_AuthService.class);

        Call<Model_User> call = apiService.getProfile("Bearer " + token);
        call.enqueue(new Callback<Model_User>() {
            @Override
            public void onResponse(Call<Model_User> call, Response<Model_User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    fillUserData(currentUser);
                } else {
                    Toast.makeText(EditProfileActivity.this, "Không lấy được thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Model_User> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //  4.1 Điền dữ liệu vào form
    private void fillUserData(Model_User user) {
        edtName.setText(user.getHoTen() != null ? user.getHoTen() : "");
        edtEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        edtPhone.setText(user.getSoDienThoai() != null ? user.getSoDienThoai() : "");
        edtBirthday.setText(user.getNgaySinh() != null ? user.getNgaySinh() : "");

        // Giới tính
        if (user.getGioiTinh() != null) {
            switch (user.getGioiTinh()) {
                case "Nam": rbMale.setChecked(true); break;
                case "Nữ": rbFemale.setChecked(true); break;
                case "Khác": rbOther.setChecked(true); break;
                default: rgGender.clearCheck(); break;
            }
        } else rgGender.clearCheck();

        // Avatar
        if (user.getAnhDaiDien() != null && !user.getAnhDaiDien().isEmpty()) {
            Glide.with(this)
                    .load(user.getAnhDaiDien())
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .into(imgAvatar);
        }
    }

    // 5. DatePicker
    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) ->
                        edtBirthday.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    // 6. AlertDialog chọn avatar
    private void showAvatarOptions() {
        String[] options = {"Chọn từ thư viện", "Chụp ảnh"};
        new AlertDialog.Builder(this)
                .setTitle("Chọn ảnh đại diện")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) { // Chọn từ gallery
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        startActivityForResult(intent, REQUEST_PICK_IMAGE);
                    } else if (which == 1) { // Chụp ảnh
                        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, REQUEST_CAMERA);
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_PICK_IMAGE) {
                selectedAvatarUri = data.getData();
                imgAvatar.setImageURI(selectedAvatarUri);
            } else if (requestCode == REQUEST_CAMERA) {
                // Lấy ảnh từ camera
                Bundle extras = data.getExtras();
                if (extras != null && extras.get("data") != null) {
                    imgAvatar.setImageBitmap((android.graphics.Bitmap) extras.get("data"));
                    selectedAvatarUri = null; // Bạn có thể xử lý upload bitmap riêng
                }
            }
        }
    }

    // 7. Kiểm tra thay đổi & cập nhật
    private void checkAndUpdateProfile() {
        if (currentUser == null) return;

        boolean hasChange = false;

        // 1. Lấy dữ liệu mới từ form
        String newName = edtName.getText().toString().trim();
        String newEmail = edtEmail.getText().toString().trim();
        String newPhone = edtPhone.getText().toString().trim();
        String newBirthday = edtBirthday.getText().toString().trim();
        String newGender = null;

        int selectedId = rgGender.getCheckedRadioButtonId();
        if (selectedId == rbMale.getId()) newGender = "Nam";
        else if (selectedId == rbFemale.getId()) newGender = "Nữ";
        else if (selectedId == rbOther.getId()) newGender = "Khác";

        // 2. Reset cảnh báo
        edtName.setError(null);
        edtEmail.setError(null);
        edtPhone.setError(null);
        edtBirthday.setError(null);

        // Nếu bạn muốn highlight giới tính, hãy dùng TextView riêng cho nhãn
        TextView tvGenderLabel = findViewById(R.id.tvGenderLabel);
        if(tvGenderLabel != null) tvGenderLabel.setTextColor(Color.parseColor("#666666")); // màu gốc

        // 3. Kiểm tra từng field
        if (!newName.equals(currentUser.getHoTen())) hasChange = true;
        if (!newEmail.equals(currentUser.getEmail())) hasChange = true;
        if (!newPhone.equals(currentUser.getSoDienThoai())) hasChange = true;
        if (!newBirthday.equals(currentUser.getNgaySinh())) hasChange = true;
        if ((currentUser.getGioiTinh() == null && newGender != null) ||
                (currentUser.getGioiTinh() != null && !currentUser.getGioiTinh().equals(newGender))) hasChange = true;
        if (selectedAvatarUri != null) hasChange = true;

        // 4. Nếu không có thay đổi, highlight lỗi
        if (!hasChange) {
            if (newName.equals(currentUser.getHoTen())) edtName.setError("Chưa thay đổi tên");
            if (newEmail.equals(currentUser.getEmail())) edtEmail.setError("Chưa thay đổi email");
            if (newPhone.equals(currentUser.getSoDienThoai())) edtPhone.setError("Chưa thay đổi số điện thoại");
            if (newBirthday.equals(currentUser.getNgaySinh())) edtBirthday.setError("Chưa thay đổi ngày sinh");
            if ((currentUser.getGioiTinh() == null && newGender == null) ||
                    (currentUser.getGioiTinh() != null && currentUser.getGioiTinh().equals(newGender))) {
                if(tvGenderLabel != null) tvGenderLabel.setTextColor(Color.RED);
            }
            Toast.makeText(this, "Bạn chưa thay đổi thông tin nào!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 5. Cập nhật dữ liệu vào object
        currentUser.setHoTen(newName);
        currentUser.setEmail(newEmail);
        currentUser.setSoDienThoai(newPhone);
        currentUser.setNgaySinh(newBirthday);
        currentUser.setGioiTinh(newGender);

        // 6. Gọi API update
        API_AuthService apiService = API_RetrofitClient
                .getRetrofitInstance(this)
                .create(API_AuthService.class);

        Call<Model_User> call = apiService.updateProfile("Bearer " + token, currentUser);
        call.enqueue(new Callback<Model_User>() {
            @Override
            public void onResponse(Call<Model_User> call, Response<Model_User> response) {
                Log.d("UPDATE_API", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Model_User updatedUser = response.body();
                    Log.d("UPDATE_API", "Cập nhật thành công: " + updatedUser.getHoTen());

                    // Trả dữ liệu về PersonalActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updated_user", updatedUser);
                    setResult(RESULT_OK, resultIntent);

                    // Mở SuccessActivity
                    Intent successIntent = new Intent(EditProfileActivity.this, SuccessActivity.class);
                    startActivity(successIntent);

                    finish();
                } else {
                    Log.e("UPDATE_API", "Lỗi response: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            Log.e("UPDATE_API", "Error body: " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Model_User> call, Throwable t) {
                Log.e("UPDATE_API", "API call failed: " + t.getMessage());
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
