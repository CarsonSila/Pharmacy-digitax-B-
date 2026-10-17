package com.example.hethongbanthuoc.feature_shopping.cart;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.feature_shopping.cart.models.Model_ChiTietGioHang;
import com.example.hethongbanthuoc.R;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/*
 * Adapter hiển thị chi tiết giỏ hàng
 * - Hiển thị sản phẩm, số lượng, đơn giá, đơn vị
 * - Cho phép tăng/giảm/xóa sản phẩm
 * - Chọn sản phẩm để thanh toán
 */
public class ChiTietGioHangAdapter extends RecyclerView.Adapter<ChiTietGioHangAdapter.ViewHolder> {

    private final Context context;
    private final List<Model_ChiTietGioHang> list;
    private final OnItemChangeListener listener;
    private final DecimalFormat df = new DecimalFormat("#,###"); // định dạng giá
    private final API_AuthService apiService;

    public interface OnItemChangeListener {
        void onQuantityChanged();        // Cập nhật tổng tiền
        void onItemSelectionChanged();   // Cập nhật trạng thái “chọn tất cả”
    }

    public ChiTietGioHangAdapter(Context context, List<Model_ChiTietGioHang> list, OnItemChangeListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;

        // Khởi tạo Retrofit + ApiService dùng context
        Retrofit retrofit = API_RetrofitClient.getRetrofitInstance(context);
        apiService = retrofit.create(API_AuthService.class);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Model_ChiTietGioHang item = list.get(position);

        // Hiển thị thông tin sản phẩm
        holder.tvName.setText(item.getTenSanPham());
        holder.tvPrice.setText(df.format(item.getDonGia()) + "đ");
        holder.tvDonViTinh.setText("/ " + (item.getDonViTinh() != null ? item.getDonViTinh() : "Đơn vị"));
        holder.tvQuantity.setText(String.valueOf(item.getSoLuong()));

        // Load ảnh sản phẩm
        Glide.with(context)
                .load(item.getHinhAnh())
                .placeholder(R.drawable.gift)
                .error(R.drawable.gift)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .skipMemoryCache(true)
                .into(holder.imgProduct);

        // Checkbox chọn sản phẩm
        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(item.isSelected());
        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setSelected(isChecked);
            if (listener != null) listener.onItemSelectionChanged();
        });

        // Nút tăng số lượng
        holder.btnPlus.setOnClickListener(v -> {
            int soLuongMoi = item.getSoLuong() + 1;
            capNhatSoLuongTrenServer(item, soLuongMoi, position);
        });

        // Nút giảm số lượng
        holder.btnMinus.setOnClickListener(v -> {
            int soLuongMoi = item.getSoLuong() - 1;
            if (soLuongMoi <= 0) {
                xacNhanXoa(item, position);
            } else {
                capNhatSoLuongTrenServer(item, soLuongMoi, position);
            }
        });

        // Nút xóa sản phẩm
        holder.btnDelete.setOnClickListener(v -> xacNhanXoa(item, position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void capNhatSoLuongTrenServer(Model_ChiTietGioHang item, int soLuongMoi, int position) {
        Map<String, Object> body = new HashMap<>();
        body.put("soLuong", soLuongMoi);
        body.put("donGia", item.getDonGia());

        apiService.updateChiTietGioHang(item.getMaCTGH(), body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    item.setSoLuong(soLuongMoi);
                    notifyItemChanged(position);
                    if (listener != null) listener.onQuantityChanged();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                // Có thể log lỗi nếu cần
            }
        });
    }

    // Hỏi xác nhận xóa sản phẩm
    private void xacNhanXoa(Model_ChiTietGioHang item, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc muốn xóa \"" + item.getTenSanPham() + "\" khỏi giỏ hàng không?")
                .setPositiveButton("Xóa", (dialog, which) -> xoaSanPhamTrenServer(item, position))
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Xóa sản phẩm trên server (không hiển thị Toast lỗi)
    private void xoaSanPhamTrenServer(Model_ChiTietGioHang item, int position) {
        apiService.deleteChiTietGioHang(item.getMaCTGH()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    list.remove(position);
                    notifyItemRemoved(position);
                    if (listener != null) {
                        listener.onItemSelectionChanged();
                        listener.onQuantityChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    // ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbSelect;
        ImageView imgProduct, btnDelete, btnPlus, btnMinus;
        TextView tvName, tvPrice, tvQuantity, tvDonViTinh;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelect = itemView.findViewById(R.id.cbSelect);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvDonViTinh = itemView.findViewById(R.id.tvDonViTinh);
        }
    }
}
