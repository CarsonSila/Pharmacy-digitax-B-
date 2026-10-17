package com.example.hethongbanthuoc.feature_shopping.order.utils; // bạn có thể đổi package theo dự án của mình

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Lớp tiện ích giúp bắt sự kiện click và long click cho từng item trong RecyclerView
 * mà không cần chỉnh sửa Adapter.
 */
public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {

    // Interface định nghĩa hành vi khi click / long click
    public interface OnItemClickListener {
        void onItemClick(View view, int position);      // Khi người dùng click 1 item
        void onLongItemClick(View view, int position);  // Khi người dùng nhấn giữ lâu
    }

    private OnItemClickListener mListener;
    private GestureDetector mGestureDetector;

    // Hàm khởi tạo
    public RecyclerItemClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener) {
        this.mListener = listener;

        // Sử dụng GestureDetector để phát hiện hành động chạm (click, long press)
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                // Khi người dùng chạm 1 lần (click)
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                // Khi người dùng nhấn giữ lâu
                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (childView != null && mListener != null) {
                    mListener.onLongItemClick(childView, recyclerView.getChildAdapterPosition(childView));
                }
            }
        });
    }

    // Phát hiện khi người dùng chạm vào RecyclerView
    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            // Khi click vào 1 item
            mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
            return true;
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
        // Không cần xử lý thêm
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // Không cần xử lý thêm
    }
}
