package com.example.hethongbanthuoc.feature_product.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hethongbanthuoc.feature_product.models.Model_TuKhoaTimKiem;
import com.example.hethongbanthuoc.R;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class Adapter_TrendingKeyword extends RecyclerView.Adapter<Adapter_TrendingKeyword.ViewHolder> {

    private List<Model_TuKhoaTimKiem> listTrending;
    private OnKeywordClickListener listener;

    public interface OnKeywordClickListener {
        void onKeywordClick(String keyword);
    }

    public Adapter_TrendingKeyword(OnKeywordClickListener listener) {
        this.listTrending = new ArrayList<>();
        this.listener = listener;
    }

    public void setData(List<Model_TuKhoaTimKiem> list) {
        this.listTrending = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trending_keyword, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Model_TuKhoaTimKiem item = listTrending.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return listTrending != null ? listTrending.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        Chip chipKeyword;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chipKeyword = itemView.findViewById(R.id.chipKeyword);
        }

        public void bind(Model_TuKhoaTimKiem item, OnKeywordClickListener listener) {
            chipKeyword.setText(item.getTuKhoa());

            chipKeyword.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onKeywordClick(item.getTuKhoa());
                }
            });
        }
    }
}