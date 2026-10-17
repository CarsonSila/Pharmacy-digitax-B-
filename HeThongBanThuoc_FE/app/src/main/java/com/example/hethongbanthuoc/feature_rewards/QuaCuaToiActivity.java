package com.example.hethongbanthuoc.feature_rewards;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.feature_rewards.models.Model_LichSuDoiQua;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.feature_chat.chat.ChatActivity;
import com.example.hethongbanthuoc.core.utils.TokenManager;
import com.example.hethongbanthuoc.feature_auth.login.LoginActivity;
import com.example.hethongbanthuoc.feature_rewards.adapters.Adapter_QuaDaDoi;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuaCuaToiActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TabLayout tabLayout;
    private RecyclerView rvQuaDaDoi;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private MaterialButton btnDoiQuaNgay;

    private Adapter_QuaDaDoi adapter;
    private String currentFilter = "TAT_CA";
    private Integer maNguoiDung;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qua_cua_toi);

        // Lấy mã người dùng từ SharedPreferences qua TokenManager
        maNguoiDung = TokenManager.getUserId(this);

        if (maNguoiDung == -1) {
            // Chưa đăng nhập hoặc chưa lưu userId
            Toast.makeText(this, "Không tìm thấy mã người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            // Ví dụ: chuyển hướng về màn hình đăng nhập
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }


        initViews();
        setupRecyclerView();
        setupTabs();
        setupEvents();
        loadData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tabLayout = findViewById(R.id.tabLayout);
        rvQuaDaDoi = findViewById(R.id.rvQuaDaDoi);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        btnDoiQuaNgay = findViewById(R.id.btnDoiQuaNgay);
    }

    private void setupRecyclerView() {
        adapter = new Adapter_QuaDaDoi(this);
        rvQuaDaDoi.setLayoutManager(new LinearLayoutManager(this));
        rvQuaDaDoi.setAdapter(adapter);
        rvQuaDaDoi.setHasFixedSize(true);

        adapter.setOnItemClickListener(qua -> {
            // Hiển thị hộp thoại chọn cách liên hệ
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Chọn cách liên hệ")
                    .setItems(new CharSequence[]{"Tư vấn ngay", "Gọi điện thoại"}, (dialog, which) -> {
                        if (which == 0) {
                            // Người dùng chọn "Tư vấn ngay"
                            Intent intent = new Intent(this, ChatActivity.class);
                            intent.putExtra("MA_QUA_TANG", qua.getMaQuaTang());
                            intent.putExtra("TEN_QUA_TANG", qua.getTenQuaTang());
                            intent.putExtra("MO_TA", qua.getGhiChu());
                            intent.putExtra("DIEM_DOI", qua.getDiemCanDoiLucDoi());
                            intent.putExtra("HINH_ANH", qua.getHinhAnh());
                            startActivity(intent);

                        } else if (which == 1) {
                            // Người dùng chọn "Gọi điện thoại"
                            String soDienThoai = "1800 6928";
                            Intent callIntent = new Intent(Intent.ACTION_DIAL);
                            callIntent.setData(Uri.parse("tel:" + soDienThoai));
                            startActivity(callIntent);
                        }
                    })
                    .show();
        });

    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        currentFilter = "TAT_CA";
                        break;
                    case 1:
                        currentFilter = "CHO_XU_LY";
                        break;
                    case 2:
                        currentFilter = "DA_DUYET";
                        break;
                    case 3:
                        currentFilter = "TU_CHOI";
                        break;
                }
                adapter.filterByTrangThai(currentFilter);
                updateEmptyState();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());

        swipeRefresh.setOnRefreshListener(() -> loadData());

        btnDoiQuaNgay.setOnClickListener(v -> {
            // Quay về màn hình đổi quà
            finish();
        });
    }

    private void loadData() {
        showLoading();

        API_AuthService apiService = API_RetrofitClient.getRetrofitInstance(this)
                .create(API_AuthService.class);
        String token = TokenManager.getToken(this);

        // Gọi API lấy danh sách quà đã đổi
        Call<Map<String, Object>> call = apiService.getLichSuDoiQua("Bearer " + token, maNguoiDung);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call,
                                   @NonNull Response<Map<String, Object>> response) {
                hideLoading();
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    boolean success = body.get("success") != null && (boolean) body.get("success");

                    if (success) {
                        // Parse data thành List<LichSuDoiQua>
                        List<Model_LichSuDoiQua> quaList = parseDataToLichSuDoiQua(body.get("data"));
                        adapter.setData(quaList);
                        adapter.filterByTrangThai(currentFilter);
                        updateEmptyState();
                    } else {
                        Toast.makeText(QuaCuaToiActivity.this,
                                "Lỗi: " + body.get("message"),
                                Toast.LENGTH_SHORT).show();
                        updateEmptyState();
                    }
                } else {
                    Toast.makeText(QuaCuaToiActivity.this,
                            "Lỗi server: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                hideLoading();
                swipeRefresh.setRefreshing(false);
                Toast.makeText(QuaCuaToiActivity.this,
                        "Không thể kết nối server!",
                        Toast.LENGTH_SHORT).show();
                t.printStackTrace();
                updateEmptyState();
            }
        });
    }

    private List<Model_LichSuDoiQua> parseDataToLichSuDoiQua(Object data) {
        List<Model_LichSuDoiQua> result = new ArrayList<>();

        try {
            if (data != null) {
                // Sử dụng Gson để parse
                Gson gson = new Gson();
                String jsonData = gson.toJson(data);
                Type listType = new TypeToken<List<Model_LichSuDoiQua>>(){}.getType();
                result = gson.fromJson(jsonData, listType);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi parse dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return result;
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        rvQuaDaDoi.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            rvQuaDaDoi.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvQuaDaDoi.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data khi quay lại màn hình (sau khi đổi quà)
        if (maNguoiDung != null && maNguoiDung != 0) {
            loadData();
        }
    }
}