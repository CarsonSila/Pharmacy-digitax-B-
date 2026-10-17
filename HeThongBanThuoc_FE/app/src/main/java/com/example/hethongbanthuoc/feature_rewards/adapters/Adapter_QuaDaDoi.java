package com.example.hethongbanthuoc.feature_rewards.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hethongbanthuoc.feature_rewards.models.Model_LichSuDoiQua;
import com.example.hethongbanthuoc.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Adapter_QuaDaDoi extends RecyclerView.Adapter<Adapter_QuaDaDoi.ViewHolder> {

    private Context context;
    private List<Model_LichSuDoiQua> list;
    private List<Model_LichSuDoiQua> listFull; // Để filter
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onLienHeClick(Model_LichSuDoiQua qua);
    }

    public Adapter_QuaDaDoi(Context context) {
        this.context = context;
        this.list = new ArrayList<>();
        this.listFull = new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<Model_LichSuDoiQua> newList) {
        this.list.clear();
        this.listFull.clear();
        if (newList != null) {
            this.list.addAll(newList);
            this.listFull.addAll(newList);
        }
        notifyDataSetChanged();
    }

    public void filterByTrangThai(String trangThai) {
        list.clear();
        if (trangThai == null || trangThai.equals("TAT_CA")) {
            list.addAll(listFull);
        } else {
            for (Model_LichSuDoiQua qua : listFull) {
                if (trangThai.equals(qua.getTrangThaiNormalized())) {
                    list.add(qua);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_qua_da_doi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Model_LichSuDoiQua qua = list.get(position);

        // Bind dữ liệu
        holder.tvNgayDoi.setText(qua.getNgayDoiFormatted());
        holder.tvTenQuaTang.setText(qua.getTenQuaTang());
        holder.tvSoLuong.setText("x" + qua.getSoLuong());

        // Format điểm
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        holder.tvDiemSuDung.setText(formatter.format(qua.getDiemSuDung()) + " điểm");

        // Hiển thị hình ảnh
        Glide.with(context)
                .load(qua.getHinhAnh())
                .placeholder(R.drawable.gift)
                .error(R.drawable.gift)
                .into(holder.imgQuaTang);

        // Hiển thị trạng thái với màu sắc khác nhau
        holder.tvTrangThai.setText(qua.getTrangThaiText());
        setStatusStyle(holder.tvTrangThai, qua.getTrangThaiNormalized());

        // Hiển thị ghi chú nếu có
        if (qua.getGhiChu() != null && !qua.getGhiChu().isEmpty()) {
            holder.tvGhiChu.setVisibility(View.VISIBLE);
            holder.tvGhiChu.setText("Ghi chú: " + qua.getGhiChu());
            holder.tvGhiChu.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        } else {
            holder.tvGhiChu.setVisibility(View.GONE);
        }

        // Hiển thị lý do từ chối nếu có
        if ("TU_CHOI".equals(qua.getTrangThaiNormalized()) &&
                qua.getLyDoTuChoi() != null && !qua.getLyDoTuChoi().isEmpty()) {
            holder.tvGhiChu.setVisibility(View.VISIBLE);
            holder.tvGhiChu.setText("Lý do từ chối: " + qua.getLyDoTuChoi());
            holder.tvGhiChu.setTextColor(ContextCompat.getColor(context, R.color.red_error));
        }

        // Xử lý sự kiện button liên hệ
        holder.btnLienHe.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLienHeClick(qua);
            }
        });
    }

    private void setStatusStyle(TextView tvStatus, String trangThai) {
        if (trangThai == null) trangThai = "CHO_XU_LY";

        switch (trangThai) {
            case "CHO_XU_LY":
                tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.orange_warning));
                break;
            case "DA_DUYET":
                tvStatus.setBackgroundResource(R.drawable.bg_status_success);
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.green_success));
                break;
            case "TU_CHOI":
                tvStatus.setBackgroundResource(R.drawable.bg_status_rejected);
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.red_error));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvNgayDoi, tvTrangThai, tvTenQuaTang, tvSoLuong, tvDiemSuDung, tvGhiChu;
        ImageView imgQuaTang;
        MaterialButton btnLienHe;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvNgayDoi = itemView.findViewById(R.id.tvNgayDoi);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            tvTenQuaTang = itemView.findViewById(R.id.tvTenQuaTang);
            tvSoLuong = itemView.findViewById(R.id.tvSoLuong);
            tvDiemSuDung = itemView.findViewById(R.id.tvDiemSuDung);
            tvGhiChu = itemView.findViewById(R.id.tvGhiChu);
            imgQuaTang = itemView.findViewById(R.id.imgQuaTang);
            btnLienHe = itemView.findViewById(R.id.btnLienHe);
        }
    }
}