package com.example.hethongbanthuoc.feature_product.search;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hethongbanthuoc.feature_product.list.Adapter_SanPhamGrid;
import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.feature_product.models.Model_LichSuTimKiem;
import com.example.hethongbanthuoc.feature_product.models.Model_TuKhoaTimKiem;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.feature_shopping.cart.BottomSheetChonMua;
import com.example.hethongbanthuoc.core.utils.TokenManager;
import com.example.hethongbanthuoc.feature_product.models.Model_Product;
import com.google.android.material.chip.Chip;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * MAIN TÌM KIẾM SẢN PHẨM
 * - Hiển thị lịch sử tìm kiếm, từ khóa xu hướng
 * - Cho phép nhập từ khóa để tìm sản phẩm theo API
 * - Hỗ trợ sắp xếp kết quả (bán chạy, giá thấp, giá cao)
 * - Cho phép xóa từng lịch sử hoặc toàn bộ lịch sử tìm kiếm
 *
 */
public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";
    private static final int DEBOUNCE_DELAY = 300; // Thời gian chờ debounce 300ms

    // KHAI BÁO VIEW
    private EditText edtSearch;
    private ImageView btnBack, btnClearSearch;
    private CardView loadingCard;

    // Các khu vực hiển thị (dạng CardView)
    private CardView sectionLichSu, sectionTrending;
    private LinearLayout sectionKetQua;
    private LinearLayout layoutEmptyState;

    // RecyclerView
    private RecyclerView rvLichSu, rvKetQuaSanPham;

    // Adapter
    private Adapter_LichSuTimKiem lichSuAdapter;
    private Adapter_SanPhamGrid productAdapter;

    // Dữ liệu
    private List<Model_LichSuTimKiem> listLichSu = new ArrayList<>();
    private List<Model_TuKhoaTimKiem> listTrending = new ArrayList<>();
    private List<Model_Product> listProduct = new ArrayList<>();

    // API
    private API_AuthService apiService;
    private String token;

    // Handler dùng cho debounce tìm kiếm
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Sắp xếp
    private String currentSortBy = "Gia";
    private String currentSortDir = "ASC";
    private TextView tvSortBanChay, tvSortGiaThap, tvSortGiaCao;

    // 1. LIFECYCLE METHODS

    // Hàm khởi tạo Activity
    // 1: set layout
    // 2: ánh xạ view, khởi tạo API, RecyclerView, listeners
    // 3: load từ khóa trending và lịch sử; nếu intent có keyword thì tìm ngay
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();         // Ánh xạ view
        initAPI();           // Khởi tạo API service
        setupRecyclerViews();// Thiết lập RecyclerView
        setupListeners();    // Thiết lập sự kiện click

        // Tải dữ liệu ban đầu
        loadTrendingKeywords(); // Từ khóa thịnh hành
        loadSearchHistory();    // Lịch sử tìm kiếm

        // Nếu có keyword truyền từ Intent thì tự động tìm kiếm
        String keyword = getIntent().getStringExtra("keyword");
        if (keyword != null && !keyword.isEmpty()) {
            edtSearch.setText(keyword);
            edtSearch.setSelection(keyword.length());
            performSearch(keyword);
        }
    }

    // Hàm hủy Activity
    // 1: xóa callbacks của handler nếu đang có
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }

    // 2. INITIALIZATION METHODS

    // Ánh xạ các view từ layout
    // 1: findViewById cho tất cả view cần dùng
    // 2: gán listener đơn giản cho nút "Xóa tất cả"
    private void initViews() {
        edtSearch = findViewById(R.id.edtSearch);
        btnBack = findViewById(R.id.btnBack);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        loadingCard = findViewById(R.id.loadingCard); // Loading card

        sectionLichSu = findViewById(R.id.sectionLichSu);
        sectionTrending = findViewById(R.id.sectionTrending);
        sectionKetQua = findViewById(R.id.sectionKetQua);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        rvLichSu = findViewById(R.id.rvLichSu);
        rvKetQuaSanPham = findViewById(R.id.rvKetQuaSanPham);

        tvSortBanChay = findViewById(R.id.tvSortBanChay);
        tvSortGiaThap = findViewById(R.id.tvSortGiaThap);
        tvSortGiaCao = findViewById(R.id.tvSortGiaCao);

        TextView tvXoaTatCa = findViewById(R.id.tvXoaTatCa);
        tvXoaTatCa.setOnClickListener(v -> showClearAllHistoryDialog());
    }

    // Khởi tạo API service và token
    // 1: lấy retrofit instance
    // 2: gán token kèm Bearer
    private void initAPI() {
        apiService = API_RetrofitClient.getRetrofitInstance(this).create(API_AuthService.class);
        token = "Bearer " + TokenManager.getToken(this);
    }

    // Thiết lập RecyclerView cho lịch sử và sản phẩm
    // 1: tạo adapter lịch sử và gán sự kiện
    // 2: tạo adapter sản phẩm dạng grid và gán
    private void setupRecyclerViews() {
        // Lịch sử tìm kiếm
        lichSuAdapter = new Adapter_LichSuTimKiem(new Adapter_LichSuTimKiem.OnItemClickListener() {
            @Override
            public void onItemClick(Model_LichSuTimKiem item) {
                edtSearch.setText(item.getTuKhoa());
                edtSearch.setSelection(item.getTuKhoa().length());
                performSearch(item.getTuKhoa());
            }

            @Override
            public void onDeleteClick(Model_LichSuTimKiem item, int position) {
                deleteSearchHistory(item, position);
            }
        });
        rvLichSu.setLayoutManager(new LinearLayoutManager(this));
        rvLichSu.setAdapter(lichSuAdapter);

        // Sản phẩm dạng lưới (Grid 2 cột)
        productAdapter = new Adapter_SanPhamGrid(this, listProduct, this::showBottomSheetChonMua);
        rvKetQuaSanPham.setLayoutManager(new GridLayoutManager(this, 2));
        rvKetQuaSanPham.setAdapter(productAdapter);
    }

    //  3. EVENT LISTENERS

    // Thiết lập các sự kiện UI
    // 1: xử lý nút back và nút xóa
    // 2: thiết lập TextWatcher với debounce
    // 3: xử lý focus và các nút sắp xếp
    private void setupListeners() {
        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Nút xóa nội dung tìm kiếm
        btnClearSearch.setOnClickListener(v -> {
            edtSearch.setText("");
            showInitialState();
        });

        // Theo dõi khi người dùng nhập (debounce 300ms)
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 1: hiện/ẩn nút clear
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);

                // 2: hủy callback cũ nếu có
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);

                // 3: nếu rỗng thì về trạng thái ban đầu, ngược lại debounce và gọi search
                if (s.length() == 0) {
                    showInitialState();
                    return;
                }

                searchRunnable = () -> performSearch(s.toString());
                searchHandler.postDelayed(searchRunnable, DEBOUNCE_DELAY);
            }
        });

        // Khi EditText được focus: nếu rỗng hiển thị trạng thái ban đầu
        edtSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && edtSearch.getText().toString().isEmpty()) {
                showInitialState();
            }
        });

        // Sắp xếp: Bán chạy
        tvSortBanChay.setOnClickListener(v -> {
            currentSortBy = "TongBan";
            currentSortDir = "DESC";
            updateSortUI(tvSortBanChay);
            performSearch(edtSearch.getText().toString());
        });

        // Sắp xếp: Giá thấp
        tvSortGiaThap.setOnClickListener(v -> {
            currentSortBy = "Gia";
            currentSortDir = "ASC";
            updateSortUI(tvSortGiaThap);
            performSearch(edtSearch.getText().toString());
        });

        // Sắp xếp: Giá cao
        tvSortGiaCao.setOnClickListener(v -> {
            currentSortBy = "Gia";
            currentSortDir = "DESC";
            updateSortUI(tvSortGiaCao);
            performSearch(edtSearch.getText().toString());
        });
    }

    // 4. UI STATE MANAGEMENT

    // Hiển thị trạng thái ban đầu: show lịch sử và trending, ẩn kết quả
    // 1: show/hidden section dựa trên dữ liệu hiện có
    // 2: ẩn sectionKetQua
    private void showInitialState() {
        sectionLichSu.setVisibility(listLichSu.isEmpty() ? View.GONE : View.VISIBLE);
        sectionTrending.setVisibility(listTrending.isEmpty() ? View.GONE : View.VISIBLE);
        sectionKetQua.setVisibility(View.GONE);
    }

    // Hiển thị giao diện khi có kết quả tìm kiếm
    // 1: ẩn lịch sử và trending
    // 2: show kết quả
    private void showSearchResultState() {
        sectionLichSu.setVisibility(View.GONE);
        sectionTrending.setVisibility(View.GONE);
        sectionKetQua.setVisibility(View.VISIBLE);
    }

    // Hiển thị loading
    // 1: set visible cho loadingCard
    private void showLoading() {
        loadingCard.setVisibility(View.VISIBLE);
    }

    // Ẩn loading
    // 1: set gone cho loadingCard
    private void hideLoading() {
        loadingCard.setVisibility(View.GONE);
    }

    // Hiển thị giao diện khi không có kết quả
    // 1: ẩn RecyclerView, show empty layout
    private void showEmptyState() {
        rvKetQuaSanPham.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
    }

    // Cập nhật giao diện sắp xếp (pill background)
    // 1: reset trạng thái các nút
    // 2: set trạng thái cho nút được chọn
    private void updateSortUI(TextView selectedView) {
        // Reset màu chữ
        tvSortBanChay.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        tvSortGiaThap.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        tvSortGiaCao.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));

        // Reset background
        tvSortBanChay.setBackgroundResource(R.drawable.bg_sort_unselected);
        tvSortGiaThap.setBackgroundResource(R.drawable.bg_sort_unselected);
        tvSortGiaCao.setBackgroundResource(R.drawable.bg_sort_unselected);

        // Set selected
        selectedView.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        selectedView.setBackgroundResource(R.drawable.bg_sort_selected);
    }

    //  5. API CALLS

    // Lấy danh sách từ khóa thịnh hành
    // 1: gọi API
    // 2: parse response, chuyển vào listTrending
    // 3: gọi updateTrendingUI để render
    private void loadTrendingKeywords() {
        apiService.getTrendingKeywords(token, 8).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> result = response.body();
                    Boolean success = (Boolean) result.get("success");

                    if (Boolean.TRUE.equals(success)) {
                        List<Object> data = (List<Object>) result.get("data");
                        if (data != null) {
                            listTrending.clear();
                            Gson gson = new Gson();
                            for (Object obj : data) {
                                String json = gson.toJson(obj);
                                Model_TuKhoaTimKiem item = gson.fromJson(json, Model_TuKhoaTimKiem.class);
                                listTrending.add(item);
                            }
                            updateTrendingUI();
                        }
                    }
                } else {
                    Log.e(TAG, "Error loading trending: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Failure loading trending: " + t.getMessage());
            }
        });
    }

    // Cập nhật UI hiển thị các từ khóa xu hướng
    // 1: lấy FlexboxLayout và clear các view cũ
    // 2: inflate từng chip, gán text và click listener
    // 3: add view vào flex và show/hide sectionTrending
    private void updateTrendingUI() {
        com.google.android.flexbox.FlexboxLayout flexTrending = findViewById(R.id.flexTrending);
        flexTrending.removeAllViews();

        for (Model_TuKhoaTimKiem item : listTrending) {
            View chipView = getLayoutInflater().inflate(R.layout.item_trending_keyword, flexTrending, false);
            Chip chip = chipView.findViewById(R.id.chipKeyword);
            chip.setText(item.getTuKhoa());
            chip.setOnClickListener(v -> {
                edtSearch.setText(item.getTuKhoa());
                edtSearch.setSelection(item.getTuKhoa().length());
                performSearch(item.getTuKhoa());
            });
            flexTrending.addView(chipView);
        }

        sectionTrending.setVisibility(listTrending.isEmpty() ? View.GONE : View.VISIBLE);
    }

    // Lấy danh sách lịch sử tìm kiếm
    // 1: gọi API
    // 2: parse response, map thành listLichSu
    // 3: cập nhật adapter và show/hide sectionLichSu
    private void loadSearchHistory() {
        apiService.getSearchHistory(token, 10).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> result = response.body();
                    Boolean success = (Boolean) result.get("success");

                    if (Boolean.TRUE.equals(success)) {
                        List<Object> data = (List<Object>) result.get("data");
                        if (data != null) {
                            listLichSu.clear();
                            Gson gson = new Gson();
                            for (Object obj : data) {
                                String json = gson.toJson(obj);
                                Model_LichSuTimKiem item = gson.fromJson(json, Model_LichSuTimKiem.class);
                                listLichSu.add(item);
                            }
                            lichSuAdapter.setData(listLichSu);
                            sectionLichSu.setVisibility(listLichSu.isEmpty() ? View.GONE : View.VISIBLE);
                        }
                    }
                } else {
                    Log.e(TAG, "Error loading history: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Failure loading history: " + t.getMessage());
            }
        });
    }

    // Thực hiện tìm kiếm sản phẩm
    // 1: kiểm tra keyword, nếu rỗng trả về trạng thái ban đầu
    // 2: show UI kết quả + loading
    // 3: gọi API searchProducts -> parse -> cập nhật adapter hoặc hiển thị empty
    private void performSearch(String keyword) {
        if (keyword.trim().isEmpty()) {
            showInitialState();
            return;
        }

        showSearchResultState();
        showLoading();

        apiService.searchProducts(token, keyword, 1, 20, currentSortBy, currentSortDir)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        hideLoading();

                        if (response.isSuccessful() && response.body() != null) {
                            Map<String, Object> result = response.body();
                            Boolean success = (Boolean) result.get("success");

                            if (Boolean.TRUE.equals(success)) {
                                List<Object> data = (List<Object>) result.get("data");
                                if (data != null && !data.isEmpty()) {
                                    listProduct.clear();
                                    Gson gson = new Gson();
                                    for (Object obj : data) {
                                        String json = gson.toJson(obj);
                                        Model_Product item = gson.fromJson(json, Model_Product.class);
                                        listProduct.add(item);
                                    }

                                    productAdapter.updateData(listProduct);
                                    layoutEmptyState.setVisibility(View.GONE);
                                    rvKetQuaSanPham.setVisibility(View.VISIBLE);

                                    loadSearchHistory(); // Reload lại lịch sử sau khi tìm kiếm
                                } else {
                                    showEmptyState();
                                }
                            } else {
                                showEmptyState();
                            }
                        } else {
                            Log.e(TAG, "Error search: " + response.code());
                            Toast.makeText(SearchActivity.this, "Lỗi tìm kiếm", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        hideLoading();
                        Log.e(TAG, "Failure search: " + t.getMessage());
                        Toast.makeText(SearchActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Xóa 1 lịch sử tìm kiếm
    // 1: gọi API xóa theo id
    // 2: nếu thành công remove item khỏi list + adapter
    private void deleteSearchHistory(Model_LichSuTimKiem item, int position) {
        apiService.deleteSearchHistory(token, item.getMaLichSu())
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Map<String, Object> result = response.body();
                            Boolean success = (Boolean) result.get("success");

                            if (Boolean.TRUE.equals(success)) {
                                // gọi Adapter để xóa
                                lichSuAdapter.removeItem(position);

                                Toast.makeText(SearchActivity.this, "Đã xóa", Toast.LENGTH_SHORT).show();

                                if (lichSuAdapter.getItemCount() == 0) {
                                    sectionLichSu.setVisibility(View.GONE);
                                }
                            } else {
                                Toast.makeText(SearchActivity.this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Log.e(TAG, "Delete history failed: " + t.getMessage());
                        Toast.makeText(SearchActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Hiển thị dialog xác nhận xóa toàn bộ lịch sử
    // 1: hiển thị AlertDialog
    // 2: nếu người dùng xác nhận thì gọi clearAllHistory()
    private void showClearAllHistoryDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa tất cả lịch sử")
                .setMessage("Bạn có chắc muốn xóa toàn bộ lịch sử tìm kiếm?")
                .setPositiveButton("Xóa", (dialog, which) -> clearAllHistory())
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Xóa toàn bộ lịch sử tìm kiếm
    // 1: gọi API clearAllSearchHistory
    // 2: nếu thành công clear list và cập nhật UI
    private void clearAllHistory() {
        apiService.clearAllSearchHistory(token).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> result = response.body();
                    Boolean success = (Boolean) result.get("success");

                    if (Boolean.TRUE.equals(success)) {
                        listLichSu.clear();
                        lichSuAdapter.setData(listLichSu);
                        sectionLichSu.setVisibility(View.GONE);
                        Toast.makeText(SearchActivity.this, "Đã xóa tất cả lịch sử", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SearchActivity.this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Clear all history failed: " + t.getMessage());
                Toast.makeText(SearchActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hiển thị buttonsheet thông tin chi tiết sản phẩm khi chọn mua sản phẩm
    private void showBottomSheetChonMua(Model_Product sanPham) {
        Log.d(TAG, "Mở BottomSheet: " + sanPham.getTenThuoc());

        BottomSheetChonMua bottomSheet = new BottomSheetChonMua(sanPham, (sp, soLuong, donVi) -> {
            // Callback khi thêm vào giỏ hàng thành công
            Toast.makeText(SearchActivity.this,
                    "Đã thêm " + soLuong + " " + sp.getTenThuoc() + " vào giỏ hàng",
                    Toast.LENGTH_SHORT).show();
        });

        bottomSheet.show(getSupportFragmentManager(), "BottomSheetChonMua");
    }
}
