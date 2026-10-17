package com.example.hethongbanthuoc.feature_profile.account;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hethongbanthuoc.feature_home.adapters.Adapter_MenuParent;
import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.feature_home.models.Model_Category;
import com.example.hethongbanthuoc.core.models.Model_User;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.core.utils.TokenManager;
import com.example.hethongbanthuoc.feature_home.CategoryActivity;

import java.text.DecimalFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Dialog_menu_sidebar extends Dialog {

    private static final String TAG = "MenuSidebarDialog";

    private CircleImageView imgAvatar;
    private TextView tvUserName, tvDiemThuong;
    private RecyclerView recyclerViewMenu;
    private ImageView btnClose;

    private final Context context;
    private final API_AuthService apiService;
    private final String token;

    public Dialog_menu_sidebar(@NonNull Context context) {
        super(context, R.style.SlideFromLeftDialog);
        this.context = context;

        apiService = API_RetrofitClient.getRetrofitInstance(context).create(API_AuthService.class);

        String rawToken = TokenManager.getToken(context);
        if (rawToken != null && !rawToken.isEmpty()) {
            token = "Bearer " + rawToken;
        } else {
            token = "";
            Log.w(TAG, "Token không tồn tại hoặc rỗng");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog_menu_sidebar);

        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.START;
            params.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.85);
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(params);
            window.setWindowAnimations(R.style.DialogAnimation);
        }

        initViews();
        loadUserProfile();
        loadMenuCategories();
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvDiemThuong = findViewById(R.id.tvDiemThuong);
        recyclerViewMenu = findViewById(R.id.recyclerViewMenu);
        btnClose = findViewById(R.id.btnClose);

        recyclerViewMenu.setLayoutManager(new LinearLayoutManager(context));
        btnClose.setOnClickListener(v -> dismiss());
    }

    private void loadUserProfile() {
        if (token == null || token.isEmpty()) {
            showDefaultUserInfo();
            return;
        }

        apiService.getProfile(token).enqueue(new Callback<Model_User>() {
            @Override
            public void onResponse(Call<Model_User> call, Response<Model_User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Model_User user = response.body();
                    tvUserName.setText(user.getHoTen() != null ? user.getHoTen() : "Người dùng");

                    DecimalFormat formatter = new DecimalFormat("#,###");
                    tvDiemThuong.setText(formatter.format(user.getDiemTichLuy()) + " điểm thưởng");

                    if (user.getAnhDaiDien() != null && !user.getAnhDaiDien().isEmpty()) {
                        Glide.with(context)
                                .load(user.getAnhDaiDien())
                                .placeholder(R.drawable.profile)
                                .error(R.drawable.profile)
                                .into(imgAvatar);
                    }
                } else {
                    showDefaultUserInfo();
                }
            }

            @Override
            public void onFailure(Call<Model_User> call, Throwable t) {
                showDefaultUserInfo();
            }
        });
    }

    private void loadMenuCategories() {
        Log.d(TAG, "Bắt đầu load danh mục...");
        apiService.getAllDanhMuc().enqueue(new Callback<List<Model_Category>>() {
            @Override
            public void onResponse(Call<List<Model_Category>> call, Response<List<Model_Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Model_Category> categories = response.body();

                    Adapter_MenuParent adapter = new Adapter_MenuParent(categories, (danhMuc, isDanhMucCha) -> {
                        Intent intent = new Intent(context, CategoryActivity.class);

                        if (isDanhMucCha) {
                            intent.putExtra("DANHMUC_CHA_ID", danhMuc.getMaDanhMuc());
                            intent.putExtra("IS_DANHMUC_CHA", true);
                        } else {
                            intent.putExtra("DANHMUC_CON_ID", danhMuc.getMaDanhMuc());
                            intent.putExtra("DANHMUC_CHA_ID", danhMuc.getMaDanhMucCha());
                            intent.putExtra("IS_DANHMUC_CHA", false);
                        }

                        intent.putExtra("DANHMUC_TEN", danhMuc.getTenDanhMuc());
                        context.startActivity(intent);
                        dismiss();
                    });

                    recyclerViewMenu.setAdapter(adapter);
                    Log.d(TAG, "Load " + categories.size() + " danh mục thành công");
                } else {
                    Toast.makeText(context, "Không thể tải danh mục", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Model_Category>> call, Throwable t) {
                Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDefaultUserInfo() {
        tvUserName.setText("Người dùng");
        tvDiemThuong.setText("0 điểm thưởng");
    }
}
