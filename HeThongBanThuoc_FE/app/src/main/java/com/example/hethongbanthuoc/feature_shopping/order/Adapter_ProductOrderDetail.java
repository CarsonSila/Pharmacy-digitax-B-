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
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.feature_shopping.order.models.Model_ChiTietDonHang;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/*
* Adapter cho RecyclerView hiển thị danh sách sản phẩm trong chi tiết đơn hàng
 */
public class Adapter_ProductOrderDetail extends RecyclerView.Adapter<Adapter_ProductOrderDetail.ProductViewHolder> {

    private Context context;
    private List<Model_ChiTietDonHang> productList;

    // 1. Constructor
    public Adapter_ProductOrderDetail(Context context) {
        this.context = context;
        this.productList = new ArrayList<>();
    }

    public void setData(List<Model_ChiTietDonHang> list) {
        this.productList = list;
        notifyDataSetChanged(); // Làm mới khi có data mới từ API
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_order_detail, parent, false);
        return new ProductViewHolder(view);
    }

    // Bind dữ liệu vào view
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Model_ChiTietDonHang product = productList.get(position);

        // Tên sản phẩm
        holder.tvProductName.setText(product.getTenSanPham());

        // Giá sản phẩm
        holder.tvProductPrice.setText(formatMoney(product.getDonGia()));

        // Số lượng
        holder.tvProductQuantity.setText("x" + product.getSoLuong() + " Hộp");

        // Load hình ảnh
        Glide.with(context)
                .load(product.getHinhAnh())
                .placeholder(R.drawable.gift)
                .error(R.drawable.gift)
                .into(holder.imgProduct);
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    private String formatMoney(double amount) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(amount) + "đ";
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName, tvProductPrice, tvProductQuantity;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
        }
    }
}