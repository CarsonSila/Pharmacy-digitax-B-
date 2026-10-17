package com.example.hethongbanthuoc.feature_home.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.feature_home.models.Model_Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Adapter_MenuParent extends RecyclerView.Adapter<Adapter_MenuParent.ParentViewHolder> {

    private List<Model_Category> parentList;
    private Map<Integer, List<Model_Category>> childrenMap;
    private OnMenuClickListener listener;

    public interface OnMenuClickListener {
        void onMenuClick(Model_Category danhMuc, boolean isDanhMucCha);
    }

    public Adapter_MenuParent(List<Model_Category> allCategories, OnMenuClickListener listener) {
        this.listener = listener;
        this.childrenMap = new HashMap<>();
        this.parentList = new ArrayList<>();

        // Phân loại danh mục cha và con
        if (allCategories != null) {
            for (Model_Category dm : allCategories) {
                if (dm.getMaDanhMucCha() == null) {
                    // Đây là danh mục cha
                    parentList.add(dm);
                } else {
                    // Đây là danh mục con
                    Integer parentId = dm.getMaDanhMucCha();
                    if (!childrenMap.containsKey(parentId)) {
                        childrenMap.put(parentId, new ArrayList<>());
                    }
                    childrenMap.get(parentId).add(dm);
                }
            }
        }
    }

    @NonNull
    @Override
    public ParentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu_parent, parent, false);
        return new ParentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParentViewHolder holder, int position) {
        Model_Category parent = parentList.get(position);
        List<Model_Category> children = childrenMap.get(parent.getMaDanhMuc());
        holder.bind(parent, children);
    }

    @Override
    public int getItemCount() {
        return parentList != null ? parentList.size() : 0;
    }

    class ParentViewHolder extends RecyclerView.ViewHolder {
        TextView tvMenuName;
        ImageView imgExpand;
        RecyclerView recyclerViewChildren;
        boolean isExpanded = false;

        public ParentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMenuName = itemView.findViewById(R.id.tvMenuName);
            imgExpand = itemView.findViewById(R.id.imgExpand);
            recyclerViewChildren = itemView.findViewById(R.id.recyclerViewChildren);
        }

        public void bind(Model_Category parent, List<Model_Category> children) {
            tvMenuName.setText(parent.getTenDanhMuc());

            // Nếu có danh mục con
            if (children != null && !children.isEmpty()) {
                imgExpand.setVisibility(View.VISIBLE);

                // Setup RecyclerView cho menu con
                recyclerViewChildren.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                Adapter_MenuChild childAdapter = new Adapter_MenuChild(children, danhMuc -> {
                    if (listener != null) {
                        // Truyền isDanhMucCha = false (đây là danh mục con)
                        listener.onMenuClick(danhMuc, false);
                    }
                });
                recyclerViewChildren.setAdapter(childAdapter);

                // TÁCH RIÊNG LOGIC CLICK

                // 1. Click vào ICON → Expand/Collapse
                imgExpand.setOnClickListener(v -> toggleExpand());

                // 2. Click vào TÊN DANH MỤC CHA → Navigate
                tvMenuName.setOnClickListener(v -> {
                    if (listener != null) {
                        // Truyền isDanhMucCha = true (đây là danh mục cha)
                        listener.onMenuClick(parent, true);
                    }
                });

            } else {
                // Không có con → click vào để navigate
                imgExpand.setVisibility(View.GONE);
                itemView.findViewById(R.id.layoutMenuParent).setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onMenuClick(parent, true);
                    }
                });
            }
        }

        // Toggle expand/collapse
        private void toggleExpand() {
            isExpanded = !isExpanded;
            if (isExpanded) {
                recyclerViewChildren.setVisibility(View.VISIBLE);
                imgExpand.setRotation(180);
            } else {
                recyclerViewChildren.setVisibility(View.GONE);
                imgExpand.setRotation(0);
            }
        }
    }
}