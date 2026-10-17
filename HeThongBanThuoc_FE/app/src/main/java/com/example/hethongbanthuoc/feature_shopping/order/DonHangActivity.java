package com.example.hethongbanthuoc.feature_shopping.order;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.feature_shopping.cart.BottomSheetChonMua;
import com.example.hethongbanthuoc.feature_shopping.order.models.Model_ChiTietDonHangRequest;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.core.utils.TokenManager;
import com.example.hethongbanthuoc.feature_home.Main_HomePageActivity;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * Activity quản lý danh sách đơn hàng
 *
 * Chức năng:
 * - Hiển thị danh sách đơn hàng theo trạng thái (Tab Layout)
 * - Tìm kiếm đơn hàng theo mã đơn hoặc tên sản phẩm
 * - Xem chi tiết đơn hàng (click vào item hoặc nút "Xem chi tiết")
 * - Hủy đơn hàng (long click - chỉ với đơn "Đang xử lý")
 * - Mua lại đơn hàng (nút "Mua lại" - chỉ với đơn "Đã giao")
 */
public class DonHangActivity extends AppCompatActivity implements Adapter_DonHang.OnDonHangClickListener {

    private static final String TAG = "DonHangActivity";

    // Views
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private LinearLayout layoutEmpty;
    private Button btnKhamPha;
    private ImageView btnBack;
    private EditText edtSearch;
    private com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton fabHelp;

