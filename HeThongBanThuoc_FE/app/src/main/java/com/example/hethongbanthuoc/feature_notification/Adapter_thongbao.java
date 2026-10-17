package com.example.hethongbanthuoc.feature_notification;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.feature_notification.models.model_ThongBao;

import java.util.ArrayList;
import java.util.List;

public class Adapter_thongbao extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_FOOTER = 1;

    private Context context;
    private List<model_ThongBao> thongBaoList;
    private OnThongBaoClickListener listener;
    private OnFooterClickListener footerListener;
    private boolean showFooter = false;

    // Interface cho click listener
    public interface OnThongBaoClickListener {
        void onThongBaoClick(model_ThongBao thongBao, int position);
        void onThongBaoLongClick(model_ThongBao thongBao, int position);
    }

    // Interface cho footer
    public interface OnFooterClickListener {
        void onDeleteAllReadClick();
    }

    public Adapter_thongbao(Context context) {
        this.context = context;
        this.thongBaoList = new ArrayList<>();
    }

    public void setOnThongBaoClickListener(OnThongBaoClickListener listener) {
        this.listener = listener;
    }

    public void setOnFooterClickListener(OnFooterClickListener listener) {
        this.footerListener = listener;
    }

    public void setThongBaoList(List<model_ThongBao> list) {
        this.thongBaoList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addThongBao(model_ThongBao thongBao) {
        this.thongBaoList.add(0, thongBao);
        notifyItemInserted(0);
    }

    public void updateThongBao(int position, model_ThongBao thongBao) {
        if (position >= 0 && position < thongBaoList.size()) {
            thongBaoList.set(position, thongBao);
            notifyItemChanged(position);
        }
    }

    public void removeThongBao(int position) {
        if (position >= 0 && position < thongBaoList.size()) {
            thongBaoList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clearAll() {
        int size = thongBaoList.size();
        thongBaoList.clear();
        notifyItemRangeRemoved(0, size);
    }

    // Hiển thị footer nếu có thông báo
    public void setShowFooter(boolean showFooter) {
        this.showFooter = showFooter;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (showFooter && position == thongBaoList.size()) {
            return VIEW_TYPE_FOOTER;
        }
        return VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_FOOTER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_thong_bao_footer, parent, false);
            return new FooterViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_thong_bao, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FooterViewHolder) {
            FooterViewHolder footerHolder = (FooterViewHolder) holder;

            footerHolder.tvSeenAll.setText("Bạn đã xem hết danh sách");
            footerHolder.tvDeleteAll.setText("Xóa tất cả thông báo đã đọc");

            footerHolder.tvDeleteAll.setOnClickListener(v -> {
                if (footerListener != null) footerListener.onDeleteAllReadClick();
            });
        } else if (holder instanceof ViewHolder) {
            model_ThongBao thongBao = thongBaoList.get(position);
            ViewHolder viewHolder = (ViewHolder) holder;

            // Set dữ liệu
            viewHolder.tvTieuDe.setText(thongBao.getTieuDe());
            viewHolder.tvNoiDung.setText(thongBao.getNoiDung());
            viewHolder.tvLoaiThongBao.setText(thongBao.getLoaiThongBao());
            viewHolder.tvThoiGian.setText(thongBao.getTimeAgo());

            // Set icon theo loại
            viewHolder.ivIcon.setImageResource(thongBao.getIconResource());

            // Set màu sắc theo loại
            int color = ContextCompat.getColor(context, thongBao.getColorResource());
            viewHolder.ivIcon.setColorFilter(color);

            // Hiển thị indicator chưa đọc
            if (thongBao.isChuaDoc()) {
                viewHolder.viewIndicator.setVisibility(View.VISIBLE);
                viewHolder.tvTieuDe.setTypeface(null, Typeface.BOLD);
                viewHolder.cardView.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.unread_background)
                );
            } else {
                viewHolder.viewIndicator.setVisibility(View.GONE);
                viewHolder.tvTieuDe.setTypeface(null, Typeface.NORMAL);
                viewHolder.cardView.setCardBackgroundColor(
                        ContextCompat.getColor(context, android.R.color.white)
                );
            }

            // Click listeners
            viewHolder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onThongBaoClick(thongBao, position);
            });

            viewHolder.itemView.setOnLongClickListener(v -> {
                if (listener != null) listener.onThongBaoLongClick(thongBao, position);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return thongBaoList.size() + (showFooter ? 1 : 0);
    }

    public model_ThongBao getItem(int position) {
        return thongBaoList.get(position);
    }

    public List<model_ThongBao> getAllItems() {
        return thongBaoList;
    }

    // ViewHolder cho item thông báo
    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivIcon;
        TextView tvTieuDe, tvNoiDung, tvLoaiThongBao, tvThoiGian;
        View viewIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvTieuDe = itemView.findViewById(R.id.tvTieuDe);
            tvNoiDung = itemView.findViewById(R.id.tvNoiDung);
            tvLoaiThongBao = itemView.findViewById(R.id.tvLoaiThongBao);
            tvThoiGian = itemView.findViewById(R.id.tvThoiGian);
            viewIndicator = itemView.findViewById(R.id.viewIndicator);
        }
    }

    // ViewHolder cho footer
    static class FooterViewHolder extends RecyclerView.ViewHolder {
        TextView tvSeenAll, tvDeleteAll;

        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSeenAll = itemView.findViewById(R.id.tvSeenAll);
            tvDeleteAll = itemView.findViewById(R.id.tvDeleteAll);
        }
    }
}
