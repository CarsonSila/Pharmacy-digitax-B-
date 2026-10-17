package com.example.hethongbanthuoc.feature_shopping.order;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.core.utils.TokenManager;
import com.example.hethongbanthuoc.feature_chat.chat.ChatActivity;
import com.example.hethongbanthuoc.feature_shopping.order.models.Model_ChiTietDonHangRequest;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity {

    private static final String TAG = "OrderDetailActivity";

    // Views
    private ImageView btnBack;
    private RelativeLayout headerLayout;
    private TextView tvHeaderTitle, tvStatusMessage;
    private ImageView imgStatusIllustration;

    private TextView tvMaDonHang, btnCopy;
    private TextView tvTenNguoiNhan, tvSoDienThoai, tvDiaChiNhanHang;
    private TextView tvTongTien, tvGiamGia, tvThanhTien, tvDiemThuong;

    private RecyclerView recyclerViewProducts;
    private Adapter_ProductOrderDetail productAdapter;

    private Button btnHoTro, btnMuaLai;
    private ProgressBar progressBar;

    // Data
    private int maDonHang;
    private API_AuthService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Lấy mã đơn hàng từ Intent
        maDonHang = getIntent().getIntExtra("maDonHang", -1);

        if (maDonHang == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo views
        initViews();

        // Setup API
        apiService = API_RetrofitClient.getRetrofitInstance(this).create(API_AuthService.class);

        // Setup RecyclerView
        setupRecyclerView();

        // Load chi tiết đơn hàng
        loadOrderDetail();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        headerLayout = findViewById(R.id.headerLayout);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvStatusMessage = findViewById(R.id.tvStatusMessage);
        imgStatusIllustration = findViewById(R.id.imgStatusIllustration);

        tvMaDonHang = findViewById(R.id.tvMaDonHang);
        btnCopy = findViewById(R.id.btnCopy);

        tvTenNguoiNhan = findViewById(R.id.tvTenNguoiNhan);
        tvSoDienThoai = findViewById(R.id.tvSoDienThoai);
        tvDiaChiNhanHang = findViewById(R.id.tvDiaChiNhanHang);

        tvTongTien = findViewById(R.id.tvTongTien);
        tvGiamGia = findViewById(R.id.tvGiamGia);
        tvThanhTien = findViewById(R.id.tvThanhTien);
        tvDiemThuong = findViewById(R.id.tvDiemThuong);

        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);

        btnHoTro = findViewById(R.id.btnHoTro);
        btnMuaLai = findViewById(R.id.btnMuaLai);
        progressBar = findViewById(R.id.progressBar);

        // Click listeners
        btnBack.setOnClickListener(v -> finish());

        btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Mã đơn hàng", tvMaDonHang.getText().toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Đã sao chép mã đơn hàng", Toast.LENGTH_SHORT).show();
        });

        btnHoTro.setOnClickListener(v -> {
            Intent intent = new Intent(OrderDetailActivity.this, ChatActivity.class);
            startActivity(intent);
        });

        btnMuaLai.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng mua lại đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerView() {
        productAdapter = new Adapter_ProductOrderDetail(this);
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProducts.setAdapter(productAdapter);
    }

    private void loadOrderDetail() {
        progressBar.setVisibility(View.VISIBLE);

        String token = "Bearer " + TokenManager.getToken(this);

        Log.d(TAG, "Loading chi tiết đơn hàng #" + maDonHang);

        apiService.getChiTietDonHang(token, maDonHang).enqueue(new Callback<Model_ChiTietDonHangRequest>() {
            @Override
            public void onResponse(Call<Model_ChiTietDonHangRequest> call, Response<Model_ChiTietDonHangRequest> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    Model_ChiTietDonHangRequest chiTiet = response.body();
                    Log.d(TAG, "Load thành công chi tiết đơn hàng");
                    displayOrderDetail(chiTiet);
                } else {
                    Log.e(TAG, "Response không thành công: " + response.code());
                    Toast.makeText(OrderDetailActivity.this, "Lỗi khi tải chi tiết đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Model_ChiTietDonHangRequest> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Lỗi API: " + t.getMessage());
                Toast.makeText(OrderDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayOrderDetail(Model_ChiTietDonHangRequest chiTiet) {
        // Cập nhật header theo trạng thái
        updateHeaderByStatus(chiTiet.getTrangThai());

        // Hiển thị nút theo trạng thái
        updateButtonsByStatus(chiTiet.getTrangThai());

        // Mã đơn hàng
        tvMaDonHang.setText("#" + chiTiet.getMaDonHang());

        // Thông tin người nhận
        tvTenNguoiNhan.setText(chiTiet.getTenNguoiNhan());
        tvSoDienThoai.setText(formatPhoneNumber(chiTiet.getSoDienThoai()));
        tvDiaChiNhanHang.setText(chiTiet.getDiaChiDayDu());

        // Thông tin thanh toán
        tvTongTien.setText(formatMoney(chiTiet.getTongTien()));
        tvGiamGia.setText(formatMoney(chiTiet.getGiamGia()));
        tvThanhTien.setText(formatMoney(chiTiet.getTienPhaiTra()));

        // Điểm thưởng (giả sử = tiền phải trả / 1000)
        int diemThuong = (int) (chiTiet.getTienPhaiTra() / 1000);
        tvDiemThuong.setText("+" + diemThuong);

        // Danh sách sản phẩm
        if (chiTiet.getDanhSachSanPham() != null && !chiTiet.getDanhSachSanPham().isEmpty()) {
            productAdapter.setData(chiTiet.getDanhSachSanPham());
        }
    }

    private void updateHeaderByStatus(String trangThai) {
        switch (trangThai) {
            case "Đang xử lý":
                tvHeaderTitle.setText("Đang xử lý");
                tvStatusMessage.setText("Đơn hàng của bạn đang được chuẩn bị.");
                imgStatusIllustration.setImageResource(R.drawable.order_processing);
                break;

            case "Đang giao":
                tvHeaderTitle.setText("Đang giao");
                tvStatusMessage.setText("Đơn hàng đang được giao đến bạn.");
                imgStatusIllustration.setImageResource(R.drawable.order_shipping);
                break;

            case "Đã giao":
                tvHeaderTitle.setText("Đã giao");
                tvStatusMessage.setText("Rất vui vì bạn đã tin tưởng và mua hàng.");
                imgStatusIllustration.setImageResource(R.drawable.order_delivered);
                break;

            case "Đã hủy":
                tvHeaderTitle.setText("Đã hủy");
                tvStatusMessage.setText("Đơn hàng đã bị hủy.");
                imgStatusIllustration.setImageResource(R.drawable.order_cancelled);
                break;

            default:
                tvHeaderTitle.setText(trangThai);
                tvStatusMessage.setText("Trạng thái đơn hàng: " + trangThai);
                imgStatusIllustration.setImageResource(R.drawable.order_default);
                break;
        }
    }


    /*
     * Cập nhật hiển thị nút theo trạng thái đơn hàng
     * - Đã giao: Hiển thị cả 2 nút (Hỗ trợ + Mua lại)
     * - Các trạng thái khác: Chỉ hiển thị nút Hỗ trợ (full width)
     */
    private void updateButtonsByStatus(String trangThai) {
        if ("Đã giao".equals(trangThai)) {
            // Hiển thị cả 2 nút
            btnHoTro.setVisibility(View.VISIBLE);
            btnMuaLai.setVisibility(View.VISIBLE);

            // Đặt lại margin cho nút Hỗ trợ
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) btnHoTro.getLayoutParams();
            params.setMarginEnd((int) (8 * getResources().getDisplayMetrics().density));
            btnHoTro.setLayoutParams(params);
        } else {
            // Chỉ hiển thị nút Hỗ trợ (full width)
            btnHoTro.setVisibility(View.VISIBLE);
            btnMuaLai.setVisibility(View.GONE);

            // Bỏ margin để nút Hỗ trợ full width
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) btnHoTro.getLayoutParams();
            params.setMarginEnd(0);
            btnHoTro.setLayoutParams(params);
        }
    }

    private String formatMoney(double amount) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(amount) + "đ";
    }

    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.length() < 10) return phone;
        return phone.substring(0, 4) + " " + phone.substring(4, 7) + " " + phone.substring(7);
    }
}