    // Data
    private Adapter_DonHang adapter;
    private API_AuthService apiService;
    private String currentTrangThai = "Tất cả";
    private List<Model_ChiTietDonHangRequest> fullList = new ArrayList<>();
    private List<Model_ChiTietDonHangRequest> filteredList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_don_hang);

        // Khởi tạo views
        initViews();

        // Setup API
        apiService = API_RetrofitClient.getRetrofitInstance(this).create(API_AuthService.class);

        // Setup RecyclerView
        setupRecyclerView();

        // Setup TabLayout
        setupTabLayout();

        // Setup Search
        setupSearch();

        // Load dữ liệu ban đầu
        loadDonHang("Tất cả");
    }

    /*
     * Khởi tạo các view
     */
    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        recyclerView = findViewById(R.id.recyclerViewDonHang);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        btnKhamPha = findViewById(R.id.btnKhamPha);
        btnBack = findViewById(R.id.btnBack);
        edtSearch = findViewById(R.id.edtSearch);
        fabHelp = findViewById(R.id.fabHelp);

        // Nút khám phá - về trang chủ
        btnKhamPha.setOnClickListener(v -> {
            Intent intent = new Intent(DonHangActivity.this, Main_HomePageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Nút back
        btnBack.setOnClickListener(v -> finish());

        // FAB Help - hiển thị hướng dẫn
        if (fabHelp != null) {
            fabHelp.setOnClickListener(v -> {
                showHelpDialog();
            });
        }
    }

    /*
     * Setup RecyclerView với adapter
     */
    private void setupRecyclerView() {
        adapter = new Adapter_DonHang(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    /*
     * Setup TabLayout với các trạng thái đơn hàng
     */
    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Tất cả"));
        tabLayout.addTab(tabLayout.newTab().setText("Đang xử lý"));
        tabLayout.addTab(tabLayout.newTab().setText("Đang giao"));
        tabLayout.addTab(tabLayout.newTab().setText("Đã giao"));
        tabLayout.addTab(tabLayout.newTab().setText("Đã hủy"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTrangThai = tab.getText().toString();
                edtSearch.setText(""); // Clear search khi đổi tab
                loadDonHang(currentTrangThai);

                // Hiển thị FAB help nếu tab "Đang xử lý"
                updateFabHelpVisibility(currentTrangThai);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    /*
     * Cập nhật hiển thị FAB Help
     */
    private void updateFabHelpVisibility(String trangThai) {
        if (fabHelp != null) {
            if ("Đang xử lý".equals(trangThai)) {
                fabHelp.setVisibility(View.VISIBLE);
            } else {
                fabHelp.setVisibility(View.GONE);
            }
        }
    }

    /*
     * Hiển thị dialog hướng dẫn
     */
    private void showHelpDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hướng dẫn sử dụng")
                .setMessage("• Nhấn vào đơn hàng để xem chi tiết\n\n" +
                        "• Giữ lâu vào đơn hàng 'Đang xử lý' để hủy đơn\n\n" +
                        "• Nhấn nút 'Mua lại' với đơn 'Đã giao' để đặt lại đơn hàng")
                .setPositiveButton("Đã hiểu", null)
                .show();
    }

    /*
     * Setup tìm kiếm đơn hàng
     */
    private void setupSearch() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterOrders(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /*
     * Lọc danh sách đơn hàng theo từ khóa
     */
    private void filterOrders(String keyword) {
        filteredList.clear();

        if (keyword.isEmpty()) {
            filteredList.addAll(fullList);
        } else {
            String lowerKeyword = keyword.toLowerCase();
            for (Model_ChiTietDonHangRequest order : fullList) {
                // Tìm kiếm theo mã đơn hàng
                boolean matchMaDonHang = String.valueOf(order.getMaDonHang()).contains(lowerKeyword);

                // Tìm kiếm theo tên sản phẩm
                boolean matchTenSanPham = false;
                if (order.getDanhSachSanPham() != null && !order.getDanhSachSanPham().isEmpty()) {
                    for (var sanPham : order.getDanhSachSanPham()) {
                        String tenSP = sanPham.getTenSanPham();
                        if (tenSP != null && tenSP.toLowerCase().contains(lowerKeyword)) {
                            matchTenSanPham = true;
                            break;
                        }
                    }
                }

                // Nếu khớp mã hoặc tên sản phẩm thì thêm vào danh sách
                if (matchMaDonHang || matchTenSanPham) {
                    filteredList.add(order);
                }
            }
        }

        updateUI();
    }

    /*
     * Load danh sách đơn hàng từ server
     */
    private void loadDonHang(String trangThai) {
        showLoading(true);

        String token = "Bearer " + TokenManager.getToken(this);
        Log.d(TAG, "Loading đơn hàng với trạng thái: " + trangThai);

        apiService.getDanhSachDonHang(token, trangThai).enqueue(new Callback<List<Model_ChiTietDonHangRequest>>() {
            @Override
            public void onResponse(Call<List<Model_ChiTietDonHangRequest>> call, Response<List<Model_ChiTietDonHangRequest>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    fullList = response.body();
                    filteredList.clear();
                    filteredList.addAll(fullList);

                    Log.d(TAG, "Load thành công: " + fullList.size() + " đơn hàng");

                    updateUI();
                } else {
                    Log.e(TAG, "Response không thành công: " + response.code());
                    showEmptyState("Không thể tải danh sách đơn hàng.\nVui lòng thử lại sau!");
                    Toast.makeText(DonHangActivity.this, "Lỗi khi tải đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Model_ChiTietDonHangRequest>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Lỗi API: " + t.getMessage());
                showEmptyState("Lỗi kết nối mạng.\nVui lòng kiểm tra và thử lại!");
                Toast.makeText(DonHangActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*
     * Cập nhật giao diện dựa trên dữ liệu
     */
    private void updateUI() {
        if (filteredList.isEmpty()) {
            String emptyMessage;
            if (edtSearch.getText().toString().trim().isEmpty()) {
                // Không có đơn hàng trong tab này
                emptyMessage = "Bạn chưa có đơn hàng " +
                        (currentTrangThai.equals("Tất cả") ? "nào" : "\"" + currentTrangThai + "\"") +
                        ".\nCùng khám phá hàng ngàn sản phẩm\ntại Nhà thuốc Việt Phúc nhé!";
            } else {
                // Không tìm thấy kết quả
                emptyMessage = "Không tìm thấy đơn hàng nào\nkhớp với từ khóa \"" + edtSearch.getText().toString().trim() + "\"";
            }
            showEmptyState(emptyMessage);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.setData(filteredList);
        }
    }

    /*
     * Hiển thị trạng thái loading
     */
    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : recyclerView.getVisibility());
        layoutEmpty.setVisibility(isLoading ? View.GONE : layoutEmpty.getVisibility());
    }

    /*
     * Hiển thị empty state với message tùy chỉnh
     */
    private void showEmptyState(String message) {
        recyclerView.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
        tvEmpty.setText(message);
    }

    /*
     * Xem chi tiết đơn hàng
     */
    @Override
    public void onXemChiTiet(Model_ChiTietDonHangRequest donHang) {
        Intent intent = new Intent(DonHangActivity.this, OrderDetailActivity.class);
        intent.putExtra("maDonHang", donHang.getMaDonHang());
        startActivity(intent);
        Log.d(TAG, "Mở chi tiết đơn hàng #" + donHang.getMaDonHang());
    }

    /*
     * Hủy đơn hàng (chỉ với trạng thái "Đang xử lý")
     */
    @Override
    public void onHuyDonHang(Model_ChiTietDonHangRequest donHang) {
        // Kiểm tra trạng thái đơn hàng
        if (!"Đang xử lý".equals(donHang.getTrangThai())) {
            Toast.makeText(this, "Chỉ có thể hủy đơn hàng đang xử lý!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị dialog xác nhận
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy đơn")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng #" + donHang.getMaDonHang() + "?\n\n" +
                        "Hành động này không thể hoàn tác.")
                .setPositiveButton("Hủy đơn", (dialog, which) -> {
                    huyDonHangAPI(donHang);
                })
                .setNegativeButton("Không", null)
                .setIcon(R.drawable.info)
                .show();
    }

    /*
     * Gọi API hủy đơn hàng
     */
    private void huyDonHangAPI(Model_ChiTietDonHangRequest donHang) {
        showLoading(true);

        String token = "Bearer " + TokenManager.getToken(this);
        int maDonHang = donHang.getMaDonHang();

        apiService.huyDonHang(token, maDonHang).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> result = response.body();
                    Boolean success = (Boolean) result.get("success");
                    String message = (String) result.get("message");

                    if (success != null && success) {
                        Toast.makeText(DonHangActivity.this, "Đã hủy đơn hàng thành công!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Đã hủy đơn hàng #" + maDonHang);

                        // Reload danh sách
                        loadDonHang(currentTrangThai);
                    } else {
                        Toast.makeText(DonHangActivity.this,
                                message != null ? message : "Không thể hủy đơn hàng",
                                Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Lỗi hủy đơn: " + message);
                    }
                } else {
                    Toast.makeText(DonHangActivity.this,
                            "Không thể hủy đơn hàng. Vui lòng thử lại!",
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Lỗi hủy đơn: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(DonHangActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Lỗi API hủy đơn: " + t.getMessage());
            }
        });
    }

    /*
     * Mua lại đơn hàng (chỉ với trạng thái "Đã giao")
     */
    @Override
    public void onMuaLai(Model_ChiTietDonHangRequest donHang) {
        if (!"Đã giao".equals(donHang.getTrangThai())) {
            Toast.makeText(this, "Chỉ có thể mua lại đơn hàng đã giao!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (donHang.getDanhSachSanPham() == null || donHang.getDanhSachSanPham().isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm trong đơn hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            BottomSheetChonMua bottomSheet = new BottomSheetChonMua();

            Bundle bundle = new Bundle();
            ArrayList<HashMap<String, Object>> sanPhamList = new ArrayList<>();

            for (var sanPham : donHang.getDanhSachSanPham()) {
                HashMap<String, Object> item = new HashMap<>();
                item.put("maSanPham", sanPham.getMaSanPham());
                item.put("tenSanPham", sanPham.getTenSanPham());
                item.put("hinhAnh", sanPham.getHinhAnh());
                item.put("soLuong", sanPham.getSoLuong());
                item.put("donGia", sanPham.getDonGia());
                sanPhamList.add(item);
            }

            bundle.putSerializable("listSanPham", sanPhamList);
            bundle.putBoolean("fromMuaLai", true);
            bundle.putInt("maDonHangGoc", donHang.getMaDonHang());

            bottomSheet.setArguments(bundle);
            bottomSheet.show(getSupportFragmentManager(), "BottomSheetChonMua");

            Log.d(TAG, "Mở BottomSheet mua lại đơn hàng #" + donHang.getMaDonHang() +
                    " với " + donHang.getDanhSachSanPham().size() + " sản phẩm");

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi mở BottomSheet mua lại: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Có lỗi xảy ra: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload khi quay lại màn hình
        Log.d(TAG, "onResume - Reloading data");
        loadDonHang(currentTrangThai);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear references
        if (adapter != null) {
            adapter.setData(null);
        }
        fullList.clear();
        filteredList.clear();
    }
}