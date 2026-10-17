package com.example.hethongbanthuoc.core.utils;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class CustomPageTransformer implements ViewPager2.PageTransformer {

    @Override
    public void transformPage(@NonNull View page, float position) {
        float absPos = Math.abs(position);

        if (position <= -1 || position >= 1) {
            page.setAlpha(0f);
            page.setScaleX(0.85f);
            page.setScaleY(0.85f);
        } else if (position == 0) {
            page.setAlpha(1f);
            page.setScaleX(1f);
            page.setScaleY(1f);
        } else {
            page.setAlpha(1 - absPos);
            page.setScaleX(0.85f + (1 - absPos) * 0.15f);
            page.setScaleY(0.85f + (1 - absPos) * 0.15f);
            page.setRotationY(position * -30);
        }
    }
}