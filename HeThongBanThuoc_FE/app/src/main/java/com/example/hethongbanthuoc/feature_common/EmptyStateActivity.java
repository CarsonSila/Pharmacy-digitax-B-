package com.example.hethongbanthuoc.feature_common;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.feature_profile.address.Adapter_Address;
import com.example.hethongbanthuoc.feature_profile.models.Model_DiaChi;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.core.utils.TokenManager;
import com.example.hethongbanthuoc.feature_profile.account.AccountActivity;
import com.example.hethongbanthuoc.feature_profile.address.AddAndressActivity;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmptyStateActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayout layoutEmpty;
    private MaterialButton btnAddAddressEmpty, btnAddAddressBottom;
    private Adapter_Address adapter;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_state);

        // Ánh xạ view
        recyclerView = findViewById(R.id.recyclerViewAddresses);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        btnAddAddressEmpty = findViewById(R.id.btnAddAddressEmpty);
        btnAddAddressBottom = findViewById(R.id.btnAddAddressBottom);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v ->{
            Intent intent = new Intent(EmptyStateActivity.this, AccountActivity.class);
            startActivity(intent);
            finish();
        });

        // Load danh sách địa chỉ
        loadAddressList();

        // Sự kiện thêm địa chỉ
        btnAddAddressEmpty.setOnClickListener(v -> openAddAddress());
        btnAddAddressBottom.setOnClickListener(v -> openAddAddress());
    }

    private void openAddAddress() {
        startActivity(new Intent(this, AddAndressActivity.class));
    }

    private void loadAddressList() {
        API_AuthService api = API_RetrofitClient
                .getRetrofitInstance(this)
                .create(API_AuthService.class);

        String token = TokenManager.getToken(this);
        if (token == null || token.isEmpty()) {
            showEmptyLayout();
            Toast.makeText(this, "Vui lòng đăng nhập để xem địa chỉ", Toast.LENGTH_SHORT).show();
            return;
        }

        api.getDanhSachDiaChi("Bearer " + token).enqueue(new Callback<List<Model_DiaChi>>() {
            @Override
            public void onResponse(Call<List<Model_DiaChi>> call, Response<List<Model_DiaChi>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Model_DiaChi> diaChiList = response.body();
                    if (diaChiList.isEmpty()) {
                        showEmptyLayout();
                    } else {
                        showAddressList(diaChiList);
                    }
                } else {
                    showEmptyLayout();
                    Toast.makeText(EmptyStateActivity.this, "Không thể tải danh sách địa chỉ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Model_DiaChi>> call, Throwable t) {
                showEmptyLayout();
                Toast.makeText(EmptyStateActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmptyLayout() {
        layoutEmpty.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        btnAddAddressBottom.setVisibility(View.GONE);
    }

    private void showAddressList(List<Model_DiaChi> diaChiList) {
        layoutEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        btnAddAddressBottom.setVisibility(View.VISIBLE);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter_Address(this, diaChiList);
        recyclerView.setAdapter(adapter);
    }
}
