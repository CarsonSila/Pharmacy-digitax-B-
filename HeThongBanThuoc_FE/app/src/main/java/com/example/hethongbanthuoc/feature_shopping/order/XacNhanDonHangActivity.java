package com.example.hethongbanthuoc.feature_shopping.order;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hethongbanthuoc.feature_home.Main_HomePageActivity;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.core.models.Model_User;
import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.core.utils.TokenManager;
import com.example.hethongbanthuoc.feature_auth.login.LoginActivity;
import com.example.hethongbanthuoc.feature_profile.address.Adapter_Address;
import com.example.hethongbanthuoc.feature_profile.models.Model_DiaChi;
import com.example.hethongbanthuoc.feature_shopping.cart.models.Model_ChiTietGioHang;
import com.example.hethongbanthuoc.feature_shopping.order.utils.RecyclerItemClickListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class XacNhanDonHangActivity extends AppCompatActivity {

    // ========== VIEWS ==========
    private ImageView btnBack;
    private TextView tvHinhThuc, txtTotalPrice, txtFinalPrice, txtDiaChi, tvThayDoiDiaChi;
    private TextView tvThoiGianDuKien, tvTenNguoiNhan, tvSoDienThoai, tvThayDoiThoiGian;
    private TextView tvTongDiem, txtDirectDiscount, txtDiemThuong;
    private Button btnThanhToan;
    private RecyclerView rvProducts;
    private Switch switchDiscount;

    private List<Model_ChiTietGioHang> sanPhamList = new ArrayList<>();
    private Model_DiaChi diaChiDaChon;
    private API_AuthService apiService;
    private String token;
    private Integer maNguoiDung;
    private String hinhThucDaChon = "Giao hàng tận nơi";
    private double tongTien = 0;
    private int diemThuongHienCo = 0;
    private int diemThuongMoi = 0;
    private DecimalFormat df = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanh_toan);

        initViews();
        initApiService();

        if (!checkLogin()) return;

        loadProductsFromIntent();
        setupRecyclerView();
        setupClickListeners();
        setDefaultDeliveryTime();
        loadUserData();
    }

  // Khởi tạo view

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvHinhThuc = findViewById(R.id.tvhinhthuc);
        btnThanhToan = findViewById(R.id.btnthanhtoan);
        rvProducts = findViewById(R.id.rvProducts);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        txtFinalPrice = findViewById(R.id.txtFinalPrice);
        txtDiaChi = findViewById(R.id.txtDeliveryAddress);
        tvThayDoiDiaChi = findViewById(R.id.tvThayDoiDiaChi);
        tvTenNguoiNhan = findViewById(R.id.txtRecipientName);
        tvSoDienThoai = findViewById(R.id.txtRecipientPhone);
        tvThoiGianDuKien = findViewById(R.id.tvThoiGianDuKien);
        tvThayDoiThoiGian = findViewById(R.id.tvThayDoiThoiGian);
        txtDiemThuong = findViewById(R.id.txtPoints);
        switchDiscount = findViewById(R.id.switchDiscount);
        tvTongDiem = findViewById(R.id.tvTongDiem);
        txtDirectDiscount = findViewById(R.id.txtDirectDiscount);
    }

    private void initApiService() {
        apiService = API_RetrofitClient.getRetrofitInstance(this).create(API_AuthService.class);
        token = TokenManager.getToken(this);
        maNguoiDung = TokenManager.getUserId(this);

        Log.d("XacNhanDonHang", "Token: " + token);
        Log.d("XacNhanDonHang", "UserId: " + maNguoiDung);
    }

    private boolean checkLogin() {
        if (token == null || token.isEmpty() || maNguoiDung == -1) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return false;
        }
        return true;
    }

    private void loadProductsFromIntent() {
        Intent intent = getIntent();
        if (intent == null) return;

        // Load từ giỏ hàng (nhiều sản phẩm)
        if (intent.hasExtra("listSanPham")) {
            sanPhamList = (List<Model_ChiTietGioHang>) intent.getSerializableExtra("listSanPham");
        }
        // Mua ngay
        else if (intent.hasExtra("maSanPham")) {
            Model_ChiTietGioHang sp = new Model_ChiTietGioHang();
            sp.setMaSanPham(intent.getIntExtra("maSanPham", -1));
            sp.setTenSanPham(intent.getStringExtra("tenThuoc"));
            sp.setDonGia(intent.getDoubleExtra("donGia", 0));
            sp.setSoLuong(intent.getIntExtra("soLuong", 1));
            sp.setHinhAnh(intent.getStringExtra("hinhAnh"));
            sanPhamList.clear();
            sanPhamList.add(sp);
        }

        // Sửa link ảnh Google Drive
        fixGoogleDriveLinks();
    }

    private void fixGoogleDriveLinks() {
        for (Model_ChiTietGioHang sp : sanPhamList) {
            String imageUrl = sp.getHinhAnh();
            if (imageUrl != null && imageUrl.contains("drive.google.com") && imageUrl.contains("export=view")) {
                sp.setHinhAnh(imageUrl.replace("export=view", "export=download"));
            }
        }
    }

    private void setupRecyclerView() {
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(new XacNhanAdapter(this, sanPhamList));
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        tvHinhThuc.setOnClickListener(v -> showPopupChonHinhThuc());
        tvThayDoiThoiGian.setOnClickListener(v -> showPopupChonThoiGian());
        tvThayDoiDiaChi.setOnClickListener(v -> fetchAndShowDiaChiList());
        btnThanhToan.setOnClickListener(v -> thanhToanDonHang());

        switchDiscount.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && diemThuongHienCo < 50) {
                Toast.makeText(this, "Bạn cần ít nhất 50 điểm để sử dụng!", Toast.LENGTH_SHORT).show();
                switchDiscount.setChecked(false);
            } else {
                tinhTongTien();
            }
        });
    }

    private void setDefaultDeliveryTime() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 4);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
        tvThoiGianDuKien.setText("Dự kiến giao vào " + sdf.format(c.getTime()));
    }

    // Load dữ liệu

    private void loadUserData() {
        loadDiaChiNguoiDung();
        loadUserProfile();
    }

    private void loadDiaChiNguoiDung() {
        SharedPreferences prefs = getSharedPreferences("DiaChiPrefs", MODE_PRIVATE);
        int maDiaChi = prefs.getInt("maDiaChi", -1);

        if (maDiaChi != -1) {
            diaChiDaChon = new Model_DiaChi();
            diaChiDaChon.setMaDiaChi(maDiaChi);
            diaChiDaChon.setDiaChiChiTiet(prefs.getString("diaChiChiTiet", ""));
            diaChiDaChon.setPhuongXa(prefs.getString("phuongXa", ""));
            diaChiDaChon.setQuanHuyen(prefs.getString("quanHuyen", ""));
            diaChiDaChon.setTinhThanh(prefs.getString("tinhThanh", ""));
            diaChiDaChon.setTenNguoiNhan(prefs.getString("tenNguoiNhan", ""));
            diaChiDaChon.setSoDienThoai(prefs.getString("soDienThoai", ""));

            updateDiaChiUI(diaChiDaChon);
            Log.d("XacNhanDonHang", "Load địa chỉ từ SharedPreferences: " + maDiaChi);
            return;
        }

        // Nếu chưa có, gọi API
        fetchDanhSachDiaChiDefault();
    }

    private void fetchDanhSachDiaChiDefault() {
        if (token == null || token.isEmpty()) {
            txtDiaChi.setText("Chưa đăng nhập");
            return;
        }

        apiService.getDanhSachDiaChi("Bearer " + token)
                .enqueue(new Callback<List<Model_DiaChi>>() {
                    @Override
                    public void onResponse(Call<List<Model_DiaChi>> call, Response<List<Model_DiaChi>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            diaChiDaChon = response.body().get(0);
                            updateDiaChiUI(diaChiDaChon);
                            saveDiaChiToPreferences(diaChiDaChon);
                        } else {
                            txtDiaChi.setText("Không có địa chỉ giao hàng");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Model_DiaChi>> call, Throwable t) {
                        txtDiaChi.setText("Lỗi tải địa chỉ");
                    }
                });
    }

    private void loadUserProfile() {
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getProfile("Bearer " + token).enqueue(new Callback<Model_User>() {
            @Override
            public void onResponse(Call<Model_User> call, Response<Model_User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Model_User user = response.body();

                    tvTenNguoiNhan.setText(user.getHoTen() != null ? user.getHoTen() : "Chưa có tên");
                    tvSoDienThoai.setText(user.getSoDienThoai() != null ? user.getSoDienThoai() : "Chưa cập nhật");

                    diemThuongHienCo = user.getDiemTichLuy();
                    tvTongDiem.setText("Điểm tích lũy: " + diemThuongHienCo + " điểm");

                    switchDiscount.setEnabled(diemThuongHienCo >= 50);
                    tinhTongTien();
                } else {
                    tvTenNguoiNhan.setText("Không thể tải tên người nhận");
                    tvSoDienThoai.setText("-");
                    tvTongDiem.setText("Điểm tích lũy: 0 điểm");
                }
            }

            @Override
            public void onFailure(Call<Model_User> call, Throwable t) {
                tvTenNguoiNhan.setText("Lỗi tải thông tin");
                tvSoDienThoai.setText("-");
                tvTongDiem.setText("Điểm tích lũy: 0 điểm");
            }
        });
    }

    // Thanh toán đơn hàng

    private void thanhToanDonHang() {
        if (diaChiDaChon == null) {
            Toast.makeText(this, "Vui lòng chọn địa chỉ giao hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        final int diemSuDung = (switchDiscount.isChecked() && diemThuongHienCo >= 50)
                ? Math.min(diemThuongHienCo, (int) tongTien) : 0;
        final double finalPrice = Math.max(tongTien - diemSuDung, 0);
        diemThuongMoi = (int) (finalPrice * 0.02);

        // TẠO ĐỐI TƯỢNG REQUEST ĐÚNG FORMAT
        Map<String, Object> donHang = new HashMap<>();
        donHang.put("maNguoiDung", maNguoiDung);
        donHang.put("maDiaChi", diaChiDaChon.getMaDiaChi());
        donHang.put("maGiamGia", null);  // Nếu không dùng mã giảm giá
        donHang.put("tongTien", tongTien);
        donHang.put("giamGia", (double) diemSuDung);
        donHang.put("tienPhaiTra", finalPrice);
        donHang.put("suDungDiemThuong", switchDiscount.isChecked());
        donHang.put("diemDaDung", diemSuDung);

        // TẠO DANH SÁCH CHI TIẾT ĐƠN HÀNG
        List<Map<String, Object>> chiTietDonHangList = new ArrayList<>();
        for (Model_ChiTietGioHang sp : sanPhamList) {
            Map<String, Object> chiTiet = new HashMap<>();
            chiTiet.put("maSanPham", sp.getMaSanPham());
            chiTiet.put("soLuong", sp.getSoLuong());
            chiTiet.put("donGia", sp.getDonGia());
            chiTiet.put("thanhTien", sp.getDonGia() * sp.getSoLuong());
            chiTietDonHangList.add(chiTiet);
        }

        // THÊM DANH SÁCH CHI TIẾT VÀO ĐƠN HÀNG (KEY ĐÚNG)
        donHang.put("chiTietDonHangList", chiTietDonHangList);

        // Log để debug
        String jsonData = new com.google.gson.Gson().toJson(donHang);
        Log.d("ĐƠN_HÀNG", "DATA GỬI LÊN SERVER: " + jsonData);
        Log.d("ĐƠN_HÀNG", "Số lượng sản phẩm: " + chiTietDonHangList.size());

        // GỌI API VỚI TOKEN
        apiService.taoDonHang("Bearer " + token, donHang).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> result = response.body();
                    Boolean success = (Boolean) result.get("success");

                    if (success != null && success) {
                        Object maDonHangObj = result.get("maDonHang");
                        int maDonHang = maDonHangObj instanceof Number
                                ? ((Number) maDonHangObj).intValue()
                                : Integer.parseInt(maDonHangObj.toString());

                        Log.d("ĐƠN_HÀNG", "Tạo đơn hàng thành công! Mã đơn: " + maDonHang);
                        xoaSanPhamKhoiGioHang(diemSuDung);
                    } else {
                        String message = (String) result.get("message");
                        Toast.makeText(XacNhanDonHangActivity.this,
                                message != null ? message : "Tạo đơn hàng thất bại!",
                                Toast.LENGTH_SHORT).show();
                        Log.e("ĐƠN_HÀNG", "Lỗi: " + message);
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Không có phản hồi";
                        Log.e("ĐƠN_HÀNG", "LỖI SERVER (Code: " + response.code() + "): " + errorBody);
                        Toast.makeText(XacNhanDonHangActivity.this,
                                "Lỗi server: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(XacNhanDonHangActivity.this, "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
                Log.e("ĐƠN_HÀNG", "Lỗi kết nối: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

     // Gọi API - Xóa sản phẩm đã mua khỏi giỏ hàng
    private void xoaSanPhamKhoiGioHang(int diemSuDung) {
        for (Model_ChiTietGioHang sp : sanPhamList) {
            apiService.deleteChiTietGioHang(sp.getMaCTGH())
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            Log.d("XoaGioHang", "Xóa sản phẩm thành công");
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e("XoaGioHang", "Lỗi: " + t.getMessage());
                        }
                    });
        }

        // Chuyển về MainActivity sau khi xóa
        chuyenVeTrangChu(diemSuDung);
    }

     // Chuyển về trang chủ sau khi thanh toán thành công
    private void chuyenVeTrangChu(int diemSuDung) {
        Toast.makeText(this,
                "Đặt hàng thành công!\n" +
                        (diemSuDung > 0 ? "Đã trừ " + diemSuDung + " điểm.\n" : "") +
                        "Bạn được cộng " + diemThuongMoi + " điểm!",
                Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, Main_HomePageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // Hàm API - Load danh sác địa chỉ của người dùng

    private void fetchAndShowDiaChiList() {
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để chọn địa chỉ!", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getDanhSachDiaChi("Bearer " + token)
                .enqueue(new Callback<List<Model_DiaChi>>() {
                    @Override
                    public void onResponse(Call<List<Model_DiaChi>> call, Response<List<Model_DiaChi>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Model_DiaChi> danhSachMoi = response.body();
                            if (danhSachMoi.isEmpty()) {
                                Toast.makeText(XacNhanDonHangActivity.this,
                                        "Bạn chưa có địa chỉ nào. Vui lòng thêm mới!",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                showDiaChiPopup(danhSachMoi);
                            }
                        } else {
                            Toast.makeText(XacNhanDonHangActivity.this,
                                    "Không thể tải danh sách địa chỉ!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Model_DiaChi>> call, Throwable t) {
                        Toast.makeText(XacNhanDonHangActivity.this,
                                "Lỗi kết nối khi tải địa chỉ!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateDiaChiUI(Model_DiaChi dc) {
        txtDiaChi.setText(dc.getDiaChiChiTiet() + ", " + dc.getPhuongXa() + ", " +
                dc.getQuanHuyen() + ", " + dc.getTinhThanh());
        tvTenNguoiNhan.setText(dc.getTenNguoiNhan());
        tvSoDienThoai.setText(dc.getSoDienThoai());
    }

    private void saveDiaChiToPreferences(Model_DiaChi dc) {
        SharedPreferences.Editor editor = getSharedPreferences("DiaChiPrefs", MODE_PRIVATE).edit();
        editor.putInt("maDiaChi", dc.getMaDiaChi());
        editor.putString("diaChiChiTiet", dc.getDiaChiChiTiet());
        editor.putString("phuongXa", dc.getPhuongXa());
        editor.putString("quanHuyen", dc.getQuanHuyen());
        editor.putString("tinhThanh", dc.getTinhThanh());
        editor.putString("tenNguoiNhan", dc.getTenNguoiNhan());
        editor.putString("soDienThoai", dc.getSoDienThoai());
        editor.apply();
    }

    // popop

    private void showDiaChiPopup(List<Model_DiaChi> diaChiList) {
        if (isFinishing() || isDestroyed()) return;

        View view = LayoutInflater.from(this).inflate(R.layout.popup_chon_diachi, null);
        RecyclerView recyclerView = view.findViewById(R.id.rvDiaChi);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new Adapter_Address(this, diaChiList));

        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Model_DiaChi dc = diaChiList.get(position);
                        if (dc == null) return;

                        diaChiDaChon = dc;
                        updateDiaChiUI(dc);
                        saveDiaChiToPreferences(dc);
                        dialog.dismiss();
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {}
                }));

        dialog.show();
    }

    private void showPopupChonHinhThuc() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        LinearLayout view = (LinearLayout) LayoutInflater.from(this)
                .inflate(R.layout.popup_chon_hinhthuc, null);
        dialog.setContentView(view);

        view.findViewById(R.id.optionGiaoHang).setOnClickListener(v -> hinhThucDaChon = "Giao hàng tận nơi");
        view.findViewById(R.id.optionNhanTaiCuaHang).setOnClickListener(v -> hinhThucDaChon = "Nhận tại cửa hàng");

        ((Button) view.findViewById(R.id.btnXacNhanHinhThuc)).setOnClickListener(v -> {
            tvHinhThuc.setText(hinhThucDaChon);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showPopupChonThoiGian() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        LinearLayout view = (LinearLayout) LayoutInflater.from(this)
                .inflate(R.layout.dialog_chon_thoi_gian_giao_hang, null);
        dialog.setContentView(view);

        TextView txtThoiGianDuKien = view.findViewById(R.id.txtThoiGianDuKien);
        Button btnXacNhan = view.findViewById(R.id.btnXacNhanThoiGian);

        Button[] btnNgayArr = {view.findViewById(R.id.btnNgay1), view.findViewById(R.id.btnNgay2),
                view.findViewById(R.id.btnNgay3), view.findViewById(R.id.btnNgay4)};

        SimpleDateFormat sdfNgay = new SimpleDateFormat("EEE, dd/MM/yyyy", new Locale("vi", "VN"));
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < btnNgayArr.length; i++) {
            Calendar temp = (Calendar) calendar.clone();
            temp.add(Calendar.DAY_OF_MONTH, i);
            btnNgayArr[i].setText((i == 0 ? "Hôm nay, " : "") + sdfNgay.format(temp.getTime()));
        }

        final String[] ngayNhan = {btnNgayArr[3].getText().toString()};
        final String[] gioNhan = {""};

        for (Button btn : btnNgayArr) {
            btn.setOnClickListener(v -> {
                ngayNhan[0] = btn.getText().toString();
                txtThoiGianDuKien.setText("Thời gian dự kiến: " + (gioNhan[0].isEmpty() ? "" : gioNhan[0] + " ") + ngayNhan[0]);
            });
        }

        int[] gioIds = {R.id.btn15_16, R.id.btn16_17, R.id.btn17_18, R.id.btn18_19, R.id.btn19_20, R.id.btn20_21};
        for (int id : gioIds) {
            Button btn = view.findViewById(id);
            btn.setOnClickListener(v -> {
                gioNhan[0] = btn.getText().toString();
                txtThoiGianDuKien.setText("Thời gian dự kiến: " + gioNhan[0] + " " + ngayNhan[0]);
            });
        }

        btnXacNhan.setOnClickListener(v -> {
            if (gioNhan[0].isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn giờ nhận hàng", Toast.LENGTH_SHORT).show();
                return;
            }
            tvThoiGianDuKien.setText("Từ " + gioNhan[0] + " " + ngayNhan[0]);
            dialog.dismiss();
        });

        dialog.show();
    }

    // Hàm tính tổng tiền

    private void tinhTongTien() {
        tongTien = 0;
        for (Model_ChiTietGioHang sp : sanPhamList) {
            tongTien += sp.getDonGia() * sp.getSoLuong();
        }

        int diemSuDung = (switchDiscount.isChecked() && diemThuongHienCo >= 50)
                ? Math.min(diemThuongHienCo, (int) tongTien) : 0;

        double finalPrice = Math.max(tongTien - diemSuDung, 0);
        diemThuongMoi = (int) (finalPrice * 0.02);

        txtTotalPrice.setText(df.format(tongTien) + "đ");
        txtFinalPrice.setText(df.format(finalPrice) + "đ");
        txtDirectDiscount.setText("-" + df.format(diemSuDung) + "đ");
        txtDiemThuong.setText("+" + diemThuongMoi + " điểm");
    }
}