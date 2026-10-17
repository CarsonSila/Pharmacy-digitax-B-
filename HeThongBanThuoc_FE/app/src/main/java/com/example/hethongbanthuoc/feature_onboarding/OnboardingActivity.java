package com.example.hethongbanthuoc.feature_onboarding;

import android.os.Bundle;
import android.view.View;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.hethongbanthuoc.feature_onboarding.models.Model_Onboarding;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.core.utils.CustomPageTransformer;
import com.example.hethongbanthuoc.feature_auth.login.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private OnboardingAdapter adapter;
    private MaterialButton btnBack, btnNext, btnSkip;
    private DotsIndicator dotsIndicator;
    private List<Model_Onboarding> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        initViews();
        setupOnboardingItems();
        setupViewPager();
        setupClickListeners();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);
        dotsIndicator = findViewById(R.id.dotsIndicator);
    }

    private void setupOnboardingItems() {
        items = new ArrayList<>();
        items.add(new Model_Onboarding(
                R.raw.confirmed,
                "Đặt thuốc nhanh chóng",
                "Chỉ vài thao tác đơn giản, bạn có thể tìm kiếm và đặt mua thuốc ngay tại nhà."
        ));
        items.add(new Model_Onboarding(
                R.raw.registro,
                "Theo dõi đơn hàng tiện lợi",
                "Cập nhật trạng thái đơn hàng liên tục, giúp bạn yên tâm chờ nhận thuốc."
        ));
        items.add(new Model_Onboarding(
                R.raw.runner,
                "Bắt đầu ngay hôm nay",
                "Khám phá ứng dụng mua thuốc thông minh, chăm sóc sức khỏe tốt hơn."
        ));
    }

    private void setupViewPager() {
        adapter = new OnboardingAdapter(items);
        viewPager.setAdapter(adapter);
        dotsIndicator.attachTo(viewPager);

        // Gắn hiệu ứng chuyển trang
        viewPager.setPageTransformer(new CustomPageTransformer());

        // Xử lý thay đổi trang
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateButtonStates(position);
            }
        });
    }

    private void updateButtonStates(int position) {
        boolean isFirstPage = position == 0;
        boolean isLastPage = position == items.size() - 1;

        // Update Back button visibility
        btnBack.setVisibility(isFirstPage ? View.GONE : View.VISIBLE);

        // Update Skip button visibility
        btnSkip.setVisibility(isLastPage ? View.GONE : View.VISIBLE);

        // Update Next button text and icon
        if (isLastPage) {
            btnNext.setText("Bắt đầu");
            btnNext.setIconResource(R.drawable.checklist); // Thay icon khi là trang cuối
        } else {
            btnNext.setText("Tiếp tục");
            btnNext.setIconResource(R.drawable.forwad);
        }

    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem > 0) {
                viewPager.setCurrentItem(currentItem - 1, true);
            }
        });

        // Next button
        btnNext.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem < items.size() - 1) {
                // Chuyển sang trang tiếp theo
                viewPager.setCurrentItem(currentItem + 1, true);
            } else {
                // Trang cuối - chuyển đến màn hình đăng nhập
                navigateToLogin();
            }
        });

        // Skip button
        btnSkip.setOnClickListener(v -> navigateToLogin());
    }

    private void navigateToLogin() {
        Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
        // Thêm animation chuyển màn hình (optional)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup
        if (viewPager != null) {
            viewPager.unregisterOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {});
        }
    }
}