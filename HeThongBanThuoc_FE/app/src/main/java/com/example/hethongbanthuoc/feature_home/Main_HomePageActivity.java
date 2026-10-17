package com.example.hethongbanthuoc.feature_home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import android.widget.TextView;
import android.view.View;
import android.text.SpannableString;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.graphics.Color;

import com.example.hethongbanthuoc.core.models.CountResponse;
import com.example.hethongbanthuoc.feature_profile.account.AccountActivity;
import com.example.hethongbanthuoc.feature_chat.advise.AdviseActivity;
import com.example.hethongbanthuoc.feature_profile.account.Dialog_menu_sidebar;
import com.example.hethongbanthuoc.feature_shopping.order.DonHangActivity;
import com.example.hethongbanthuoc.feature_notification.NotificationActivity;
import com.example.hethongbanthuoc.feature_shopping.cart.QuanLyGioHangActivity;
import com.example.hethongbanthuoc.feature_rewards.RewardsActivity;
import com.example.hethongbanthuoc.feature_home.adapters.Adapter_Banner;
import com.example.hethongbanthuoc.feature_home.adapters.Adapter_Category;
import com.example.hethongbanthuoc.feature_home.models.Model_Banner;
import com.example.hethongbanthuoc.feature_home.models.Model_Category;
import com.example.hethongbanthuoc.feature_product.search.SearchActivity;
import com.example.hethongbanthuoc.core.models.Model_User;
import com.example.hethongbanthuoc.core.utils.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.feature_product.list.Adapter_Product;
import com.example.hethongbanthuoc.feature_product.models.Model_Product;
import com.example.hethongbanthuoc.R;
import com.google.android.material.appbar.AppBarLayout;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;
import com.example.hethongbanthuoc.core.utils.CurvedBottomNavigationView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import com.example.hethongbanthuoc.feature_shopping.cart.BottomSheetChonMua;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Main_HomePageActivity extends AppCompatActivity {

    // Banner
    private ViewPager2 bannerViewPager;
    private WormDotsIndicator dotsIndicator;
    private Handler handler = new Handler();
    private Runnable runnable;

    private TextView tvHoTen, tvDiemTichLuy, tvBadge;
    private ImageView ivNotification, ivMenu, ivAvatar;

    private EditText searchBar;

    private static final int DELAY_MS = 3000; // 3 giây auto-slide

    // RecyclerView sản phẩm bán chạy
    private RecyclerView recyclerBestSeller;
    private Adapter_Product productsAdapter;

    // RecyclerView danh mục nổi bật
    private RecyclerView recyclerCategories;
    private Adapter_Category categoryAdapter;

    private CurvedBottomNavigationView bottomNav;
    private MaterialButton btnMyOrders;
    private MaterialCardView menuButton, notificationButton;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home_page);

        // 1. Ánh xạ các view
        initViews();

        // 2. Gọi API load dữ liệu
        loadBanners();       // banner
        loadBestSellers();   // sản phẩm bán chạy
        loadCategories();    // danh mục nổi bật
        loadUnreadNotifications(); // danh sách thông báo chưa đọc
        loadUserProfile(); // profile người dùng

        // 3. Thực hiện việc giữ nguyên màu cho icon, chỉ đổi màu text
        bottomNav.setItemIconTintList(null);

        // 4. Sự kiện click bottom navigation
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Toast.makeText(this, "Trang chủ", Toast.LENGTH_SHORT).show();
                return true;

            } else if (id == R.id.nav_rewards) {
                startActivity(new Intent(Main_HomePageActivity.this, RewardsActivity.class));
                return true;

            } else if (id == R.id.bottomAppBar) {
                // Slot giữa (Tư vấn)
                Intent intent = new Intent(Main_HomePageActivity.this, AdviseActivity.class);
                startActivity(intent);
                // Load hiệu ứng cho interface
                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                return true;

            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(Main_HomePageActivity.this, QuanLyGioHangActivity.class));
                return true;

            } else if (id == R.id.nav_account) {
                startActivity(new Intent(Main_HomePageActivity.this, AccountActivity.class));
                return true;
            }
            return false;
        });

        // 5. Sự kiện click FAB (Tư vấn)
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(Main_HomePageActivity.this, AdviseActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
        });

        // 6. Thêm WindowInsets để AppBarLayout tự paddingTop bằng chiều cao status bar
        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        ViewCompat.setOnApplyWindowInsetsListener(appBarLayout, (v, insets) -> {
            Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBarInsets.top, 0, 0); // chừa khoảng trống status bar
            return insets;
        });

        // 7. Thực hiện hiệu ứng thu nhỏ AppBar khi scroll
        LinearLayout logoTitleContainer = findViewById(R.id.logoTitleContainer);
        LinearLayout sloganContainer = findViewById(R.id.sloganContainer);
        MaterialCardView searchContainer = findViewById(R.id.searchContainer);

        // Dùng AppBarLayout.OnOffsetChangedListener (CollapsingToolbar)
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            int totalScrollRange = appBarLayout1.getTotalScrollRange();
            float progress = (float) Math.abs(verticalOffset) / totalScrollRange;

            // Logo + tên + slogan biến mất dần
            logoTitleContainer.setAlpha(1 - progress);
            sloganContainer.setAlpha(1 - progress);

            // Search bar scale và dịch lên
            float scale = 1 - 0.3f * progress; // giảm 30%
            searchContainer.setScaleX(scale);
            searchContainer.setScaleY(scale);
            searchContainer.setTranslationY(-progress * 50); // đẩy lên dần
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_home); // reset về Trang chủ
    }

    // Ánh xạ view
    private void initViews() {
        // Banner
        bannerViewPager = findViewById(R.id.bannerViewPager);
        dotsIndicator = findViewById(R.id.dotsIndicator);

        // Header
        tvHoTen = findViewById(R.id.tvHoTen);
        ivMenu = findViewById(R.id.ivMenu);
        tvDiemTichLuy = findViewById(R.id.tvDiemTichLuy);
        ivNotification = findViewById(R.id.ivNotification);
        tvBadge = findViewById(R.id.tvBadge);
        ivAvatar = findViewById(R.id.ivAvatar);

        // Buttons
        menuButton = findViewById(R.id.menuButton);
        notificationButton = findViewById(R.id.notificationButton);
        btnMyOrders = findViewById(R.id.btnMyOrders);

        // Search
        searchBar = findViewById(R.id.search_bar);

        // RecyclerViews
        recyclerBestSeller = findViewById(R.id.recyclerBestSeller);
        recyclerCategories = findViewById(R.id.recyclerCategories);

        // Bottom Navigation & FAB
        bottomNav = findViewById(R.id.bottomNavigationView);
        fab = findViewById(R.id.fab);

        // Xử lý sự kiện click vào icon menu
        menuButton.setOnClickListener(v -> {
            Dialog_menu_sidebar dialog = new Dialog_menu_sidebar(this);
            dialog.show();
        });

        // Sự kiện xem quản lý đơn hàng
        btnMyOrders.setOnClickListener(v -> {
            Intent intent = new Intent(Main_HomePageActivity.this, DonHangActivity.class);
            startActivity(intent);
        });

        // Sự kiện hiển thị thông báo
        notificationButton.setOnClickListener(v -> {
            Intent intent = new Intent(Main_HomePageActivity.this, NotificationActivity.class);
            startActivity(intent);
        });


        // Sản phẩm bán chạy
        recyclerBestSeller.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // TẠO ADAPTER VỚI LISTENER ĐỂ MỞ BOTTOMSHEET
        productsAdapter = new Adapter_Product(this, new ArrayList<>(), new Adapter_Product.OnProductClickListener() {
            @Override
            public void onAddToCartClick(Model_Product product) {
                showBottomSheetChonMua(product);
            }
        });
        recyclerBestSeller.setAdapter(productsAdapter);

        // Danh mục nổi bật
        recyclerCategories.setLayoutManager(new GridLayoutManager(this, 2)); // Grid 2 cột
        categoryAdapter = new Adapter_Category(this, new ArrayList<>());
        recyclerCategories.setAdapter(categoryAdapter);

        // Chuyển sang SearchActivity khi bấm vào ô tìm kiếm
        searchBar.setOnClickListener(v -> {
            Intent intent = new Intent(Main_HomePageActivity.this, SearchActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    // Lấy banner hiển thị
    private void loadBanners() {
        // Log token để debug
        String token = TokenManager.getToken(this);
        Log.d("API_TOKEN", "Token hiện tại: " + token);

        API_AuthService api = API_RetrofitClient
                .getRetrofitInstance(this)
                .create(API_AuthService.class);

        api.getBanners().enqueue(new Callback<List<Model_Banner>>() {
            @Override
            public void onResponse(Call<List<Model_Banner>> call, Response<List<Model_Banner>> response) {
                Log.d("API_BANNER", "URL: " + call.request().url());
                Log.d("API_BANNER", "HTTP CODE: " + response.code());

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        List<Model_Banner> banners = response.body();
                        Log.d("API_BANNER", "SIZE: " + banners.size());
                        for (Model_Banner b : banners) {
                            Log.d("API_BANNER", "ITEM: " + b.toString());
                        }

                        Adapter_Banner adapter = new Adapter_Banner(Main_HomePageActivity.this, banners);
                        bannerViewPager.setAdapter(adapter);

                        if (dotsIndicator != null) {
                            dotsIndicator.attachTo(bannerViewPager);
                        }

                        setupAutoSlide(banners.size());
                    } else {
                        Log.e("API_BANNER", "BODY NULL !!!");
                        Toast.makeText(Main_HomePageActivity.this, "Banner body null", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        Log.e("API_BANNER", "Error body: " + response.errorBody().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Model_Banner>> call, Throwable t) {
                Log.e("API_BANNER", "Failure: " + t.getMessage(), t);
                Toast.makeText(Main_HomePageActivity.this, "Lỗi API banner: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAutoSlide(int itemCount) {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (itemCount > 0) {
                    int current = bannerViewPager.getCurrentItem();
                    int next = (current + 1) % itemCount;
                    bannerViewPager.setCurrentItem(next, true);
                    handler.postDelayed(this, DELAY_MS);
                }
            }
        };
        handler.postDelayed(runnable, DELAY_MS);
    }

    // Lấy sản phẩm bán chạy
    private void loadBestSellers() {
        API_AuthService api = API_RetrofitClient
                .getRetrofitInstance(this)
                .create(API_AuthService.class);

        api.getBestSellers(5).enqueue(new Callback<List<Model_Product>>() {
            @Override
            public void onResponse(Call<List<Model_Product>> call, Response<List<Model_Product>> response) {
                Log.d("API_PRODUCT", "URL: " + call.request().url());
                Log.d("API_PRODUCT", "HTTP CODE: " + response.code());

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        List<Model_Product> list = response.body();
                        Log.d("API_PRODUCT", "SIZE: " + list.size());

                        // XỬ LÝ TRƯỜNG HỢP KHÔNG CÓ DỮ LIỆU
                        if (list.isEmpty()) {
                            Log.w("API_PRODUCT", "Danh sách sản phẩm bán chạy trống!");
                            Toast.makeText(Main_HomePageActivity.this,
                                    "Chưa có sản phẩm bán chạy",
                                    Toast.LENGTH_SHORT).show();
                            recyclerBestSeller.setVisibility(View.GONE);
                            return;
                        }

                        //  LOG KIỂM TRA SỐ LƯỢNG BÁN
                        for (Model_Product product : list) {
                            Log.d("API_PRODUCT", "Sản phẩm: " + product.getTenThuoc() +
                                    " - Đã bán: " + (product.getTongBan() != null ? product.getTongBan() : 0) + " sản phẩm");
                        }

                        /** CẬP NHẬT DỮ LIỆU CHO ADAPTER ĐÃ TẠO SẴN **/
                        productsAdapter.setData(list);
                        recyclerBestSeller.setVisibility(View.VISIBLE);

                    } else {
                        Log.e("API_PRODUCT", "BODY NULL !!!");
                        Toast.makeText(Main_HomePageActivity.this,
                                "Không có dữ liệu sản phẩm",
                                Toast.LENGTH_SHORT).show();
                        recyclerBestSeller.setVisibility(View.GONE);
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e("API_PRODUCT", "Error body: " + errorBody);
                        Toast.makeText(Main_HomePageActivity.this,
                                "Lỗi tải sản phẩm: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(Main_HomePageActivity.this,
                                "Lỗi không xác định",
                                Toast.LENGTH_SHORT).show();
                    }
                    recyclerBestSeller.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Model_Product>> call, Throwable t) {
                Log.e("API_PRODUCT", "Failure: " + t.getMessage(), t);
                Toast.makeText(Main_HomePageActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                recyclerBestSeller.setVisibility(View.GONE);
            }
        });
    }

    // Lấy danh mục nổi bật
    private void loadCategories() {
        API_AuthService api = API_RetrofitClient
                .getRetrofitInstance(this)
                .create(API_AuthService.class);

        api.getDanhMucNoiBat(6, 3).enqueue(new Callback<List<Model_Category>>() {
            @Override
            public void onResponse(Call<List<Model_Category>> call, Response<List<Model_Category>> response) {
                Log.d("API_CATEGORY", "URL: " + call.request().url());
                Log.d("API_CATEGORY", "HTTP CODE: " + response.code());

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        List<Model_Category> list = response.body();
                        Log.d("API_CATEGORY", "SIZE danh mục nổi bật: " + list.size());

                        for (Model_Category dm : list) {
                            Log.d("API_CATEGORY", "Danh mục: " + dm.getTenDanhMuc() +
                                    " - Sản phẩm: " + dm.getSoLuongSanPham());
                        }

                        /** CẬP NHẬT DỮ LIỆU CHO ADAPTER ĐÃ TẠO SẴN **/
                        categoryAdapter.setData(list);
                    } else {
                        Log.e("API_CATEGORY", "BODY NULL !!!");
                        Toast.makeText(Main_HomePageActivity.this, "Danh mục body null", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        Log.e("API_CATEGORY", "Error body: " + response.errorBody().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Model_Category>> call, Throwable t) {
                Log.e("API_CATEGORY", "Failure: " + t.getMessage(), t);
                Toast.makeText(Main_HomePageActivity.this, "Lỗi API danh mục: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm cập nhật badge
    private void updateNotificationBadge(int count) {
        if (count > 0) {
            tvBadge.setText(String.valueOf(count));
            tvBadge.setVisibility(View.VISIBLE);
        } else {
            tvBadge.setVisibility(View.GONE);
        }
    }

    private void loadUnreadNotifications() {
        Log.d("NOTIF_DEBUG", "loadUnreadNotifications() được gọi");
        String token = TokenManager.getToken(this);
        Log.d("NOTIF_DEBUG", "Token hiện tại: " + token);
        if (token == null || token.isEmpty()) {
            Log.e("NOTIF_DEBUG", "Token rỗng, không thể lấy thông báo.");
            updateNotificationBadge(0);
            return;
        }

        API_AuthService api = API_RetrofitClient
                .getRetrofitInstance(this)
                .create(API_AuthService.class);

        // Chuyển sang Call<ResponseBody> để parse thủ công
        api.getUnreadCountRaw("Bearer " + token)
                .enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
                    @Override
                    public void onResponse(Call<okhttp3.ResponseBody> call, retrofit2.Response<okhttp3.ResponseBody> response) {
                        Log.d("NOTIF_DEBUG", "URL: " + call.request().url());
                        Log.d("NOTIF_DEBUG", "HTTP CODE: " + response.code());

                        int count = 0;
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String jsonStr = response.body().string();
                                JSONObject json = new JSONObject(jsonStr);
                                CountResponse countResp = CountResponse.fromJson(json);
                                if (countResp.isSuccess()) {
                                    count = countResp.getUnreadCount();
                                    Log.d("NOTIF_DEBUG", "Unread notifications count: " + count);
                                } else {
                                    Log.e("NOTIF_DEBUG", "API success=false, message: " + countResp.getMessage());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                Log.e("NOTIF_DEBUG", "Response error body: " + response.errorBody().string());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        updateNotificationBadge(count);
                    }

                    @Override
                    public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                        Log.e("NOTIF_DEBUG", "API call failure: " + t.getMessage(), t);
                        updateNotificationBadge(0);
                    }
                });
    }

    private void loadUserProfile() {
        String token = TokenManager.getToken(this);
        Log.d("PROFILE_DEBUG", "Token hiện tại: " + token);

        if (token == null || token.isEmpty()) {
            Log.e("PROFILE_DEBUG", "Token rỗng, không thể lấy profile!");
            Toast.makeText(this, "Token không tồn tại", Toast.LENGTH_SHORT).show();
            return;
        }

        API_AuthService apiService = API_RetrofitClient
                .getRetrofitInstance(this)
                .create(API_AuthService.class);

        Log.d("PROFILE_DEBUG", "Gọi API profile với token");

        Call<Model_User> call = apiService.getProfile("Bearer " + token);
        call.enqueue(new Callback<Model_User>() {
            @Override
            public void onResponse(Call<Model_User> call, Response<Model_User> response) {
                Log.d("PROFILE_DEBUG", "onResponse HTTP CODE: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Model_User user = response.body();
                    Log.d("PROFILE_DEBUG", "Body nhận được: " + user.toString());

                    // Lấy tên người dùng
                    String hoTen = user.getHoTen();
                    tvHoTen.setText(hoTen != null ? hoTen : "");

                    // Lấy và định dạng điểm tích lũy
                    double diemValue = 0;
                    try {
                        Object diemObj = user.getDiemTichLuy();
                        if (diemObj instanceof Number) {
                            diemValue = ((Number) diemObj).doubleValue();
                        } else if (diemObj != null) {
                            diemValue = Double.parseDouble(diemObj.toString());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Bỏ .0 nếu là số nguyên
                    String displayDiem = (diemValue % 1 == 0)
                            ? String.valueOf((int) diemValue)
                            : String.valueOf(diemValue);

                    String text = displayDiem + " điểm thưởng";
                    SpannableString spannable = new SpannableString(text);

                    spannable.setSpan(
                            new ForegroundColorSpan(Color.GRAY),
                            displayDiem.length(),
                            text.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );

                    tvDiemTichLuy.setText(spannable);

                } else {
                    try {
                        Log.e("PROFILE_DEBUG", "Response error body: " +
                                (response.errorBody() != null ? response.errorBody().string() : "null"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(Main_HomePageActivity.this, "Không lấy được profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Model_User> call, Throwable t) {
                Log.e("PROFILE_DEBUG", "API call failure: " + t.getMessage(), t);
                Toast.makeText(Main_HomePageActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dọn dẹp handler để tránh memory leak
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    // HIỂN THỊ BOTTOMSHEET CHỌN MUA
    private void showBottomSheetChonMua(Model_Product sanPham) {
        BottomSheetChonMua bottomSheet = new BottomSheetChonMua(sanPham, new BottomSheetChonMua.OnChonMuaListener() {
            @Override
            public void onChonMua(Model_Product sp, int soLuong, String donVi) {
                Toast.makeText(Main_HomePageActivity.this,
                        "Đã thêm " + soLuong + " " + donVi + " " + sp.getTenThuoc() + " vào giỏ hàng",
                        Toast.LENGTH_SHORT).show();

                // TODO: Thêm logic thêm vào giỏ hàng thực tế ở đây
                // Ví dụ: CartManager.getInstance().addToCart(sp, soLuong, donVi);
            }
        });

        bottomSheet.show(getSupportFragmentManager(), "BottomSheetChonMua");
    }
}