package com.example.hethongbanthuoc.feature_product.list;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hethongbanthuoc.feature_product.models.Model_Product;
import com.example.hethongbanthuoc.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class Adapter_SanPhamGrid extends RecyclerView.Adapter<Adapter_SanPhamGrid.ViewHolder> {

    private static final String TAG = "SanPhamGridAdapter";

    private final Context context;
    private List<Model_Product> sanPhamList;
    private final OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Model_Product sanPham);
    }

    public Adapter_SanPhamGrid(Context context, List<Model_Product> sanPhamList, OnProductClickListener listener) {
        this.context = context;
        this.sanPhamList = sanPhamList;
        this.listener = listener;
        Log.d(TAG, "Adapter created with " + (sanPhamList != null ? sanPhamList.size() : 0) + " items");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sanpham_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Model_Product sanPham = sanPhamList.get(position);

        Log.d(TAG, "Binding product: " + sanPham.getTenThuoc());

        // 1. Ảnh sản phẩm
        String url = sanPham.getDuongDanAnh();
        if (url != null && !url.isEmpty()) {
            Log.d(TAG, "Loading image: " + url);
            Glide.with(context)
                    .load(url)
                    .placeholder(R.drawable.noimage)
                    .error(R.drawable.noimage)
                    .into(holder.imgSanPham);
        } else {
            Log.w(TAG, "No image URL for: " + sanPham.getTenThuoc());
            holder.imgSanPham.setImageResource(R.drawable.drugs);
        }

        // 2. Tên sản phẩm
        String tenSP = sanPham.getTenThuoc();
        holder.tvTenSP.setText(tenSP != null ? tenSP : "Không có tên");
        Log.d(TAG, "Tên SP: " + tenSP);

        // 3. Giá bán
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        Double giaBan = sanPham.getGia();
        if (giaBan != null) {
            holder.tvGia.setText(nf.format(giaBan) + "đ");
            Log.d(TAG, "Giá: " + giaBan);
        } else {
            holder.tvGia.setText("Liên hệ");
        }

        // 4. Đơn vị tính
        String donVi = sanPham.getTenDonVi();
        if (donVi != null && !donVi.isEmpty()) {
            holder.tvDonViTinh.setText("/ " + donVi);
            holder.tvDonViTinh.setVisibility(View.VISIBLE);
        } else {
            holder.tvDonViTinh.setVisibility(View.GONE);
        }

        // 5. Mô tả đơn vị
        String moTaDonVi = sanPham.getMoTaDonVi();
        if (moTaDonVi != null && !moTaDonVi.isEmpty()) {
            holder.tvMoTaDonVi.setText(moTaDonVi);
            holder.tvMoTaDonVi.setVisibility(View.VISIBLE);
            Log.d(TAG, "Mô tả đơn vị: " + moTaDonVi);
        } else {
            holder.tvMoTaDonVi.setVisibility(View.GONE);
        }

        // 6. Click nút chọn mua
        holder.btnChonMua.setOnClickListener(v -> {
            if (listener != null) {
                Log.d(TAG, "Click chọn mua: " + sanPham.getTenThuoc());
                listener.onProductClick(sanPham);
            }
        });

        // 7. Click toàn bộ card
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                Log.d(TAG, "Click card: " + sanPham.getTenThuoc());
                listener.onProductClick(sanPham);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sanPhamList != null ? sanPhamList.size() : 0;
    }

    public void updateData(List<Model_Product> newList) {
        this.sanPhamList = newList;
        notifyDataSetChanged();
        Log.d(TAG, "Data updated: " + (newList != null ? newList.size() : 0) + " items");
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSanPham;
        TextView tvTenSP;
        TextView tvGia;
        TextView tvDonViTinh;
        TextView tvMoTaDonVi;
        Button btnChonMua;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgSanPham = itemView.findViewById(R.id.imgSanPham);
            tvTenSP = itemView.findViewById(R.id.tvTenSP);
            tvGia = itemView.findViewById(R.id.tvGia);
            tvDonViTinh = itemView.findViewById(R.id.tvDonViTinh);
            tvMoTaDonVi = itemView.findViewById(R.id.tvMoTaDonVi);
            btnChonMua = itemView.findViewById(R.id.btnChonMua);

            // Kiểm tra null
            if (imgSanPham == null) Log.e("ViewHolder", "imgSanPham is NULL!");
            if (tvTenSP == null) Log.e("ViewHolder", "tvTenSP is NULL!");
            if (tvGia == null) Log.e("ViewHolder", "tvGia is NULL!");
            if (tvDonViTinh == null) Log.e("ViewHolder", "tvDonViTinh is NULL!");
            if (tvMoTaDonVi == null) Log.e("ViewHolder", "tvMoTaDonVi is NULL!");
            if (btnChonMua == null) Log.e("ViewHolder", "btnChonMua is NULL!");
        }
    }
}