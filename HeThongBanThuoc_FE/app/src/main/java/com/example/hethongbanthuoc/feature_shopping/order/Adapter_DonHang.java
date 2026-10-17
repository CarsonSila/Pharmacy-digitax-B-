package com.example.hethongbanthuoc.feature_shopping.order;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hethongbanthuoc.feature_shopping.order.models.Model_ChiTietDonHang;
import com.example.hethongbanthuoc.feature_shopping.order.models.Model_ChiTietDonHangRequest;
import com.example.hethongbanthuoc.R;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/*
 * Adapter hiển thị danh sách đơn hàng
 *
 * Logic hiển thị nút:
 * - "Xem chi tiết": Luôn hiển thị (click vào item hoặc nút)
 * - "Mua lại": Chỉ hiển thị với đơn "Đã giao"
 */
public class Adapter_DonHang extends RecyclerView.Adapter<Adapter_DonHang.DonHangViewHolder> {

    private Context context;
    private List<Model_ChiTietDonHangRequest> danhSachDonHang;
    private OnDonHangClickListener listener;

    /*
     * Interface callback cho các sự kiện click
     */
    public interface OnDonHangClickListener {
        void onXemChiTiet(Model_ChiTietDonHangRequest donHang);
        void onHuyDonHang(Model_ChiTietDonHangRequest donHang);
        void onMuaLai(Model_ChiTietDonHangRequest donHang);
    }

    public Adapter_DonHang(Context context, OnDonHangClickListener listener) {
        this.context = context;
        this.danhSachDonHang = new ArrayList<>();
        this.listener = listener;
    }

    public void setData(List<Model_ChiTietDonHangRequest> list) {
        this.danhSachDonHang = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DonHangViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_donhang, parent, false);
        return new DonHangViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DonHangViewHolder holder, int position) {
        Model_ChiTietDonHangRequest donHang = danhSachDonHang.get(position);
        String trangThai = donHang.getTrangThai();

        // Format ngày tạo
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String ngayTao = sdf.format(donHang.getNgayTao());
        holder.tvNgayTao.setText("Đơn hàng " + ngayTao);

        // Mã đơn hàng
        holder.tvMaDonHang.setText("Nhận tại cửa hàng • #" + donHang.getMaDonHang());

        // Trạng thái với icon và màu
        holder.tvTrangThai.setText(trangThai);
        setTrangThaiStyle(holder.tvTrangThai, holder.imgTrangThai, trangThai);

        // Hiển thị sản phẩm đầu tiên
        if (donHang.getDanhSachSanPham() != null && !donHang.getDanhSachSanPham().isEmpty()) {
            Model_ChiTietDonHang sanPham = donHang.getDanhSachSanPham().get(0);

            // Tên sản phẩm
            holder.tvTenSanPham.setText(sanPham.getTenSanPham());

            // Số lượng sản phẩm khác
            int soLuongKhac = donHang.getDanhSachSanPham().size() - 1;
            if (soLuongKhac > 0) {
                holder.tvSoLuongKhac.setVisibility(View.VISIBLE);
                holder.tvSoLuongKhac.setText("+" + soLuongKhac + " sản phẩm khác");
            } else {
                holder.tvSoLuongKhac.setVisibility(View.GONE);
            }

            // Hình ảnh sản phẩm
            Glide.with(context)
                    .load(sanPham.getHinhAnh())
                    .placeholder(R.drawable.gift)
                    .error(R.drawable.gift)
                    .into(holder.imgSanPham);
        }

        // Thành tiền
        holder.tvThanhTien.setText("Thành tiền: " + formatMoney(donHang.getTienPhaiTra()));

        // LOGIC HIỂN THỊ NÚT THEO TRẠNG THÁI

        // Nút "Mua lại" - chỉ hiển thị với "Đã giao"
        if ("Đã giao".equals(trangThai)) {
            holder.btnMuaLai.setVisibility(View.VISIBLE);
            holder.btnMuaLai.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMuaLai(donHang);
                }
            });
        } else {
            holder.btnMuaLai.setVisibility(View.GONE);
        }

        // Click vào "Xem chi tiết"
        holder.layoutXemChiTiet.setOnClickListener(v -> {
            if (listener != null) {
                listener.onXemChiTiet(donHang);
            }
        });

        // Click vào toàn bộ item để xem chi tiết
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onXemChiTiet(donHang);
            }
        });

        // Long click để hiển thị menu hủy đơn (chỉ với "Đang xử lý")
        if ("Đang xử lý".equals(trangThai)) {
            holder.itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onHuyDonHang(donHang);
                }
                return true;
            });
        } else {
            holder.itemView.setOnLongClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return danhSachDonHang != null ? danhSachDonHang.size() : 0;
    }

    /*
     * Set màu và icon cho TextView trạng thái theo từng loại
     */
    private void setTrangThaiStyle(TextView tvTrangThai, ImageView imgTrangThai, String trangThai) {
        int color;

        switch (trangThai) {
            case "Đang xử lý":
                color = context.getResources().getColor(android.R.color.holo_orange_dark);
                break;
            case "Đang giao":
                color = context.getResources().getColor(android.R.color.holo_blue_dark);
                break;
            case "Đã giao":
                color = context.getResources().getColor(android.R.color.holo_green_dark);
                break;
            case "Đã hủy":
                color = context.getResources().getColor(android.R.color.holo_red_dark);
                break;
            default:
                color = context.getResources().getColor(android.R.color.darker_gray);
        }

        tvTrangThai.setTextColor(color);

        // Set tint cho icon trạng thái
        if (imgTrangThai != null) {
            imgTrangThai.setColorFilter(color);
        }
    }

    /*
     * Format tiền VND
     */
    private String formatMoney(double amount) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(amount) + "đ";
    }

    /*
     * ViewHolder cho mỗi item đơn hàng
     */
    static class DonHangViewHolder extends RecyclerView.ViewHolder {
        TextView tvNgayTao, tvMaDonHang, tvTrangThai, btnXemChiTiet;
        ImageView imgSanPham, imgTrangThai, icArrowRight;
        TextView tvTenSanPham, tvSoLuongKhac, tvThanhTien;
        Button btnMuaLai;
        LinearLayout layoutXemChiTiet;

        public DonHangViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNgayTao = itemView.findViewById(R.id.tvNgayTao);
            tvMaDonHang = itemView.findViewById(R.id.tvMaDonHang);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            imgTrangThai = itemView.findViewById(R.id.imgTrangThai);
            imgSanPham = itemView.findViewById(R.id.imgSanPham);
            tvTenSanPham = itemView.findViewById(R.id.tvTenSanPham);
            tvSoLuongKhac = itemView.findViewById(R.id.tvSoLuongKhac);
            tvThanhTien = itemView.findViewById(R.id.tvThanhTien);
            btnXemChiTiet = itemView.findViewById(R.id.btnXemChiTiet);
            btnMuaLai = itemView.findViewById(R.id.btnMuaLai);
            icArrowRight = itemView.findViewById(R.id.icArrowRight);

            // Layout chứa "Xem chi tiết"
            layoutXemChiTiet = itemView.findViewById(R.id.btnXemChiTiet).getParent() instanceof LinearLayout
                    ? (LinearLayout) itemView.findViewById(R.id.btnXemChiTiet).getParent()
                    : null;
        }
    }
}