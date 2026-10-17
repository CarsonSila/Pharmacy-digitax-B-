package com.example.hethongbanthuoc.feature_product.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hethongbanthuoc.feature_product.models.Model_LichSuTimKiem;
import com.example.hethongbanthuoc.R;

import java.util.ArrayList;
import java.util.List;

public class Adapter_LichSuTimKiem extends RecyclerView.Adapter<Adapter_LichSuTimKiem.ViewHolder> {

    private List<Model_LichSuTimKiem> listLichSu;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Model_LichSuTimKiem item);
        void onDeleteClick(Model_LichSuTimKiem item, int position);
    }

    public Adapter_LichSuTimKiem(OnItemClickListener listener) {
        this.listLichSu = new ArrayList<>();
        this.listener = listener;
    }

    public void setData(List<Model_LichSuTimKiem> list) {
        this.listLichSu = list;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < listLichSu.size()) {
            listLichSu.remove(position);
            notifyItemRemoved(position);
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lich_su_tim_kiem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Model_LichSuTimKiem item = listLichSu.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return listLichSu != null ? listLichSu.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvKeyword;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvKeyword = itemView.findViewById(R.id.tvKeyword);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Model_LichSuTimKiem item, OnItemClickListener listener) {
            tvKeyword.setText(item.getTuKhoa());

            // Click vào item để search lại
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });

            // Click nút delete
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(item, getAdapterPosition());
                }
            });
        }
    }
}