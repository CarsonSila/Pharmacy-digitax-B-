package com.example.hethongbanthuoc.feature_home.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.feature_home.CategoryActivity;
import com.example.hethongbanthuoc.feature_home.models.Model_Category;

import java.util.List;

public class Adapter_Category extends RecyclerView.Adapter<Adapter_Category.CategoryViewHolder> {

    private Context context;
    private List<Model_Category> categoryList;

    // Constructor
    public Adapter_Category(Context context, List<Model_Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    // Ánh xạ các view trong item_category
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCategory;
        TextView txtCategoryName, txtCategoryCount;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCategory = itemView.findViewById(R.id.imgCategory);
            txtCategoryName = itemView.findViewById(R.id.txtCategoryName);
            txtCategoryCount = itemView.findViewById(R.id.txtCategoryCount);
        }
    }

    // Tạo ra từng item riêng
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    // Gắn dữ liệu danh mục từ model
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Model_Category danhMuc = categoryList.get(position);

        // Hiển thị tên danh mục
        holder.txtCategoryName.setText(danhMuc.getTenDanhMuc());

        // Hiển thị số lượng sản phẩm
        if (danhMuc.getSoLuongSanPham() != null) {
            holder.txtCategoryCount.setText("Có " + danhMuc.getSoLuongSanPham() + " sản phẩm");
        } else {
            holder.txtCategoryCount.setText("Có 0 sản phẩm");
        }

        // Load ảnh Icon (nếu backend trả link icon)
        if (danhMuc.getIcon() != null && !danhMuc.getIcon().isEmpty()) {
            Glide.with(context)
                    .load(danhMuc.getIcon())
                    .placeholder(R.drawable.brain) // ảnh mặc định
                    .into(holder.imgCategory);
        } else {
            holder.imgCategory.setImageResource(R.drawable.brain); // fallback
        }

        // PHẦN QUAN TRỌNG: Xử lý click danh mục
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CategoryActivity.class);

            // Kiểm tra xem là danh mục cha hay danh mục con
            boolean isDanhMucCha = (danhMuc.getMaDanhMucCha() == null);

            if (isDanhMucCha) {
                // Trường hợp danh mục cha
                intent.putExtra("DANHMUC_CHA_ID", danhMuc.getMaDanhMuc());
                intent.putExtra("IS_DANHMUC_CHA", true);
            } else {
                // Trường hợp danh mục con
                intent.putExtra("DANHMUC_CON_ID", danhMuc.getMaDanhMuc());
                intent.putExtra("DANHMUC_CHA_ID", danhMuc.getMaDanhMucCha());
                intent.putExtra("IS_DANHMUC_CHA", false);
            }

            intent.putExtra("DANHMUC_TEN", danhMuc.getTenDanhMuc());
            context.startActivity(intent);

            // Thêm animation nếu context là Activity
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).overridePendingTransition(
                        R.anim.slide_in_right,  // animation vào
                        R.anim.slide_out_left   // animation ra
                );
            }
        });
    }

    // Đếm số lượng thực
    @Override
    public int getItemCount() {
        return categoryList != null ? categoryList.size() : 0;
    }

    // Hàm cập nhật lại data khi có dữ liệu mới từ API
    public void setData(List<Model_Category> list) {
        this.categoryList = list;
        notifyDataSetChanged();
    }
}