package com.example.hethongbanthuoc.feature_rewards;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.hethongbanthuoc.feature_rewards.adapters.Adapter_LichSuDoiQua;
import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.feature_rewards.models.Model_DiemThuong;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.core.utils.TokenManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LichSuDoiQuaActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTongDiem;
    private MaterialButton btnDoiQuaNgay;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvLichSu;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;

    private Adapter_LichSuDoiQua adapter;
    private List<Model_DiemThuong> lichSuList = new ArrayList<>();
    private List<Model_DiemThuong> allList = new ArrayList<>();
    private int userId;
    private String currentFilter = "all";

    private API_AuthService apiService;
    private String token;

    private static final String TAG = "LichSuDiemActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lich_su_diem);

        initViews();
        initData();
        setupListeners();
        setupRecyclerView();
        loadData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTongDiem = findViewById(R.id.tvTongDiem);
        btnDoiQuaNgay = findViewById(R.id.btnDoiQuaNgay);
        tabLayout = findViewById(R.id.tabLayout);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        rvLichSu = findViewById(R.id.rvLichSu);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initData() {
        token = "Bearer " + TokenManager.getToken(this);
        userId = TokenManager.getUserId(this);
        apiService = API_RetrofitClient.getRetrofitInstance(this).create(API_AuthService.class);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnDoiQuaNgay.setOnClickListener(v -> {
            Intent intent = new Intent(this, DanhSachQuaTangActivity.class);
            startActivity(intent);
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentFilter = (tab.getPosition() == 0) ? "tich" : "tieu";
                filterList(tab.getPosition() == 0 ? "Tich" : "Tieu");
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        swipeRefresh.setOnRefreshListener(this::loadData);
    }

    private void setupRecyclerView() {
        adapter = new Adapter_LichSuDoiQua(this, lichSuList);
        rvLichSu.setLayoutManager(new LinearLayoutManager(this));
        rvLichSu.setAdapter(adapter);

        adapter.setOnItemClickListener((diemThuong, position) -> {
            Toast.makeText(this, "Chi tiết: " + diemThuong.getMoTa(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadData() {
        showLoading(true);
        loadDiemThuong();
        loadLichSuDiem();
    }

    private void loadDiemThuong() {
        Call<Map<String, Object>> call = apiService.getDiemThuongBalance(token, userId);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> result = response.body();
                    Boolean success = (Boolean) result.get("success");
                    if (success != null && success) {
                        Map<String, Object> data = (Map<String, Object>) result.get("data");
                        if (data != null) {
                            Object tongDiemObj = data.get("tongDiem");
                            int tongDiem = 0;
                            if (tongDiemObj instanceof Double) tongDiem = ((Double) tongDiemObj).intValue();
                            else if (tongDiemObj instanceof Integer) tongDiem = (Integer) tongDiemObj;
                            tvTongDiem.setText(String.format("%,d", tongDiem));
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
            }
        });
    }

    private void loadLichSuDiem() {
        Call<Map<String, Object>> call = apiService.getLichSuDiem(token, userId);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                showLoading(false);
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> result = response.body();
                    Boolean success = (Boolean) result.get("success");
                    if (success != null && success) {
                        List<Map<String, Object>> dataList = (List<Map<String, Object>>) result.get("data");
                        if (dataList != null && !dataList.isEmpty()) {
                            allList.clear();
                            lichSuList.clear();
                            Gson gson = new Gson();
                            for (Map<String, Object> item : dataList) {
                                String json = gson.toJson(item);
                                Model_DiemThuong diemThuong = gson.fromJson(json, Model_DiemThuong.class);
                                allList.add(diemThuong);
                            }
                            if ("tich".equals(currentFilter)) filterList("Tich");
                            else if ("tieu".equals(currentFilter)) filterList("Tieu");
                            else { lichSuList.addAll(allList); adapter.updateList(lichSuList); }
                            showEmptyState(false);
                        } else showEmptyState(true);
                    }
                } else {
                    Toast.makeText(LichSuDoiQuaActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showLoading(false);
                swipeRefresh.setRefreshing(false);
                Log.e(TAG, "Error: " + t.getMessage());
                Toast.makeText(LichSuDoiQuaActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterList(String loaiGiaoDich) {
        lichSuList.clear();
        for (Model_DiemThuong item : allList) {
            if (item.getLoaiGiaoDich().equals(loaiGiaoDich)) lichSuList.add(item);
        }
        adapter.updateList(lichSuList);
        showEmptyState(lichSuList.isEmpty());
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvLichSu.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        layoutEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        rvLichSu.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
