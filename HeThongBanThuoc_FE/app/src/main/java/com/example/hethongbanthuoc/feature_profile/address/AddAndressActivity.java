package com.example.hethongbanthuoc.feature_profile.address;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.example.hethongbanthuoc.feature_common.EmptyStateActivity;
import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.feature_profile.models.Model_DiaChi;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.core.utils.TokenManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddAndressActivity extends AppCompatActivity {

    // Views
    private ImageView btnBack, btnNext;
    private TextInputEditText edtName, edtPhone, edtStreetAddress;
    private TextView tvLocationSelector, tvDelete;
    private MaterialButton btnTypeHome, btnTypeOffice, btnComplete;
    private SwitchCompat switchDefault;

    // Trạng thái
    private boolean isEditMode = false;
    private int idDiaChiSua = -1;
    private String loaiDiaChi = "Nhà";
    private boolean isDefault = false;
    private String khuVucDayDu = ""; // Lưu khu vực (phường/xã, quận/huyện, tỉnh/thành)

    // Lưu dữ liệu gốc để so sánh
    private String tenNguoiNhanGoc = "";
    private String soDienThoaiGoc = "";
    private String diaChiChiTietGoc = "";
    private String khuVucDayDuGoc = "";
    private String loaiDiaChiGoc = "Nhà";
    private boolean isDiaChiMacDinhGoc = false;

    private ActivityResultLauncher<Intent> selectCityLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_andress);

        // Ánh xạ view
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtStreetAddress = findViewById(R.id.edtStreetAddress);
        btnTypeHome = findViewById(R.id.btnTypeHome);
        btnTypeOffice = findViewById(R.id.btnTypeOffice);
        switchDefault = findViewById(R.id.switchDefault);
        btnComplete = findViewById(R.id.btnComplete);
        tvLocationSelector = findViewById(R.id.tvLocationSelector);
        tvDelete = findViewById(R.id.tvDelete);

        // Mở Activity chọn tỉnh/huyện
        selectCityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String address = result.getData().getStringExtra("selected_address");
                        if (address != null) {
                            khuVucDayDu = address;
                            tvLocationSelector.setText(address);
                        }
                    }
                }
        );

        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Mở chọn khu vực
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectCityActivity.class);
            selectCityLauncher.launch(intent);
        });

        // Chọn loại địa chỉ
        btnTypeHome.setOnClickListener(v -> selectAddressType("Nhà"));
        btnTypeOffice.setOnClickListener(v -> selectAddressType("Văn phòng"));

        // Bật/tắt mặc định
        switchDefault.setOnCheckedChangeListener((buttonView, isChecked) -> isDefault = isChecked);

        // Xử lý số điện thoại
        setupPhoneWatcher();

        // Nhận dữ liệu nếu sửa
        handleIntentExtras();

        // Hoàn tất / cập nhật
        btnComplete.setOnClickListener(v -> {
            if (isEditMode) capNhatDiaChi();
            else validateAndSubmit();
        });
    }

    // Nhận dữ liệu khi sửa - TÁCH RIÊNG địa chỉ chi tiết và khu vực
    private void handleIntentExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("maDiaChi")) {
            isEditMode = true;
            idDiaChiSua = extras.getInt("maDiaChi", -1);

            // Lưu dữ liệu gốc
            tenNguoiNhanGoc = extras.getString("tenNguoiNhan", "");
            soDienThoaiGoc = extras.getString("soDienThoai", "");
            diaChiChiTietGoc = extras.getString("diaChiChiTiet", "");

            edtName.setText(tenNguoiNhanGoc);
            edtPhone.setText(soDienThoaiGoc);
            edtStreetAddress.setText(diaChiChiTietGoc);

            // Gom khu vực riêng để hiển thị trong TextView
            String phuongXa = extras.getString("phuongXa", "");
            String quanHuyen = extras.getString("quanHuyen", "");
            String tinhThanh = extras.getString("tinhThanh", "");

            khuVucDayDu = String.join(", ", new String[]{phuongXa, quanHuyen, tinhThanh})
                    .replaceAll("(, )+", ", ")
                    .replaceAll("^(, )|, $", "");
            khuVucDayDuGoc = khuVucDayDu; // Lưu khu vực gốc
            tvLocationSelector.setText(khuVucDayDu);

            loaiDiaChi = extras.getString("loaiDiaChi", "Nhà");
            loaiDiaChiGoc = loaiDiaChi; // Lưu loại địa chỉ gốc
            selectAddressType(loaiDiaChi);

            isDefault = extras.getBoolean("diaChiMacDinh", false);
            isDiaChiMacDinhGoc = isDefault; // Lưu trạng thái mặc định gốc
            switchDefault.setChecked(isDefault);

            tvDelete.setVisibility(android.view.View.VISIBLE);
            tvDelete.setOnClickListener(v -> xoaDiaChi());
            btnComplete.setText("Cập nhật");
        } else {
            btnComplete.setText("Hoàn tất");
        }
    }

    private void selectAddressType(String type) {
        loaiDiaChi = type;
        if (type.equals("Nhà")) {
            btnTypeHome.setBackgroundResource(R.drawable.bg_button_primary);
            btnTypeHome.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            btnTypeOffice.setBackgroundResource(R.drawable.bg_button_outlined);
            btnTypeOffice.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        } else {
            btnTypeOffice.setBackgroundResource(R.drawable.bg_button_primary);
            btnTypeOffice.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            btnTypeHome.setBackgroundResource(R.drawable.bg_button_outlined);
            btnTypeHome.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));
        }
    }

    private void setupPhoneWatcher() {
        edtPhone.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String input = s.toString().trim();
                String newInput = input;
                if (input.startsWith("0")) newInput = "+84" + input.substring(1);

                if (!newInput.equals(input)) {
                    edtPhone.setText(newInput);
                }

                int pos = Math.min(newInput.length(), edtPhone.getText().length());
                edtPhone.setSelection(pos);

                if (!edtPhone.getText().toString().startsWith("+84")) {
                    edtPhone.setError("Số điện thoại phải bắt đầu +84");
                } else {
                    edtPhone.setError(null);
                }

                isFormatting = false;
            }
        });
    }

    // Validate và thêm mới - LƯU RIÊNG địa chỉ chi tiết và khu vực
    private void validateAndSubmit() {
        String ten = edtName.getText().toString().trim();
        String sdt = edtPhone.getText().toString().trim();
        String diaChiChiTiet = edtStreetAddress.getText().toString().trim();

        if (ten.isEmpty() || sdt.isEmpty() || diaChiChiTiet.isEmpty() || khuVucDayDu.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        Model_DiaChi diaChi = new Model_DiaChi();
        diaChi.setTenNguoiNhan(ten);
        diaChi.setSoDienThoai(sdt);

        // CHỈ LƯU ĐỊA CHỈ CHI TIẾT (số nhà, ngõ) - KHÔNG GỘP KHU VỰC
        diaChi.setDiaChiChiTiet(diaChiChiTiet);

        diaChi.setLoaiDiaChi(loaiDiaChi);
        diaChi.setDiaChiMacDinh(isDefault);

        // Tách khu vực riêng vào các trường riêng
        String[] khuVucParts = khuVucDayDu.split(",");
        diaChi.setPhuongXa(khuVucParts.length > 0 ? khuVucParts[0].trim() : "");
        diaChi.setQuanHuyen(khuVucParts.length > 1 ? khuVucParts[1].trim() : "");
        diaChi.setTinhThanh(khuVucParts.length > 2 ? khuVucParts[2].trim() : "");

        if (isDefault) kiemTraMacDinhVaThem(diaChi);
        else themDiaChiAPI(diaChi, false);
    }

    //Cập nhật địa chỉ - KIỂM TRA THAY ĐỔI trước khi cập nhật
    private void capNhatDiaChi() {
        String ten = edtName.getText().toString().trim();
        String sdt = edtPhone.getText().toString().trim();
        String diaChiChiTiet = edtStreetAddress.getText().toString().trim();

        if (ten.isEmpty() || sdt.isEmpty() || diaChiChiTiet.isEmpty() || khuVucDayDu.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // KIỂM TRA XEM CÓ THAY ĐỔI GÌ KHÔNG
        boolean coThayDoi = kiemTraCoThayDoi(ten, sdt, diaChiChiTiet);

        if (!coThayDoi) {
            // Hiển thị dialog thông báo không có thay đổi
            new AlertDialog.Builder(this)
                    .setTitle("Không có thay đổi")
                    .setMessage("Bạn chưa thay đổi bất kỳ thông tin nào. Vui lòng chỉnh sửa trước khi cập nhật.")
                    .setPositiveButton("OK", null)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
            return;
        }

        Model_DiaChi diaChi = new Model_DiaChi();
        diaChi.setMaDiaChi(idDiaChiSua);
        diaChi.setTenNguoiNhan(ten);
        diaChi.setSoDienThoai(sdt);

        // CHỈ LƯU ĐỊA CHỈ CHI TIẾT (số nhà, ngõ) - KHÔNG GỘP KHU VỰC
        diaChi.setDiaChiChiTiet(diaChiChiTiet);

        diaChi.setLoaiDiaChi(loaiDiaChi);
        diaChi.setDiaChiMacDinh(isDefault);

        // Tách khu vực riêng vào các trường riêng
        String[] khuVucParts = khuVucDayDu.split(",");
        diaChi.setPhuongXa(khuVucParts.length > 0 ? khuVucParts[0].trim() : "");
        diaChi.setQuanHuyen(khuVucParts.length > 1 ? khuVucParts[1].trim() : "");
        diaChi.setTinhThanh(khuVucParts.length > 2 ? khuVucParts[2].trim() : "");

        String token = TokenManager.getToken(this);
        if (token == null) {
            Toast.makeText(this, "Chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo bản copy để gửi API (gộp địa chỉ đầy đủ nếu backend yêu cầu)
        Model_DiaChi diaChiGuiAPI = taoObjectGuiAPI(diaChi);

        API_AuthService api = API_RetrofitClient.getRetrofitInstance(this).create(API_AuthService.class);
        api.capNhatDiaChi("Bearer " + token, diaChiGuiAPI).enqueue(new Callback<Map<String,String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddAndressActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddAndressActivity.this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(AddAndressActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Kiểm tra xem có thay đổi dữ liệu không
    private boolean kiemTraCoThayDoi(String ten, String sdt, String diaChiChiTiet) {
        // So sánh tên
        if (!ten.equals(tenNguoiNhanGoc)) {
            return true;
        }

        // So sánh số điện thoại (chuẩn hóa trước khi so sánh)
        String sdtChuanHoa = sdt.startsWith("+84") ? sdt : "+84" + sdt.replaceFirst("^0", "");
        String sdtGocChuanHoa = soDienThoaiGoc.startsWith("+84") ? soDienThoaiGoc : "+84" + soDienThoaiGoc.replaceFirst("^0", "");
        if (!sdtChuanHoa.equals(sdtGocChuanHoa)) {
            return true;
        }

        // So sánh địa chỉ chi tiết
        if (!diaChiChiTiet.equals(diaChiChiTietGoc)) {
            return true;
        }

        // So sánh khu vực
        if (!khuVucDayDu.equals(khuVucDayDuGoc)) {
            return true;
        }

        // So sánh loại địa chỉ
        if (!loaiDiaChi.equals(loaiDiaChiGoc)) {
            return true;
        }

        // So sánh trạng thái mặc định
        if (isDefault != isDiaChiMacDinhGoc) {
            return true;
        }

        // Không có thay đổi gì
        return false;
    }

    // Kiểm tra mặc định trước khi thêm
    private void kiemTraMacDinhVaThem(Model_DiaChi diaChiMoi) {
        String token = TokenManager.getToken(this);
        if (token == null) {
            Toast.makeText(this, "Chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        API_AuthService api = API_RetrofitClient.getRetrofitInstance(this).create(API_AuthService.class);
        api.getDanhSachDiaChi("Bearer " + token).enqueue(new Callback<List<Model_DiaChi>>() {
            @Override
            public void onResponse(Call<List<Model_DiaChi>> call, Response<List<Model_DiaChi>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean coMacDinhCu = false;
                    for (Model_DiaChi d : response.body()) {
                        if (Boolean.TRUE.equals(d.getDiaChiMacDinh())) {
                            coMacDinhCu = true;
                            break;
                        }
                    }

                    if (!coMacDinhCu) {
                        themDiaChiAPI(diaChiMoi, true);
                    } else {
                        new AlertDialog.Builder(AddAndressActivity.this)
                                .setTitle("Thay đổi địa chỉ mặc định")
                                .setMessage("Bạn đã có một địa chỉ mặc định. Bạn có muốn thay đổi sang địa chỉ mới không?")
                                .setPositiveButton("Có", (dialog, which) -> themDiaChiAPI(diaChiMoi, true))
                                .setNegativeButton("Không", null)
                                .show();
                    }
                } else {
                    Toast.makeText(AddAndressActivity.this, "Không thể kiểm tra danh sách!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Model_DiaChi>> call, Throwable t) {
                Toast.makeText(AddAndressActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Tạo object để gửi API - KHÔNG GỘP, chỉ gửi từng trường riên
    private Model_DiaChi taoObjectGuiAPI(Model_DiaChi diaChi) {
        Model_DiaChi diaChiAPI = new Model_DiaChi();

        if (diaChi.getMaDiaChi() != null) {
            diaChiAPI.setMaDiaChi(diaChi.getMaDiaChi());
        }

        diaChiAPI.setTenNguoiNhan(diaChi.getTenNguoiNhan());
        diaChiAPI.setSoDienThoai(diaChi.getSoDienThoai());

        // CHỈ GỬI ĐỊA CHỈ CHI TIẾT - KHÔNG GỘP KHU VỰC
        diaChiAPI.setDiaChiChiTiet(diaChi.getDiaChiChiTiet());

        // GỬI CÁC TRƯỜNG RIÊNG
        diaChiAPI.setPhuongXa(diaChi.getPhuongXa());
        diaChiAPI.setQuanHuyen(diaChi.getQuanHuyen());
        diaChiAPI.setTinhThanh(diaChi.getTinhThanh());

        diaChiAPI.setLoaiDiaChi(diaChi.getLoaiDiaChi());
        diaChiAPI.setDiaChiMacDinh(diaChi.getDiaChiMacDinh());

        return diaChiAPI;
    }

    private void themDiaChiAPI(Model_DiaChi diaChi, boolean datMacDinh) {
        String token = TokenManager.getToken(this);
        if (token == null) {
            Toast.makeText(this, "Chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo bản copy để gửi API (gộp địa chỉ đầy đủ)
        Model_DiaChi diaChiGuiAPI = taoObjectGuiAPI(diaChi);

        API_AuthService api = API_RetrofitClient.getRetrofitInstance(this).create(API_AuthService.class);
        api.themDiaChi("Bearer " + token, diaChiGuiAPI).enqueue(new Callback<Map<String,String>>() {
            @Override
            public void onResponse(Call<Map<String,String>> call, Response<Map<String,String>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().containsKey("id")) {
                    int idMoi = Integer.parseInt(response.body().get("id"));
                    if (datMacDinh) {
                        api.datDiaChiMacDinh("Bearer " + token, idMoi).enqueue(new Callback<Map<String,String>>() {
                            @Override
                            public void onResponse(Call<Map<String,String>> call, Response<Map<String,String>> response) {
                                Toast.makeText(AddAndressActivity.this, "Đặt mặc định thành công!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                            @Override
                            public void onFailure(Call<Map<String,String>> call, Throwable t) {
                                Toast.makeText(AddAndressActivity.this, "Lỗi cập nhật mặc định!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(AddAndressActivity.this, "Thêm địa chỉ thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(AddAndressActivity.this, "Không thể thêm địa chỉ!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String,String>> call, Throwable t) {
                Toast.makeText(AddAndressActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void xoaDiaChi() {
        String token = TokenManager.getToken(this);
        if (token == null) {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        API_AuthService api = API_RetrofitClient.getRetrofitInstance(this).create(API_AuthService.class);
        api.xoaDiaChi("Bearer " + token, idDiaChiSua).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    // Sau khi xóa thành công
                    Toast.makeText(AddAndressActivity.this, "Xóa thành công!", Toast.LENGTH_SHORT).show();

                    // Chuyển về màn hình EmptyStateActivity
                    Intent intent = new Intent(AddAndressActivity.this, EmptyStateActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } else {
                    Toast.makeText(AddAndressActivity.this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(AddAndressActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}