package com.example.hethongbanthuoc.feature_home.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hethongbanthuoc.feature_home.models.Model_Category;
import com.example.hethongbanthuoc.R;

import java.util.List;

public class Adapter_DanhMucCon extends RecyclerView.Adapter<Adapter_DanhMucCon.ViewHolder> {

    private List<Model_Category> danhMucList;
    private OnDanhMucClickListener listener;
    private int selectedPosition = -1; // -1 = không chọn item nào (hiển thị tất cả)

    public interface OnDanhMucClickListener {
        void onDanhMucClick(Model_Category danhMuc);
    }

    public Adapter_DanhMucCon(List<Model_Category> danhMucList, OnDanhMucClickListener listener) {
        this.danhMucList = danhMucList;
        this.listener = listener;
    }

    /**
     * Set position được chọn từ bên ngoài (từ CategoryActivity)
     * @param position - vị trí cần highlight
     */
    public void setSelectedPosition(int position) {
        // Kiểm tra position hợp lệ
        if (position < -1 || position >= getItemCount()) {
            return; // Bỏ qua nếu position không hợp lệ
        }

        int oldPosition = selectedPosition;
        selectedPosition = position;

        // Chỉ notify nếu position hợp lệ và khác nhau
        if (oldPosition != selectedPosition) {
            if (oldPosition >= 0 && oldPosition < getItemCount()) {
                notifyItemChanged(oldPosition);
            }
            if (selectedPosition >= 0 && selectedPosition < getItemCount()) {
                notifyItemChanged(selectedPosition);
            }
        }
    }

    /**
     * Lấy position hiện tại đang được chọn
     * @return selected position hoặc -1 nếu không có
     */
    public int getSelectedPosition() {
        return selectedPosition;
    }

    /**
     * Reset selection (không item nào được chọn)
     */
    public void clearSelection() {
        int oldPosition = selectedPosition;
        selectedPosition = -1;

        if (oldPosition >= 0 && oldPosition < getItemCount()) {
            notifyItemChanged(oldPosition);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_danhmuc_con, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Model_Category danhMuc = danhMucList.get(position);
        holder.tvTenDanhMuc.setText(danhMuc.getTenDanhMuc());

        // --- Load icon ---
        if (danhMuc.getIcon() != null && !danhMuc.getIcon().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(danhMuc.getIcon())
                    .placeholder(R.drawable.drugs)
                    .error(R.drawable.drugs)
                    .into(holder.imgIconDanhMuc);
        } else {
            holder.imgIconDanhMuc.setImageResource(R.drawable.drugs);
        }

        // --- Hiệu ứng chọn ---
        if (selectedPosition == position) {
            // Item được chọn → Màu primary
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));
            holder.tvTenDanhMuc.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
            holder.imgIconDanhMuc.setColorFilter(
                    ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
        } else {
            // Item bình thường → Màu trắng
            holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
            holder.tvTenDanhMuc.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
            holder.imgIconDanhMuc.setColorFilter(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));
        }

        // --- Click item ---
        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();

            // Kiểm tra position hợp lệ
            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }

            // Lưu position cũ
            int oldPosition = selectedPosition;

            // Update position mới
            selectedPosition = currentPosition;

            // Refresh UI chỉ khi position thay đổi
            if (oldPosition != selectedPosition) {
                if (oldPosition >= 0 && oldPosition < getItemCount()) {
                    notifyItemChanged(oldPosition); // Bỏ highlight item cũ
                }
                notifyItemChanged(selectedPosition); // Highlight item mới
            }

            // Trigger callback
            if (listener != null) {
                listener.onDanhMucClick(danhMuc);
            }
        });
    }

    @Override
    public int getItemCount() {
        return danhMucList != null ? danhMucList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imgIconDanhMuc;
        TextView tvTenDanhMuc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            imgIconDanhMuc = itemView.findViewById(R.id.imgIconDanhMuc);
            tvTenDanhMuc = itemView.findViewById(R.id.tvTenDanhMuc);
        }
    }
}