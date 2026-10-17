package com.example.hethongbanthuoc.feature_rewards.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hethongbanthuoc.feature_rewards.models.Model_DiemThuong;
import com.example.hethongbanthuoc.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Adapter_LichSuDoiQua extends RecyclerView.Adapter<Adapter_LichSuDoiQua.ViewHolder> {

    private Context context;
    private List<Model_DiemThuong> lichSuList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onXemChiTietClick(Model_DiemThuong diemThuong, int position);
    }

    public Adapter_LichSuDoiQua(Context context, List<Model_DiemThuong> lichSuList) {
        this.context = context;
        this.lichSuList = lichSuList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lich_su_diem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Model_DiemThuong diemThuong = lichSuList.get(position);

        // Format ngày
        holder.tvNgayGiaoDich.setText(formatDate(diemThuong.getNgayTao()));

        // Set tiêu đề dựa vào loại giao dịch
        String tieuDe = getTieuDe(diemThuong.getLoaiGiaoDich());
        holder.tvTieuDe.setText(tieuDe);

        // Set mô tả
        holder.tvMoTa.setText(diemThuong.getMoTa());

        // Set chi tiết (nếu có đơn hàng)
        if (diemThuong.getMaDonHang() != null) {
            holder.tvChiTiet.setVisibility(View.VISIBLE);
            holder.tvChiTiet.setText("Đơn hàng #" + diemThuong.getMaDonHang());
        } else {
            holder.tvChiTiet.setVisibility(View.GONE);
        }

        // Set ngày hết hạn (chỉ hiển thị với điểm tích được)
        if ("Tich".equals(diemThuong.getLoaiGiaoDich()) && "HieuLuc".equals(diemThuong.getTrangThai())) {
            holder.tvNgayHetHan.setVisibility(View.VISIBLE);
            holder.tvNgayHetHan.setText("Điểm thưởng sẽ hết hạn vào ngày: " + formatDate(diemThuong.getNgayHetHan()));
        } else {
            holder.tvNgayHetHan.setVisibility(View.GONE);
        }

        // Set số điểm với màu sắc
        int soDiem = diemThuong.getSoDiem();
        if (soDiem > 0) {
            holder.tvSoDiem.setText("+ " + soDiem);
            holder.tvSoDiem.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            holder.tvSoDiem.setText(String.valueOf(soDiem));
            holder.tvSoDiem.setTextColor(Color.parseColor("#F44336")); // Red
        }

        // Click xem chi tiết
        holder.tvXemChiTiet.setOnClickListener(v -> {
            if (listener != null) {
                listener.onXemChiTietClick(diemThuong, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lichSuList != null ? lichSuList.size() : 0;
    }

    public void updateList(List<Model_DiemThuong> newList) {
        this.lichSuList = newList;
        notifyDataSetChanged();
    }

    private String getTieuDe(String loaiGiaoDich) {
        switch (loaiGiaoDich) {
            case "Tich":
                return "Mua hàng tích điểm";
            case "Tieu":
                return "Đổi quà tặng";
            case "HetHan":
                return "Điểm hết hạn";
            case "HoanLai":
                return "Hoàn điểm";
            default:
                return "Giao dịch điểm";
        }
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return "Ngày " + outputFormat.format(date);
        } catch (Exception e) {
            return dateString;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNgayGiaoDich;
        TextView tvTieuDe;
        TextView tvMoTa;
        TextView tvChiTiet;
        TextView tvXemChiTiet;
        TextView tvNgayHetHan;
        TextView tvSoDiem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNgayGiaoDich = itemView.findViewById(R.id.tvNgayGiaoDich);
            tvTieuDe = itemView.findViewById(R.id.tvTieuDe);
            tvMoTa = itemView.findViewById(R.id.tvMoTa);
            tvChiTiet = itemView.findViewById(R.id.tvChiTiet);
            tvXemChiTiet = itemView.findViewById(R.id.tvXemChiTiet);
            tvNgayHetHan = itemView.findViewById(R.id.tvNgayHetHan);
            tvSoDiem = itemView.findViewById(R.id.tvSoDiem);
        }
    }
}