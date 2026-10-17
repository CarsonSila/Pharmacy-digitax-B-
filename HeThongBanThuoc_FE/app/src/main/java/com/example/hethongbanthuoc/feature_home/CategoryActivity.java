package com.example.hethongbanthuoc.feature_home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hethongbanthuoc.feature_home.adapters.Adapter_DanhMucCon;
import com.example.hethongbanthuoc.feature_product.list.Adapter_SanPhamGrid;
import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.core.models.DanhMucSanPhamResponse;
import com.example.hethongbanthuoc.feature_home.models.Model_Category;
import com.example.hethongbanthuoc.feature_product.models.Model_Product;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.feature_shopping.cart.BottomSheetChonMua;
import com.example.hethongbanthuoc.feature_product.search.SearchActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryActivity extends AppCompatActivity {

    private static final String TAG = "CategoryActivity";

    // View
    private ImageView btnBack, btnSearch;
    private RelativeLayout btnCart;
    private TextView tvCategoryTitle, tvCartCount, tvProductCount;
    private TabLayout tabLayoutParent;
    private CardView cardDanhMucCon, loadingCard;
    private RecyclerView recyclerViewChildCategories, recyclerViewProducts;
    private LinearLayout layoutEmptyState;
    private Chip chipBestSeller, chipPriceLow, chipPriceHigh;

    // Dữ liệu
    private Integer selectedDanhMucChaId, selectedDanhMucConId;
    private boolean isDanhMucCha;
    private String tenDanhMucFromIntent;
    private String currentSort = "ban-chay";
    private boolean isLoadingInitialData = true;

    private API_AuthService apiService;
    private Adapter_DanhMucCon danhMucConAdapter;
    private Adapter_SanPhamGrid sanPhamAdapter;

    private List<Model_Category> allDanhMucCha = new ArrayList<>();
    private List<Model_Category> currentDanhMucCon = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        getIntentData();
        initAPI();
        initViews();
        setupListeners();

        if (tenDanhMucFromIntent != null) {
            tvCategoryTitle.setText(tenDanhMucFromIntent);
        }

        loadAllDanhMucCha();
    }

    // Lấy dữ liệu từ Intent
    private void getIntentData() {
        selectedDanhMucChaId = getIntent().getIntExtra("DANHMUC_CHA_ID", -1);
        selectedDanhMucConId = getIntent().getIntExtra("DANHMUC_CON_ID", -1);
        isDanhMucCha = getIntent().getBooleanExtra("IS_DANHMUC_CHA", true);
        tenDanhMucFromIntent = getIntent().getStringExtra("DANHMUC_TEN");

        if (selectedDanhMucChaId == -1) selectedDanhMucChaId = null;
        if (selectedDanhMucConId == -1) selectedDanhMucConId = null;
    }

    // Khởi tạo API
    private void initAPI() {
        apiService = API_RetrofitClient.getRetrofitInstance(this).create(API_AuthService.class);
    }

    // Ánh xạ view và cấu hình RecyclerView
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSearch = findViewById(R.id.btnSearch);
        btnCart = findViewById(R.id.btnCart);
        tvCategoryTitle = findViewById(R.id.tvCategoryTitle);
        tvCartCount = findViewById(R.id.tvCartCount);
        tabLayoutParent = findViewById(R.id.tabLayoutParent);
        cardDanhMucCon = findViewById(R.id.cardDanhMucCon);
        loadingCard = findViewById(R.id.loadingCard);
        recyclerViewChildCategories = findViewById(R.id.recyclerViewChildCategories);
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        tvProductCount = findViewById(R.id.tvProductCount);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        chipBestSeller = findViewById(R.id.chipBestSeller);
        chipPriceLow = findViewById(R.id.chipPriceLow);
        chipPriceHigh = findViewById(R.id.chipPriceHigh);

        recyclerViewChildCategories.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewProducts.setNestedScrollingEnabled(false);
    }

    // Thiết lập sự kiện
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSearch.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        btnCart.setOnClickListener(v -> Toast.makeText(this, "Mở giỏ hàng", Toast.LENGTH_SHORT).show());
        setupChipListeners();
    }

    // Thiết lập filter chip
    private void setupChipListeners() {
        chipBestSeller.setOnClickListener(v -> onSortSelected("ban-chay"));
        chipPriceLow.setOnClickListener(v -> onSortSelected("gia-thap"));
        chipPriceHigh.setOnClickListener(v -> onSortSelected("gia-cao"));
    }

    // Chọn loại sắp xếp
    private void onSortSelected(String sortType) {
        currentSort = sortType;
        updateChipStates();
        loadSanPham();
    }

    // Cập nhật chip đang chọn
    private void updateChipStates() {
        chipBestSeller.setChecked("ban-chay".equals(currentSort));
        chipPriceLow.setChecked("gia-thap".equals(currentSort));
        chipPriceHigh.setChecked("gia-cao".equals(currentSort));
    }

    // Hiển thị trạng thái loading
    private void showLoading() {
        loadingCard.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
        recyclerViewProducts.setVisibility(View.GONE);
    }

    private void hideLoading() {
        loadingCard.setVisibility(View.GONE);
    }

    // Hiển thị trạng thái trống
    private void showEmptyState() {
        hideLoading();
        layoutEmptyState.setVisibility(View.VISIBLE);
        recyclerViewProducts.setVisibility(View.GONE);
        updateProductCount(0);
    }

    private void showProducts() {
        hideLoading();
        layoutEmptyState.setVisibility(View.GONE);
        recyclerViewProducts.setVisibility(View.VISIBLE);
    }

    private void updateProductCount(int count) {
        if (tvProductCount != null) tvProductCount.setText("Tìm thấy " + count + " sản phẩm");
    }

    // Thiết lập sự kiện cho Tab danh mục cha
    private void setupTabListener() {
        tabLayoutParent.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (isLoadingInitialData) return;
                int position = tab.getPosition();
                if (position >= 0 && position < allDanhMucCha.size()) {
                    Model_Category selectedCha = allDanhMucCha.get(position);
                    selectedDanhMucChaId = selectedCha.getMaDanhMuc();
                    selectedDanhMucConId = null;
                    tvCategoryTitle.setText(selectedCha.getTenDanhMuc());
                    loadDanhMucCon(selectedDanhMucChaId);
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    // Gọi API: tải danh mục cha
    private void loadAllDanhMucCha() {
        showLoading();
        apiService.getAllDanhMuc().enqueue(new Callback<List<Model_Category>>() {
            @Override
            public void onResponse(Call<List<Model_Category>> call, Response<List<Model_Category>> response) {
                hideLoading();
                if (!response.isSuccessful() || response.body() == null) {
                    showToast("Không thể tải danh mục");
                    showEmptyState();
                    return;
                }

                List<Model_Category> allCategories = response.body();
                allDanhMucCha.clear();
                for (Model_Category dm : allCategories) {
                    if (dm.getMaDanhMucCha() == null) allDanhMucCha.add(dm);
                }

                if (allDanhMucCha.isEmpty()) {
                    showToast("Không có danh mục");
                    showEmptyState();
                } else setupTabsAfterLoad();
            }

            @Override
            public void onFailure(Call<List<Model_Category>> call, Throwable t) {
                hideLoading();
                showToast("Lỗi kết nối");
                showEmptyState();
            }
        });
    }

    // Sau khi tải danh mục cha
    private void setupTabsAfterLoad() {
        tabLayoutParent.clearOnTabSelectedListeners();
        tabLayoutParent.removeAllTabs();
        int selectedTabIndex = 0;

        for (int i = 0; i < allDanhMucCha.size(); i++) {
            Model_Category dm = allDanhMucCha.get(i);
            tabLayoutParent.addTab(tabLayoutParent.newTab().setText(dm.getTenDanhMuc()));
            if (selectedDanhMucChaId != null && dm.getMaDanhMuc().equals(selectedDanhMucChaId))
                selectedTabIndex = i;
        }

        int finalIndex = selectedTabIndex;
        tabLayoutParent.post(() -> {
            TabLayout.Tab tab = tabLayoutParent.getTabAt(finalIndex);
            if (tab != null) tab.select();
            tabLayoutParent.postDelayed(() -> {
                setupTabListener();
                loadDanhMucCon(selectedDanhMucChaId);
                isLoadingInitialData = false;
            }, 200);
        });
    }

    // Gọi API: tải danh mục con
    private void loadDanhMucCon(Integer idDanhMucCha) {
        apiService.getDanhMucCon(idDanhMucCha).enqueue(new Callback<DanhMucSanPhamResponse<List<Model_Category>>>() {
            @Override
            public void onResponse(Call<DanhMucSanPhamResponse<List<Model_Category>>> call, Response<DanhMucSanPhamResponse<List<Model_Category>>> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    handleNoDanhMucCon();
                    return;
                }

                currentDanhMucCon = response.body().getData();
                if (currentDanhMucCon == null || currentDanhMucCon.isEmpty())
                    handleNoDanhMucCon();
                else setupDanhMucConRecycler();
            }

            @Override
            public void onFailure(Call<DanhMucSanPhamResponse<List<Model_Category>>> call, Throwable t) {
                handleNoDanhMucCon();
            }
        });
    }

    // Khi không có danh mục con
    private void handleNoDanhMucCon() {
        cardDanhMucCon.setVisibility(View.GONE);
        selectedDanhMucConId = null;
        loadSanPham();
    }

    // Thiết lập RecyclerView danh mục con
    private void setupDanhMucConRecycler() {
        cardDanhMucCon.setVisibility(View.VISIBLE);
        danhMucConAdapter = new Adapter_DanhMucCon(currentDanhMucCon, danhMuc -> {
            selectedDanhMucConId = danhMuc.getMaDanhMuc();
            tvCategoryTitle.setText(danhMuc.getTenDanhMuc());
            loadSanPham();
        });
        recyclerViewChildCategories.setAdapter(danhMucConAdapter);

        if (!isDanhMucCha && selectedDanhMucConId != null) {
            for (int i = 0; i < currentDanhMucCon.size(); i++) {
                if (currentDanhMucCon.get(i).getMaDanhMuc().equals(selectedDanhMucConId)) {
                    danhMucConAdapter.setSelectedPosition(i);
                    recyclerViewChildCategories.scrollToPosition(i);
                    tvCategoryTitle.setText(currentDanhMucCon.get(i).getTenDanhMuc());
                    break;
                }
            }
        }
        loadSanPham();
    }

    // Gọi API: tải sản phẩm
    private void loadSanPham() {
        Integer targetId = (selectedDanhMucConId != null) ? selectedDanhMucConId : selectedDanhMucChaId;
        showLoading();

        apiService.getSanPhamByDanhMuc(targetId, currentSort).enqueue(new Callback<DanhMucSanPhamResponse<List<Model_Product>>>() {
            @Override
            public void onResponse(Call<DanhMucSanPhamResponse<List<Model_Product>>> call, Response<DanhMucSanPhamResponse<List<Model_Product>>> response) {
                hideLoading();
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    showToast("Không thể tải sản phẩm");
                    showEmptyState();
                    return;
                }

                List<Model_Product> list = response.body().getData();
                if (list == null || list.isEmpty()) {
                    showEmptyState();
                    return;
                }

                showProducts();
                updateProductCount(list.size()); // thực hiện đếm số lượng sản phẩm trả về
                sanPhamAdapter = new Adapter_SanPhamGrid(CategoryActivity.this, list, CategoryActivity.this::showBottomSheetChonMua);
                recyclerViewProducts.setAdapter(sanPhamAdapter);
            }

            @Override
            public void onFailure(Call<DanhMucSanPhamResponse<List<Model_Product>>> call, Throwable t) {
                hideLoading();
                showToast("Lỗi kết nối");
                showEmptyState();
            }
        });
    }

    // Hiển thị BottomSheet chọn mua
    private void showBottomSheetChonMua(Model_Product sanPham) {
        BottomSheetChonMua bottomSheet = new BottomSheetChonMua(sanPham, (sp, soLuong, donVi) -> {
            Toast.makeText(CategoryActivity.this,
                    "Đã thêm " + soLuong + " " + sp.getTenThuoc() + " vào giỏ hàng",
                    Toast.LENGTH_SHORT).show();
        });
        bottomSheet.show(getSupportFragmentManager(), "BottomSheetChonMua");
    }

    // Hiển thị thông báo ngắn
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
