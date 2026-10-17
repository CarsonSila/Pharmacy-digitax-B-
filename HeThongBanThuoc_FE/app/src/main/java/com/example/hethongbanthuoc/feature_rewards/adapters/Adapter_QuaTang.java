package com.example.hethongbanthuoc.feature_rewards.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.feature_rewards.models.Model_QuaTang;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class Adapter_QuaTang extends RecyclerView.Adapter<Adapter_QuaTang.ViewHolder> {

    private final Context context;
    private List<Model_QuaTang> quaTangList;
    private OnItemClickListener listener;

    // Interface cho các sự kiện click
    public interface OnItemClickListener {
        void onDoiQuaClick(Model_QuaTang quaTang, int position);
        void onItemClick(Model_QuaTang quaTang, int position);
    }

    public Adapter_QuaTang(Context context, List<Model_QuaTang> quaTangList) {
        this.context = context;
        this.quaTangList = quaTangList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_qua_tang, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Model_QuaTang quaTang = quaTangList.get(position);

        // Tên quà tặng
        holder.tvTenQuaTang.setText(quaTang.getTenQuaTang());

        // Điểm cần đổi
        holder.tvDiemCanDoi.setText(String.valueOf(quaTang.getDiemCanDoi()));

        // Load hình ảnh
        Glide.with(context)
                .load(quaTang.getHinhAnh())
                .placeholder(R.drawable.gift)
                .error(R.drawable.gift)
                .into(holder.imgQuaTang);

        // Sự kiện click nút "Đổi ngay"
        holder.btnDoiNgay.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDoiQuaClick(quaTang, position);
            }
        });

        // Sự kiện click vào item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(quaTang, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return quaTangList != null ? quaTangList.size() : 0;
    }

    public void updateList(List<Model_QuaTang> newList) {
        this.quaTangList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgQuaTang;
        TextView tvTenQuaTang, tvDiemCanDoi;
        MaterialButton btnDoiNgay;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgQuaTang = itemView.findViewById(R.id.imgQuaTang);
            tvTenQuaTang = itemView.findViewById(R.id.tvTenQuaTang);
            tvDiemCanDoi = itemView.findViewById(R.id.tvDiemCanDoi);
            btnDoiNgay = itemView.findViewById(R.id.btnDoiNgay);
        }
    }
}
