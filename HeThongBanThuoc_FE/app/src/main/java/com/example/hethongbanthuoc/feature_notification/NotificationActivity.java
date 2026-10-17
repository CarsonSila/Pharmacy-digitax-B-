package com.example.hethongbanthuoc.feature_notification;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.feature_notification.models.model_ThongBao;
import com.example.hethongbanthuoc.core.models.GeneralResponse;
import com.example.hethongbanthuoc.feature_notification.models.model_ThongBaoListResponse;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.core.utils.TokenManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private LinearLayout emptyLayout;
    private EditText etSearch;

    private ImageView btnBack;
    private ImageButton btnClearSearch;
    private TextView tvActionText;
    private FloatingActionButton fabScrollTop;

    private Adapter_thongbao adapter;
    private API_AuthService apiService;
    private String token;

    private int currentPage = 1;
    private final int pageSize = 20;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private String currentLoaiThongBao = "Tất cả";
    private String searchKeyword = "";

    private static final String[] TAB_TITLES = {
            "Tất cả", "Đơn hàng", "Hệ thống", "Khuyến mãi", "Đổi quà", "Tư vấn"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        initViews();
        setupTabs();
        setupRecyclerView();
        setupListeners();

        token = "Bearer " + TokenManager.getToken(this);
        apiService = API_RetrofitClient.getRetrofitInstance(this).create(API_AuthService.class);

        loadThongBao();
    }

    // Bảng ánh xạ
    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        emptyLayout = findViewById(R.id.emptyLayout);
        etSearch = findViewById(R.id.etSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v-> getOnBackPressedDispatcher().onBackPressed());
        tvActionText = findViewById(R.id.tvActionText);
        fabScrollTop = findViewById(R.id.fabScrollTop);
    }

    // Setup tabs
    private void setupTabs() {
        for (String title : TAB_TITLES) {
            tabLayout.addTab(tabLayout.newTab().setText(title));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentLoaiThongBao = TAB_TITLES[tab.getPosition()];
                resetPagination();
                loadThongBao();
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    // Khởi tạo thông báo adapter
    private void setupRecyclerView() {
        adapter = new Adapter_thongbao(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Thực hiện xóa toàn bộ tin nhắn qua footer
        adapter.setOnFooterClickListener(() -> confirmDeleteAllRead());

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadMoreThongBao();
                    }
                }

                if (firstVisibleItemPosition > 5) {
                    fabScrollTop.show();
                } else {
                    fabScrollTop.hide();
                }
            }
        });

        adapter.setOnThongBaoClickListener(new Adapter_thongbao.OnThongBaoClickListener() {
            @Override
            public void onThongBaoClick(model_ThongBao thongBao, int position) {
                openThongBaoDetail(thongBao, position);
            }

            @Override
            public void onThongBaoLongClick(model_ThongBao thongBao, int position) {
                showOptionsDialog(thongBao, position);
            }
        });
    }


    // Sử lý sự kiện kéo làm mưới + sự kiện tìm kiếm thông báo
    private void setupListeners() {
        // Load thông báo
        swipeRefresh.setOnRefreshListener(() -> {
            resetPagination();
            loadThongBao();
        });

        // Tìm kiếm thông báo
        etSearch.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler();
            private Runnable searchRunnable;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
                searchRunnable = () -> {
                    searchKeyword = s.toString().trim();
                    resetPagination();
                    if (searchKeyword.isEmpty()) loadThongBao();
                    else searchThongBao();
                };
                handler.postDelayed(searchRunnable, 500);
            }
        });

        btnClearSearch.setOnClickListener(v -> etSearch.setText(""));
        tvActionText.setOnClickListener(v -> markAllAsRead());
        fabScrollTop.setOnClickListener(v -> recyclerView.smoothScrollToPosition(0));
    }

    // Gọi API lấy danh sách thông báo
    private void loadThongBao() {
        if (isLoading) return;
        isLoading = true;
        if (currentPage == 1) progressBar.setVisibility(View.VISIBLE);

        Call<model_ThongBaoListResponse> call;
        if ("Tất cả".equals(currentLoaiThongBao)) {
            call = apiService.getThongBaoList("Bearer " + token, currentPage, pageSize);
        } else {
            call = apiService.getThongBaoByLoai("Bearer " + token, currentLoaiThongBao, currentPage, pageSize);
        }

        call.enqueue(new Callback<model_ThongBaoListResponse>() {
            @Override
            public void onResponse(Call<model_ThongBaoListResponse> call, Response<model_ThongBaoListResponse> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<model_ThongBao> list = response.body().getData();
                    if (currentPage == 1) adapter.setThongBaoList(list);
                    else list.forEach(adapter::addThongBao);

                    int total = response.body().getPagination() != null ? response.body().getPagination().getTotal() : 0;
                    isLastPage = currentPage * pageSize >= total;

                    updateEmptyState();
                    adapter.setShowFooter(!list.isEmpty());
                } else {
                    Toast.makeText(NotificationActivity.this, "Lỗi tải thông báo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<model_ThongBaoListResponse> call, Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                Toast.makeText(NotificationActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Thực hiện tải thêm dữ liệu phân trang
    private void loadMoreThongBao() {
        currentPage++;
        loadThongBao();
    }

    // Gọi API tìm kiếm theo từ khóa
    private void searchThongBao() {
        if (isLoading) return;
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);

        apiService.searchThongBao("Bearer " + token, searchKeyword, currentPage, pageSize)
                .enqueue(new Callback<model_ThongBaoListResponse>() {
                    @Override
                    public void onResponse(Call<model_ThongBaoListResponse> call, Response<model_ThongBaoListResponse> response) {
                        isLoading = false;
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            adapter.setThongBaoList(response.body().getData());
                            updateEmptyState();
                        }
                    }

                    @Override
                    public void onFailure(Call<model_ThongBaoListResponse> call, Throwable t) {
                        isLoading = false;
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(NotificationActivity.this, "Lỗi tìm kiếm: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Sựu kiện click 1 thông báo
    private void openThongBaoDetail(model_ThongBao thongBao, int position) {
        if (thongBao.isChuaDoc()) markAsRead(thongBao.getMaThongBao(), position);
        Intent intent = new Intent(this, ThongBaoDetaiActivity.class);
        intent.putExtra("thongBao", thongBao);
        startActivity(intent);
    }

    // Gửi yêu cầu API cập nhật trạng thái thông báo
    private void markAsRead(int id, int position) {
        apiService.markAsRead("Bearer " + token, id).enqueue(new Callback<GeneralResponse>() {
            @Override
            public void onResponse(Call<GeneralResponse> call, Response<GeneralResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    model_ThongBao tb = adapter.getItem(position);
                    tb.setTrangThai("Đã đọc");
                    adapter.updateThongBao(position, tb);
                }
            }
            @Override public void onFailure(Call<GeneralResponse> call, Throwable t) {}
        });
    }

    // Gọi API đánh dấu tất cả đã đọc
    private void markAllAsRead() {
        apiService.markAllAsRead("Bearer " + token).enqueue(new Callback<GeneralResponse>() {
            @Override
            public void onResponse(Call<GeneralResponse> call, Response<GeneralResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(NotificationActivity.this, "Đã đánh dấu tất cả đã đọc", Toast.LENGTH_SHORT).show();
                    resetPagination();
                    loadThongBao();
                }
            }
            @Override public void onFailure(Call<GeneralResponse> call, Throwable t) {}
        });
    }

    // Dialog xóa tất cả thông báođã đọc
    private void confirmDeleteAllRead() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc muốn xóa tất cả thông báo đã đọc?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteAllRead())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteAllRead() {
        apiService.deleteAllRead("Bearer " + token).enqueue(new Callback<GeneralResponse>() {
            @Override
            public void onResponse(Call<GeneralResponse> call, Response<GeneralResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(NotificationActivity.this, "Đã xóa thông báo đã đọc", Toast.LENGTH_SHORT).show();
                    resetPagination();
                    loadThongBao();
                }
            }
            @Override public void onFailure(Call<GeneralResponse> call, Throwable t) {}
        });
    }

    private void showOptionsDialog(model_ThongBao thongBao, int position) {
        String[] options = {"Xem chi tiết", "Đánh dấu đã đọc", "Xóa"};
        new AlertDialog.Builder(this)
                .setTitle(thongBao.getTieuDe())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: openThongBaoDetail(thongBao, position); break;
                        case 1: markAsRead(thongBao.getMaThongBao(), position); break;
                        case 2: confirmDeleteThongBao(thongBao, position); break;
                    }
                }).show();
    }

    private void confirmDeleteThongBao(model_ThongBao thongBao, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa thông báo này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteThongBao(thongBao, position))
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Gọi API xóa từng thông báo
    private void deleteThongBao(model_ThongBao thongBao, int position) {
        apiService.deleteThongBao("Bearer " + token, thongBao.getMaThongBao())
                .enqueue(new Callback<GeneralResponse>() {
                    @Override
                    public void onResponse(Call<GeneralResponse> call, Response<GeneralResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            adapter.removeThongBao(position);
                            updateEmptyState();
                            Toast.makeText(NotificationActivity.this, "Đã xóa thông báo", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(Call<GeneralResponse> call, Throwable t) {}
                });
    }

    private void resetPagination() {
        currentPage = 1;
        isLastPage = false;
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            emptyLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
