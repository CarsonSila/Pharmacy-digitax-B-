package com.example.hethongbanthuoc.feature_shopping.cart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.hethongbanthuoc.feature_product.models.Model_Product;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.core.utils.TokenManager;
import com.example.hethongbanthuoc.feature_shopping.cart.models.Model_ChiTietGioHangRequest;
import com.example.hethongbanthuoc.feature_shopping.order.XacNhanDonHangActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomSheetChonMua extends BottomSheetDialogFragment {

    // Khai báo biến UI
    private ImageView imgProduct, btnTru, btnCong;
    private TextView tvTenSanPham, tvGiaBan, tvSoLuong, tvTamTinh, tvDonVi;
    private MaterialButton btnThemVaoGio, btnMuaNgay;

    // Biến dữ liệu sản phẩm và người dùng
    private Model_Product sanPham;
    private OnChonMuaListener listener;
    private int soLuong = 1;
    private String donViDaChon = "Hộp";
    private double giaDonVi = 0;

    // API service và thông tin người dùng
    private API_AuthService apiService;
    private int maNguoiDung;
    private int maGioHang = -1;

    // Constructor mặc định (dùng cho mua lại)
    public BottomSheetChonMua() {}
    // Interface callback khi chọn mua
    public interface OnChonMuaListener {
        void onChonMua(Model_Product sanPham, int soLuong, String donVi);
    }

    // Constructor nhận vào sản phẩm và listener callback
    public BottomSheetChonMua(Model_Product sanPham, OnChonMuaListener listener) {
        this.sanPham = sanPham;
        this.listener = listener;
    }

    // Gắn layout cho BottomSheet
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_bottom_sheet_chon_mua, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo API service
        apiService = API_RetrofitClient.getRetrofitInstance(requireContext()).create(API_AuthService.class);

        // Lấy token và mã người dùng
        String token = TokenManager.getToken(requireContext());
        maNguoiDung = TokenManager.getUserId(requireContext());

        // Giải mã JWT nếu cần
        if (maNguoiDung == -1 && token != null) {
            try {
                String[] parts = token.split("\\.");
                if (parts.length == 3) {
                    String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE));
                    JSONObject json = new JSONObject(payload);
                    maNguoiDung = json.optInt("userId", -1);
                    if (maNguoiDung != -1) {
                        TokenManager.saveUserId(requireContext(), maNguoiDung);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Nếu chưa đăng nhập thì thoát
        if (maNguoiDung == -1) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để mua hàng", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        // Khởi tạo views
        initViews(view);

        // Kiểm tra xem có phải mua lại không
        Bundle bundle = getArguments();
        if (bundle != null && bundle.getBoolean("fromMuaLai", false)) {
            loadDataFromBundle(bundle);
        } else {
            loadProductInfo();
        }

        loadGioHang();
    }

    // Hàm ánh xạ view
    private void initViews(View view) {
        imgProduct = view.findViewById(R.id.imgProduct);
        tvTenSanPham = view.findViewById(R.id.tvTenSanPham);
        tvGiaBan = view.findViewById(R.id.tvGiaBan);
        tvTamTinh = view.findViewById(R.id.tvTamTinh);
        tvSoLuong = view.findViewById(R.id.tvSoLuong);
        tvDonVi = view.findViewById(R.id.tvDonVi);
        btnTru = view.findViewById(R.id.btnTru);
        btnCong = view.findViewById(R.id.btnCong);
        btnThemVaoGio = view.findViewById(R.id.btnThemVaoGio);
        btnMuaNgay = view.findViewById(R.id.btnMuaNgay);
    }

    // Load dữ liệu từ Bundle (cho trường hợp mua lại)
    private void loadDataFromBundle(Bundle bundle) {
        ArrayList<HashMap<String, Object>> sanPhamList =
                (ArrayList<HashMap<String, Object>>) bundle.getSerializable("listSanPham");

        if (sanPhamList == null || sanPhamList.isEmpty()) {
            Toast.makeText(requireContext(), "Không có sản phẩm trong đơn hàng!", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        // Lấy sản phẩm đầu tiên (hoặc bạn có thể xử lý nhiều sản phẩm)
        HashMap<String, Object> firstProduct = sanPhamList.get(0);

        // Tạo đối tượng Model_Product từ dữ liệu
        sanPham = new Model_Product();
        sanPham.setMaSanPham((Integer) firstProduct.get("maSanPham"));
        sanPham.setTenThuoc((String) firstProduct.get("tenSanPham"));
        sanPham.setDuongDanAnh((String) firstProduct.get("hinhAnh"));

        Object donGiaObj = firstProduct.get("donGia");
        if (donGiaObj instanceof Double) {
            sanPham.setGia((Double) donGiaObj);
        } else if (donGiaObj instanceof Integer) {
            sanPham.setGia(((Integer) donGiaObj).doubleValue());
        }

        Object soLuongObj = firstProduct.get("soLuong");
        if (soLuongObj instanceof Integer) {
            soLuong = (Integer) soLuongObj;
        }

        // Hiển thị thông tin
        loadProductInfo();
        tvSoLuong.setText(String.valueOf(soLuong));
        updateTamTinh();
    }

    // Hàm hiển thị thông tin sản phẩm
    private void loadProductInfo() {
        Glide.with(requireContext())
                .load(sanPham.getDuongDanAnh())
                .placeholder(R.drawable.sanpham)
                .into(imgProduct);

        giaDonVi = sanPham.getGia() != null ? sanPham.getGia() : 0.0;
        donViDaChon = sanPham.getTenDonVi() != null ? sanPham.getTenDonVi() : "Hộp";

        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvTenSanPham.setText(sanPham.getTenThuoc());
        tvGiaBan.setText(nf.format(giaDonVi) + "đ / " + donViDaChon);
        tvDonVi.setText(donViDaChon);
        updateTamTinh();
    }

    // Hàm xử lý sự kiện click
    private void setupListeners() {
        btnTru.setOnClickListener(v -> {
            if (soLuong > 1) {
                soLuong--;
                tvSoLuong.setText(String.valueOf(soLuong));
                updateTamTinh();
            }
        });

        btnCong.setOnClickListener(v -> {
            soLuong++;
            tvSoLuong.setText(String.valueOf(soLuong));
            updateTamTinh();
        });

        btnThemVaoGio.setOnClickListener(v -> themVaoGioHang());
        btnMuaNgay.setOnClickListener(v -> muaNgay());
    }

    // Cập nhật tổng tạm tính theo số lượng
    private void updateTamTinh() {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        double tamTinh = giaDonVi * soLuong;
        tvTamTinh.setText(nf.format(tamTinh) + "đ");
    }

    // Gọi API để lấy thông tin giỏ hàng hiện tại
    private void loadGioHang() {
        String token = "Bearer " + TokenManager.getToken(requireContext());
        apiService.getGioHangTheoNguoiDung(token, maNguoiDung).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    Object maGioHangObj = data.get("maGioHang");

                    if (maGioHangObj instanceof Number) {
                        maGioHang = ((Number) maGioHangObj).intValue();
                    } else if (maGioHangObj != null) {
                        maGioHang = Integer.parseInt(maGioHangObj.toString());
                    }
                    setupListeners();
                } else {
                    Toast.makeText(requireContext(), "Không thể tải giỏ hàng", Toast.LENGTH_SHORT).show();
                    setupListeners();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                setupListeners();
            }
        });
    }

    // Thêm sản phẩm vào giỏ hàng
    private void themVaoGioHang() {
        if (maGioHang == -1) {
            Toast.makeText(requireContext(), "Đang tải giỏ hàng, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            return;
        }

        btnThemVaoGio.setEnabled(false);
        btnThemVaoGio.setText("Đang thêm...");

        Model_ChiTietGioHangRequest request = new Model_ChiTietGioHangRequest(
                maGioHang,
                sanPham.getMaSanPham(),
                soLuong
        );

        apiService.addChiTietGioHang(request).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                btnThemVaoGio.setEnabled(true);
                btnThemVaoGio.setText("Thêm vào giỏ");

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onChonMua(sanPham, soLuong, donViDaChon);
                    }
                    dismiss();
                    Intent intent = new Intent(requireContext(), QuanLyGioHangActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(requireContext(), "Thêm vào giỏ thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                btnThemVaoGio.setEnabled(true);
                btnThemVaoGio.setText("Thêm vào giỏ");
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Mua ngay sản phẩm (chuyển sang trang xác nhận đơn hàng)
    private void muaNgay() {
        Intent intent = new Intent(requireContext(), XacNhanDonHangActivity.class);
        intent.putExtra("maSanPham", sanPham.getMaSanPham());
        intent.putExtra("tenThuoc", sanPham.getTenThuoc());
        intent.putExtra("soLuong", soLuong);
        intent.putExtra("donGia", giaDonVi);
        intent.putExtra("donVi", donViDaChon);
        intent.putExtra("hinhAnh", sanPham.getDuongDanAnh());
        intent.putExtra("tongTien", giaDonVi * soLuong);
        intent.putExtra("muaNgay", true);
        intent.putExtra("maNguoiDung", maNguoiDung);

        if (listener != null) {
            listener.onChonMua(sanPham, soLuong, donViDaChon);
        }

        dismiss();
        startActivity(intent);
    }
}
