package com.example.hethongbanthuoc.feature_home.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.feature_home.models.Model_Category;

import java.util.List;

public class Adapter_MenuChild extends RecyclerView.Adapter<Adapter_MenuChild.ChildViewHolder> {

    private List<Model_Category> childList;
    private OnChildMenuClickListener listener;

    public interface OnChildMenuClickListener {
        void onChildMenuClick(Model_Category danhMuc);
    }

    public Adapter_MenuChild(List<Model_Category> childList, OnChildMenuClickListener listener) {
        this.childList = childList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu_child, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        Model_Category child = childList.get(position);
        holder.bind(child);
    }

    @Override
    public int getItemCount() {
        return childList != null ? childList.size() : 0;
    }

    class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView tvChildMenuName;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChildMenuName = itemView.findViewById(R.id.tvChildMenuName);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onChildMenuClick(childList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Model_Category danhMuc) {
            tvChildMenuName.setText(danhMuc.getTenDanhMuc());
        }
    }
}