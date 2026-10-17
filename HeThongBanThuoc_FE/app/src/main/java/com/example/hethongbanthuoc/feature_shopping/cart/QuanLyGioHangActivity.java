package com.example.hethongbanthuoc.feature_shopping.cart;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.core.models.Model_User;
import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.core.utils.TokenManager;
import com.example.hethongbanthuoc.feature_home.Main_HomePageActivity;
import com.example.hethongbanthuoc.feature_shopping.cart.models.Model_ChiTietGioHang;
import com.example.hethongbanthuoc.feature_shopping.order.XacNhanDonHangActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Activity: QuanLyGioHangActivity
 * Màn hình quản lý giỏ hàng với đầy đủ chức năng
 *
 * Các chức năng chính:
 * - Hiển thị danh sách sản phẩm trong giỏ
 * - Chọn/bỏ chọn sản phẩm
 * - Tìm kiếm sản phẩm
 * - Sử dụng điểm thưởng để giảm giá
 * - Tính toán tổng tiền và điểm thưởng
 * - Xử lý thanh toán
 */
public class QuanLyGioHangActivity extends AppCompatActivity implements ChiTietGioHangAdapter.OnItemChangeListener {

    // Views
    private ImageView btnMenu, imggiohang;
    private Button btnkhampha, btnCheckout;
    private RecyclerView rcvCart;
    private ProgressBar progressBar;
    private LinearLayout cartEmpty;
    private EditText edtSearch;
    private TextView tvSoLuongGioHang, tvOriginalPrice, tvSaving, tvDiem, tvDiscountPrice, tvContinueShopping;
    private CheckBox cbSelectAll;
    private SwitchCompat switchUsePoints;
    private RecyclerView rvSanPhamVuaXem;

    // Data
    private ChiTietGioHangAdapter adapter;
    private List<Model_ChiTietGioHang> list = new ArrayList<>();
    private List<Model_ChiTietGioHang> fullList = new ArrayList<>();

