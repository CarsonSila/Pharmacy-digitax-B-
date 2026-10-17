package com.example.hethongbanthuoc.feature_shopping.order;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.hethongbanthuoc.feature_shopping.cart.models.Model_ChiTietGioHang;
import com.example.hethongbanthuoc.R;

import java.text.DecimalFormat;
import java.util.List;

public class XacNhanAdapter extends RecyclerView.Adapter<XacNhanAdapter.ViewHolder> {

    private final Context context;
    private final List<Model_ChiTietGioHang> list;
    private final DecimalFormat df = new DecimalFormat("#,###");

    public XacNhanAdapter(Context context, List<Model_ChiTietGioHang> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_xacnhan_sanpham, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Model_ChiTietGioHang item = list.get(position);
        if (item == null) return;

        holder.tvName.setText(item.getTenSanPham());
        holder.tvQty.setText("x" + item.getSoLuong());
        holder.tvPrice.setText(df.format(item.getDonGia()) + "đ");
        holder.tvSub.setText(df.format(item.getDonGia() * item.getSoLuong()) + "đ");

        String imageUrl = item.getHinhAnh();
        if (imageUrl != null && imageUrl.contains("drive.google.com") && imageUrl.contains("export=view")) {
            imageUrl = imageUrl.replace("export=view", "export=download");
        }

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.gift)
                .error(R.drawable.gift)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.ivImage);
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvQty, tvPrice, tvSub;

        ViewHolder(View v) {
            super(v);
            ivImage = v.findViewById(R.id.ivImage);
            tvName = v.findViewById(R.id.tvName);
            tvQty = v.findViewById(R.id.tvQty);
            tvPrice = v.findViewById(R.id.tvPrice);
            tvSub = v.findViewById(R.id.tvSub);
        }
    }
}
