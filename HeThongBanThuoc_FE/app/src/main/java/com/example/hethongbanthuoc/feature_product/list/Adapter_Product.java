package com.example.hethongbanthuoc.feature_product.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.feature_product.models.Model_Product;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class Adapter_Product extends RecyclerView.Adapter<Adapter_Product.ProductViewHolder> {

    private Context context;
    private List<Model_Product> productList;

    private OnProductClickListener clickListener;

    public interface OnProductClickListener {
        void onAddToCartClick(Model_Product product);
    }

    // Constructor cũ - giữ nguyên
    public Adapter_Product(Context context, List<Model_Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    // CONSTRUCTOR MỚI - NHẬN LISTENER
    public Adapter_Product(Context context, List<Model_Product> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.clickListener = listener;
    }

    // METHOD SET LISTENER
    public void setOnProductClickListener(OnProductClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_best_seller, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Model_Product p = productList.get(position);
        if (p == null) return;

        // Ảnh sản phẩm
        String imageUrl = p.getDuongDanAnh();
        Glide.with(context)
                .load(imageUrl != null ? imageUrl : R.drawable.drugs)
                .placeholder(R.drawable.drugs)
                .into(holder.imgProduct);

        // Tên thuốc
        holder.txtName.setText(p.getTenThuoc() != null ? p.getTenThuoc() : "");

        // Giá (format VND)
        if (p.getGia() != null) {
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            holder.txtPrice.setText(nf.format(p.getGia()));
        } else {
            holder.txtPrice.setText("-");
        }

        // Hiển thị tên danh mục sản phẩm
        holder.txtCategory.setText(p.getTenDanhMuc() != null ? p.getTenDanhMuc() : "Không có danh mục");

        // HIỂN THỊ SỐ LƯỢNG ĐÃ BÁN
        Integer tongBan = p.getTongBan();
        if (tongBan != null && tongBan > 0) {
            holder.txtSold.setVisibility(View.VISIBLE);

            // THÊM INDICATOR "BÁN CHẠY" NẾU BÁN TRÊN 10 SẢN PHẨM
            if (tongBan >= 10) {
                holder.txtSold.setTextColor(context.getResources().getColor(R.color.orange, null));
                holder.txtSold.setText("Đã bán " + tongBan);
            } else {
                holder.txtSold.setTextColor(context.getResources().getColor(R.color.gray, null));
                holder.txtSold.setText("Đã bán " + tongBan);
            }
        } else {
            // SẢN PHẨM MỚI (CHƯA BÁN HOẶC FALLBACK)
            holder.txtSold.setVisibility(View.VISIBLE);
            holder.txtSold.setText(" Sản phẩm mới");
            holder.txtSold.setTextColor(context.getResources().getColor(R.color.green, null));
        }

        // NÚT THÊM - XỬ LÝ MỚI
        holder.btnAdd.setOnClickListener(v -> {
            // Nếu có listener, gọi callback để mở BottomSheet
            if (clickListener != null) {
                clickListener.onAddToCartClick(p);
            } else {
                // Fallback nếu không có listener (giữ nguyên hành vi cũ)
                Toast.makeText(context,
                        "Đã thêm " + (p.getTenThuoc() != null ? p.getTenThuoc() : "sản phẩm") + " vào giỏ",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgProduct;
        TextView txtCategory, txtName, txtSold, txtPrice;
        MaterialButton btnAdd;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            txtName = itemView.findViewById(R.id.txtName);
            txtSold = itemView.findViewById(R.id.txtSold);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }
    }

    public void setData(List<Model_Product> list) {
        this.productList = list;
        notifyDataSetChanged();
    }
}