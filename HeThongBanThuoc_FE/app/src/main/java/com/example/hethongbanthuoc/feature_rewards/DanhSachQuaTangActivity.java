package com.example.hethongbanthuoc.feature_rewards;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.core.utils.TokenManager;
import com.example.hethongbanthuoc.feature_rewards.adapters.Adapter_QuaTang;
import com.example.hethongbanthuoc.feature_rewards.models.Model_QuaTang;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DanhSachQuaTangActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTitle, tvDiemHeader;
    private LinearLayout layoutEmpty;
    private RecyclerView rvQuaTang;
    private SwipeRefreshLayout swipeRefresh;
    private Adapter_QuaTang adapterQuaTang;
    private List<Model_QuaTang> quaTangList = new ArrayList<>();
    private ChipGroup chipGroupFilter;
    private Chip chipTatCa, chip1500, chip3000, chip4500;
    private ProgressBar progressLoading;

    private API_AuthService apiService;
    private int userId;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_sach_qua_tang);

        initViews();
        setupRetrofit();
        setupListeners();
        loadAllData();
    }

    private void initViews() {
        // Header views
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v->finish());

        tvTitle = findViewById(R.id.tvTitle);
        tvDiemHeader = findViewById(R.id.tvDiemHeader);

        // Content views
        rvQuaTang = findViewById(R.id.rvQuaTang);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressLoading = findViewById(R.id.progressBar);

        // Filter chips
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        chipTatCa = findViewById(R.id.chipTatCa);
        chip1500 = findViewById(R.id.chip1500);
        chip3000 = findViewById(R.id.chip3000);
        chip4500 = findViewById(R.id.chip4500);

        // Setup RecyclerView Grid 2 cột
        rvQuaTang.setLayoutManager(new GridLayoutManager(this, 2));
        adapterQuaTang = new Adapter_QuaTang(this, quaTangList);

        rvQuaTang.setAdapter(adapterQuaTang);
        // Sự kiện click "Đổi quà" trên item
        rvQuaTang.setAdapter(adapterQuaTang);
        adapterQuaTang.setOnItemClickListener(new Adapter_QuaTang.OnItemClickListener() {
            @Override
            public void onDoiQuaClick(Model_QuaTang quaTang, int position) {
                try {
                    int tongDiem = Integer.parseInt(tvDiemHeader.getText().toString().replace(".", ""));
                    int maNguoiDung = TokenManager.getUserId(DanhSachQuaTangActivity.this);
                    Button_sheet_doi_quaActivity sheet = Button_sheet_doi_quaActivity.newInstance(
                            quaTang.getTenQuaTang(),
                            quaTang.getHinhAnh(),
                            quaTang.getDiemCanDoi(),
                            tongDiem,
                            quaTang.getMaQuaTang(),
                            maNguoiDung
                    );

                    sheet.show(getSupportFragmentManager(), "DoiQuaBottomSheet");
                } catch (Exception e) {
                    Toast.makeText(DanhSachQuaTangActivity.this, "Không thể mở đổi quà", Toast.LENGTH_SHORT).show();
                    Log.e("RewardsActivity", "Lỗi: " + e.getMessage());
                }
            }

            @Override
            public void onItemClick(Model_QuaTang quaTang, int position) {
                // Có thể mở trang chi tiết quà ở đây
            }
        });

        // Swipe to refresh
        swipeRefresh.setOnRefreshListener(this::loadAllData);

    }

    private void setupRetrofit() {
        token = TokenManager.getToken(this);
        userId = TokenManager.getUserId(this);
        apiService = API_RetrofitClient.getRetrofitInstance(this).create(API_AuthService.class);
    }

    private void setupListeners() {
        // Nút Back
        btnBack.setOnClickListener(v -> finish());

        // Filter chips
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                adapterQuaTang.updateList(quaTangList);
                return;
            }

            int checkedId = checkedIds.get(0);
            List<Model_QuaTang> filtered = new ArrayList<>();

            for (Model_QuaTang quaTang : quaTangList) {
                if (checkedId == R.id.chipTatCa) {
                    filtered.add(quaTang);
                } else if (checkedId == R.id.chip1500 && quaTang.getDiemCanDoi() <= 1500) {
                    filtered.add(quaTang);
                } else if (checkedId == R.id.chip3000 && quaTang.getDiemCanDoi() <= 3000) {
                    filtered.add(quaTang);
                } else if (checkedId == R.id.chip4500 && quaTang.getDiemCanDoi() <= 4500) {
                    filtered.add(quaTang);
                }
            }

            adapterQuaTang.updateList(filtered);

            // Hiển thị empty state nếu không có kết quả
            if (filtered.isEmpty()) {
                layoutEmpty.setVisibility(View.VISIBLE);
                rvQuaTang.setVisibility(View.GONE);
            } else {
                layoutEmpty.setVisibility(View.GONE);
                rvQuaTang.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadAllData() {
        swipeRefresh.setRefreshing(true);
        getDiemThuongBalance();
        getDanhSachQuaTang();
    }

    private void getDiemThuongBalance() {
        apiService.getDiemThuongBalance("Bearer " + token, userId)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Object dataObj = response.body().get("data");

                            if (dataObj instanceof Map) {
                                Map<String, Object> data = (Map<String, Object>) dataObj;
                                Object diem = data.get("tongDiem");
                                if (diem != null) {
                                    double diemValue = 0;
                                    if (diem instanceof Number) {
                                        diemValue = ((Number) diem).doubleValue();
                                    } else {
                                        try {
                                            diemValue = Double.parseDouble(diem.toString());
                                        } catch (Exception e) {
                                            diemValue = 0;
                                        }
                                    }

                                    // Format hiển thị điểm
                                    String displayValue = (diemValue % 1 == 0)
                                            ? String.valueOf((int) diemValue)
                                            : String.valueOf(diemValue);
                                    tvDiemHeader.setText(displayValue);
                                    return;
                                }
                            }
                            Toast.makeText(DanhSachQuaTangActivity.this,
                                    "Không có dữ liệu điểm thưởng", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(DanhSachQuaTangActivity.this,
                                    "Không lấy được thông tin điểm thưởng: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(DanhSachQuaTangActivity.this,
                                "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getDanhSachQuaTang() {
        progressLoading.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        apiService.getDanhSachQuaTang("Bearer " + token, null)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        swipeRefresh.setRefreshing(false);
                        progressLoading.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            Object dataObj = response.body().get("data");
                            if (dataObj instanceof List) {
                                List<Map<String, Object>> data = (List<Map<String, Object>>) dataObj;
                                quaTangList.clear();

                                for (Map<String, Object> item : data) {
                                    Model_QuaTang quatang = new Model_QuaTang();
                                    quatang.setMaQuaTang((int) ((double) item.get("maQuaTang")));
                                    quatang.setTenQuaTang((String) item.get("tenQuaTang"));
                                    quatang.setDiemCanDoi((int) ((double) item.get("diemCanDoi")));
                                    quatang.setHinhAnh((String) item.get("hinhAnh"));
                                    quaTangList.add(quatang);
                                }

                                adapterQuaTang.updateList(quaTangList);

                                // Hiển thị empty state nếu không có dữ liệu
                                if (quaTangList.isEmpty()) {
                                    layoutEmpty.setVisibility(View.VISIBLE);
                                    rvQuaTang.setVisibility(View.GONE);
                                } else {
                                    layoutEmpty.setVisibility(View.GONE);
                                    rvQuaTang.setVisibility(View.VISIBLE);
                                }
                            } else {
                                layoutEmpty.setVisibility(View.VISIBLE);
                                rvQuaTang.setVisibility(View.GONE);
                            }
                        } else {
                            layoutEmpty.setVisibility(View.VISIBLE);
                            rvQuaTang.setVisibility(View.GONE);
                            Toast.makeText(DanhSachQuaTangActivity.this,
                                    "Không lấy được danh sách quà tặng", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        swipeRefresh.setRefreshing(false);
                        progressLoading.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.VISIBLE);
                        rvQuaTang.setVisibility(View.GONE);
                        Toast.makeText(DanhSachQuaTangActivity.this,
                                "Lỗi tải quà tặng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}