    private int maNguoiDung;
    private double tongTienHienTai = 0;
    private API_AuthService apiService;
    private double diemTichLuyHienTai = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_gio_hang);

        // Bước 1: Lấy token & userId
        initializeUser();

        // Bước 2: Khởi tạo Retrofit có token
        Retrofit retrofit = API_RetrofitClient.getRetrofitInstance(this);
        apiService = retrofit.create(API_AuthService.class);

        // Bước 3: Ánh xạ view
        anhXaView();

        // Bước 4: Cấu hình RecyclerView
        setupRecyclerView();

        // Bước 5: Thiết lập sự kiện UI
        setupSuKien();

        // Bước 6: Gọi API lấy danh sách giỏ hàng
        loadGioHang();

        // Bước 7: Gọi API lấy điểm thưởng người dùng
        loadThongTinNguoiDung();
    }

    /**
     * Khởi tạo thông tin người dùng từ token
     */
    private void initializeUser() {
        String token = TokenManager.getToken(this);
        int userId = TokenManager.getUserId(this);

        // Nếu userId chưa có -> giải mã token để lấy
        if (userId == -1 && token != null) {
            try {
                String[] parts = token.split("\\.");
                if (parts.length == 3) {
                    String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE));
                    JSONObject json = new JSONObject(payload);
                    userId = json.optInt("userId", -1);
                    if (userId != -1) {
                        TokenManager.saveUserId(this, userId);
                    }
                }
            } catch (Exception e) {
                Log.e("TOKEN_DECODE", "Lỗi giải mã token: " + e.getMessage());
            }
        }
        maNguoiDung = userId;
    }

    /**
     * Ánh xạ view từ layout
     */
    private void anhXaView() {
        btnMenu = findViewById(R.id.btnMenu);
        imggiohang = findViewById(R.id.imggiohang);
        btnkhampha = findViewById(R.id.btnkhampha);
        rcvCart = findViewById(R.id.rcvCart);
        progressBar = findViewById(R.id.progressBar);
        cartEmpty = findViewById(R.id.cartEmpty);
        edtSearch = findViewById(R.id.edtSearch);
        tvSoLuongGioHang = findViewById(R.id.tvSoLuongGioHang);
        cbSelectAll = findViewById(R.id.cbSelectAll);
        tvContinueShopping = findViewById(R.id.tvContinueShopping);
        tvOriginalPrice = findViewById(R.id.tvOriginalPrice);
        tvSaving = findViewById(R.id.tvSaving);
        tvDiem = findViewById(R.id.tvDiem);
        tvDiscountPrice = findViewById(R.id.tvDiscountPrice);
        switchUsePoints = findViewById(R.id.switchUsePoints);
        btnCheckout = findViewById(R.id.btnCheckout);
        rvSanPhamVuaXem = findViewById(R.id.rvSanPhamVuaXem);
    }

    /**
     * Cấu hình RecyclerView hiển thị danh sách giỏ hàng
     */
    private void setupRecyclerView() {
        rcvCart.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChiTietGioHangAdapter(this, list, this);
        rcvCart.setAdapter(adapter);
    }

    /**
     * Thiết lập các sự kiện UI (nút, checkbox, tìm kiếm, switch)
     */
    private void setupSuKien() {
        // Nút Back - Quay lại trang trước
        btnMenu.setOnClickListener(v -> finish());

        // Chuyển sang màn trang chủ
        tvContinueShopping.setOnClickListener(v -> goToHome());
        btnkhampha.setOnClickListener(v -> goToHome());

        // Chọn / bỏ chọn tất cả sản phẩm
        cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                for (Model_ChiTietGioHang item : list) {
                    item.setSelected(isChecked);
                }
                adapter.notifyDataSetChanged();
                onQuantityChanged();
            }
        });

        // Tìm kiếm sản phẩm trong giỏ
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCart(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Bật / tắt sử dụng điểm thưởng
        switchUsePoints.setOnCheckedChangeListener((buttonView, isChecked) -> {
            handleSwitchUsePoints(isChecked);
        });

        // Xử lý thanh toán
        btnCheckout.setOnClickListener(v -> xuLyThanhToan());
    }

    /**
     * Xử lý logic khi bật/tắt switch sử dụng điểm thưởng
     */
    private void handleSwitchUsePoints(boolean isChecked) {
        // Kiểm tra có sản phẩm được chọn không
        boolean coSanPhamChon = false;
        for (Model_ChiTietGioHang item : list) {
            if (item.isSelected()) {
                coSanPhamChon = true;
                break;
            }
        }

        if (isChecked) {
            // Kiểm tra phải chọn sản phẩm
            if (!coSanPhamChon) {
                Toast.makeText(this, "Bạn phải chọn ít nhất 1 sản phẩm để sử dụng điểm thưởng!", Toast.LENGTH_SHORT).show();
                switchUsePoints.setChecked(false);
                return;
            }

            // Kiểm tra điểm tối thiểu
            if (diemTichLuyHienTai < 50) {
                Toast.makeText(this, "Cần ít nhất 50 điểm để sử dụng!", Toast.LENGTH_SHORT).show();
                switchUsePoints.setChecked(false);
                return;
            }

            Toast.makeText(this, "Đang dùng " + String.format("%.0f", diemTichLuyHienTai) + " điểm để giảm giá!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Đã tắt sử dụng điểm thưởng", Toast.LENGTH_SHORT).show();
        }

        capNhatHienThiTongTien();
    }

    /**
     * Chuyển về trang chủ
     */
    private void goToHome() {
        Intent intent = new Intent(this, Main_HomePageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    /**
     * Gọi API lấy giỏ hàng theo người dùng
     */
    private void loadGioHang() {
        progressBar.setVisibility(View.VISIBLE);
        String token = "Bearer " + TokenManager.getToken(this);

        apiService.getGioHangTheoNguoiDung(token, maNguoiDung).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                progressBar.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null) {
                    showEmptyCart();
                    return;
                }

                Map<String, Object> body = response.body();
                List<Map<String, Object>> chiTietList = (List<Map<String, Object>>) body.get("chiTiet");

                if (chiTietList == null || chiTietList.isEmpty()) {
                    showEmptyCart();
                    return;
                }

                list.clear();
                fullList.clear();

                for (Map<String, Object> item : chiTietList) {
                    Model_ChiTietGioHang ct = Model_ChiTietGioHang.fromMap(item);
                    list.add(ct);
                    fullList.add(ct);
                }

                adapter.notifyDataSetChanged();
                cartEmpty.setVisibility(View.GONE);
                rcvCart.setVisibility(View.VISIBLE);
                capNhatSoLuongIconGio(list.size());
                onQuantityChanged();
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("LOAD_CART", "Lỗi tải giỏ hàng: " + t.getMessage());
                showEmptyCart();
                Toast.makeText(QuanLyGioHangActivity.this, "Không thể tải giỏ hàng. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Hiển thị giỏ hàng trống
     */
    private void showEmptyCart() {
        list.clear();
        fullList.clear();
        adapter.notifyDataSetChanged();

        // Reset các giá trị
        tvDiscountPrice.setText("0 đ");
        tvOriginalPrice.setText("0 đ");
        tvSaving.setText("0 đ");
        tvDiem.setText("Bạn có +" + String.format("%.0f", diemTichLuyHienTai) + " điểm");

        // Hiển thị empty state
        cartEmpty.setVisibility(View.VISIBLE);
        rcvCart.setVisibility(View.GONE);
        capNhatSoLuongIconGio(0);

        // Tắt switch và disable
        switchUsePoints.setChecked(false);
        switchUsePoints.setEnabled(false);
    }

    /**
     * Cập nhật số lượng sản phẩm hiển thị trên icon badge
     */
    private void capNhatSoLuongIconGio(int soLuong) {
        if (soLuong > 0) {
            tvSoLuongGioHang.setVisibility(View.VISIBLE);
            tvSoLuongGioHang.setText(String.valueOf(soLuong));
        } else {
            tvSoLuongGioHang.setVisibility(View.GONE);
        }
    }

    /**
     * Lọc giỏ hàng theo từ khóa tìm kiếm
     */
    private void filterCart(String keyword) {
        list.clear();

        if (keyword.isEmpty()) {
            list.addAll(fullList);
        } else {
            for (Model_ChiTietGioHang item : fullList) {
                if (item.getTenSanPham() != null &&
                        item.getTenSanPham().toLowerCase().contains(keyword.toLowerCase())) {
                    list.add(item);
                }
            }
        }

        adapter.notifyDataSetChanged();

        // Hiển thị empty nếu không tìm thấy
        if (list.isEmpty()) {
            cartEmpty.setVisibility(View.VISIBLE);
            rcvCart.setVisibility(View.GONE);
        } else {
            cartEmpty.setVisibility(View.GONE);
            rcvCart.setVisibility(View.VISIBLE);
        }

        capNhatSoLuongIconGio(list.size());
        onQuantityChanged();
    }

    /**
     * Xử lý khi người dùng nhấn nút Thanh Toán
     */
    private void xuLyThanhToan() {
        List<Model_ChiTietGioHang> selectedList = new ArrayList<>();
        double tongTien = 0;

        for (Model_ChiTietGioHang item : list) {
            if (item.isSelected()) {
                selectedList.add(item);
                tongTien += item.getDonGia() * item.getSoLuong();
            }
        }

        if (selectedList.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 sản phẩm để tiếp tục!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, XacNhanDonHangActivity.class);
        intent.putExtra("maNguoiDung", maNguoiDung);
        intent.putExtra("tongTien", tongTien);
        intent.putExtra("diemTichLuyHienTai", diemTichLuyHienTai);
        intent.putExtra("suDungDiem", switchUsePoints.isChecked());
        intent.putExtra("listSanPham", new ArrayList<>(selectedList));
        startActivity(intent);
    }

    /**
     * Callback khi số lượng sản phẩm thay đổi
     */
    @Override
    public void onQuantityChanged() {
        tongTienHienTai = 0;
        boolean allSelected = true;
        int countSelected = 0;

        for (Model_ChiTietGioHang item : list) {
            if (item.isSelected()) {
                tongTienHienTai += item.getDonGia() * item.getSoLuong();
                countSelected++;
            } else {
                allSelected = false;
            }
        }

        // Cập nhật checkbox "Chọn tất cả"
        cbSelectAll.setOnCheckedChangeListener(null);
        cbSelectAll.setChecked(allSelected && !list.isEmpty());
        cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                for (Model_ChiTietGioHang it : list) {
                    it.setSelected(isChecked);
                }
                adapter.notifyDataSetChanged();
                onQuantityChanged();
            }
        });

        // Kiểm tra nếu không còn sản phẩm nào được chọn -> tắt switch
        if (countSelected == 0 && switchUsePoints.isChecked()) {
            switchUsePoints.setChecked(false);
        }

        capNhatHienThiTongTien();
        capNhatSoLuongIconGio(list.size());

        // Kiểm tra nếu list rỗng -> hiển thị empty
        if (list.isEmpty()) {
            showEmptyCart();
        }
    }

    /**
     * Callback khi trạng thái chọn sản phẩm thay đổi
     */
    @Override
    public void onItemSelectionChanged() {
        onQuantityChanged();
    }

    /**
     * Cập nhật phần hiển thị tổng tiền và điểm thưởng
     */
    private void capNhatHienThiTongTien() {
        // Đếm số sản phẩm được chọn
        int soLuongChon = 0;
        for (Model_ChiTietGioHang item : list) {
            if (item.isSelected()) {
                soLuongChon++;
            }
        }

        // Xử lý enable/disable switch
        if (soLuongChon == 0) {
            if (switchUsePoints.isChecked()) {
                switchUsePoints.setChecked(false);
                Toast.makeText(this, "Bạn phải chọn ít nhất 1 sản phẩm để dùng điểm thưởng!", Toast.LENGTH_SHORT).show();
            }
            switchUsePoints.setEnabled(false);
        } else {
            switchUsePoints.setEnabled(diemTichLuyHienTai >= 50);
        }

        // Tính toán
        double tongTien = tongTienHienTai;
        double giamGia = 0;

        if (switchUsePoints.isChecked() && diemTichLuyHienTai >= 50) {
            giamGia = Math.min(diemTichLuyHienTai, tongTien);
        }

        double tongSauGiam = tongTien - giamGia;
        double diemThuongMoi = tongSauGiam * 0.02; // 2% giá trị đơn hàng

        // Hiển thị
        tvOriginalPrice.setText(String.format("%,.0f đ", tongTien));
        tvDiscountPrice.setText(String.format("%,.0f đ", tongSauGiam));
        tvSaving.setText(String.format("%,.0f đ", giamGia));

        // Hiển thị điểm hiện có và điểm sẽ nhận
        if (soLuongChon > 0) {
            tvDiem.setText(String.format("Bạn có +%.0f điểm (Nhận thêm +%.0f)", diemTichLuyHienTai, diemThuongMoi));
        } else {
            tvDiem.setText(String.format("Bạn có +%.0f điểm", diemTichLuyHienTai));
        }
    }

    /**
     * Gọi API lấy thông tin người dùng (điểm thưởng)
     */
    private void loadThongTinNguoiDung() {
        String token = "Bearer " + TokenManager.getToken(this);

        apiService.getProfile(token).enqueue(new Callback<Model_User>() {
            @Override
            public void onResponse(Call<Model_User> call, Response<Model_User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    diemTichLuyHienTai = response.body().getDiemTichLuy();

                    // Cập nhật hiển thị điểm
                    tvDiem.setText(String.format("Bạn có +%.0f điểm", diemTichLuyHienTai));

                    // Enable/disable switch dựa vào điểm
                    if (diemTichLuyHienTai < 50) {
                        switchUsePoints.setEnabled(false);
                    }
                }
            }

            @Override
            public void onFailure(Call<Model_User> call, Throwable t) {
                Log.e("USER_INFO", "Lỗi lấy thông tin người dùng: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload giỏ hàng khi quay lại màn hình
        loadGioHang();
        loadThongTinNguoiDung();
    }
}