package com.example.hethongbanthuoc.core.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CurvedBottomNavigationView extends BottomNavigationView {

    private Path path;
    private Paint paint;

    // bán kính lõm (chỉnh cho vừa FAB)
    private final int CURVE_CIRCLE_RADIUS = 128;

    // khoảng offset để kéo viền cong lên cao hơn (âm = kéo lên, dương = kéo xuống)
    private final int OFFSET_Y = -80;

    private int navigationBarWidth;
    private int navigationBarHeight;

    private int fabX; // toạ độ X của lõm
    private int fabY; // toạ độ Y của lõm

    public CurvedBottomNavigationView(Context context) {
        super(context);
        init();
    }

    public CurvedBottomNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CurvedBottomNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        path = new Path();
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(0xFFFFFFFF); // màu nền (trắng)
        paint.setAntiAlias(true);
        setBackground(null); // xoá background mặc định để vẽ custom
        setElevation(8f);    // thêm bóng nhẹ
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        navigationBarWidth = getWidth();
        navigationBarHeight = getHeight();

        fabX = navigationBarWidth / 2;
        fabY = 0; // lõm bắt đầu từ top
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // vẽ icon/text trước
        super.onDraw(canvas);

        // vẽ nền cong sau cùng (ở dưới)
        path.reset();
        path.moveTo(0, OFFSET_Y);
        path.lineTo(fabX - CURVE_CIRCLE_RADIUS, OFFSET_Y);

        path.quadTo(
                fabX, CURVE_CIRCLE_RADIUS * 1.5f + OFFSET_Y,
                fabX + CURVE_CIRCLE_RADIUS, OFFSET_Y
        );

        path.lineTo(navigationBarWidth, OFFSET_Y);
        path.lineTo(navigationBarWidth, navigationBarHeight + 50 );
        path.lineTo(0, navigationBarHeight + 50);
        path.close();

        canvas.drawPath(path, paint);
    }

}
