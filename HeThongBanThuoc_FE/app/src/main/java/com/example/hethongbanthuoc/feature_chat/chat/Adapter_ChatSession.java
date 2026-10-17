package com.example.hethongbanthuoc.feature_chat.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hethongbanthuoc.feature_chat.models.model_PhienTuVan;
import com.example.hethongbanthuoc.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/*
 * Adapter hiển thị danh sách phiên chat
 */
public class Adapter_ChatSession extends RecyclerView.Adapter<Adapter_ChatSession.SessionViewHolder> {

    private final List<model_PhienTuVan> sessions = new ArrayList<>();
    private OnSessionActionListener listener;

    /*
     * Interface callback cho các hành động trên session
     */
    public interface OnSessionActionListener {
        void onSessionClick(model_PhienTuVan session);        // Click vào session để xem chi tiết
        void onEndSession(model_PhienTuVan session);          // Kết thúc phiên
        void onDeleteSession(model_PhienTuVan session);       // Xóa phiên
    }

    public Adapter_ChatSession(OnSessionActionListener listener) {
        this.listener = listener;
    }

    /*
     * Xóa toàn bộ danh sách phiên
     */
    public void clearSessions() {
        sessions.clear();
        notifyDataSetChanged();
    }

    /*
     * Cập nhật danh sách sessions
     */
    public void setSessions(List<model_PhienTuVan> newSessions) {
        sessions.clear();
        if (newSessions != null) {
            sessions.addAll(newSessions);
        }
        notifyDataSetChanged();
    }

    /*
     * Thêm session mới lên đầu danh sách
     */
    public void addSessionAtTop(model_PhienTuVan session) {
        sessions.add(0, session);
        notifyItemInserted(0);
    }

    /*
     * Xóa session khỏi danh sách
     */
    public void removeSession(model_PhienTuVan session) {
        int position = sessions.indexOf(session);
        if (position != -1) {
            sessions.remove(position);
            notifyItemRemoved(position);
        }
    }

    /*
     * Cập nhật trạng thái session
     */
    public void updateSession(model_PhienTuVan session) {
        int position = findSessionPosition(session.getMaPhienTuVan());
        if (position != -1) {
            sessions.set(position, session);
            notifyItemChanged(position);
        }
    }

    private int findSessionPosition(int sessionId) {
        for (int i = 0; i < sessions.size(); i++) {
            if (sessions.get(i).getMaPhienTuVan() == sessionId) {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        model_PhienTuVan session = sessions.get(position);
        holder.bind(session, listener);
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    // ========================= VIEW HOLDER =========================

    static class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvStatus;
        ImageView btnMenu;
        View cardRoot;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSessionTitle);
            tvTime = itemView.findViewById(R.id.tvSessionTime);
            tvStatus = itemView.findViewById(R.id.tvSessionStatus);
            btnMenu = itemView.findViewById(R.id.btnSessionMenu);
            cardRoot = itemView.findViewById(R.id.cardSession);
        }

        /*
         * Bind dữ liệu PhienTuVan vào views
         */
        public void bind(model_PhienTuVan session, OnSessionActionListener listener) {
            // Tiêu đề: Dùng tin nhắn cuối hoặc "Hội thoại mới"
            String title = getTieuDeRutGon(session.getTinNhanCuoi());
            tvTitle.setText(title);

            // Thời gian (format relative: "2 giờ trước")
            tvTime.setText(formatTimeRelative(session.getNgayCapNhat()));

            // Trạng thái
            tvStatus.setText(session.getTrangThai());
            if (session.isDangMo()) {
                tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
            } else {
                tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.darker_gray));
            }

            // Click vào item → Mở phiên chat
            cardRoot.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSessionClick(session);
                }
            });

            // Click menu → Hiện popup actions
            btnMenu.setOnClickListener(v -> showPopupMenu(v, session, listener));
        }

        /*
         * Rút gọn tiêu đề (tối đa 50 ký tự)
         */
        private String getTieuDeRutGon(String tinNhan) {
            if (tinNhan == null || tinNhan.trim().isEmpty()) {
                return "Hội thoại mới";
            }

            tinNhan = tinNhan.replaceAll("\\n", " ").trim();

            if (tinNhan.length() > 50) {
                return tinNhan.substring(0, 47) + "...";
            }
            return tinNhan;
        }

        /*
         * Hiển thị popup menu với các action
         */
        private void showPopupMenu(View anchor, model_PhienTuVan session, OnSessionActionListener listener) {
            PopupMenu popup = new PopupMenu(anchor.getContext(), anchor);
            popup.inflate(R.menu.menu_chat_session);

            // Ẩn "Thêm mới" nếu phiên đã đóng
            if (!session.isDangMo()) {
                popup.getMenu().findItem(R.id.action_end_session).setVisible(false);
            }

            // Bật hiển thị icon bên cạnh text
            try {
                Field[] fields = popup.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if ("mPopup".equals(field.getName())) {
                        field.setAccessible(true);
                        Object menuPopupHelper = field.get(popup);
                        Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                        Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                        setForceIcons.invoke(menuPopupHelper, true);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_end_session) {
                    if (listener != null) listener.onEndSession(session);
                    return true;
                } else if (id == R.id.action_delete_session) {
                    if (listener != null) listener.onDeleteSession(session);
                    return true;
                }
                return false;
            });

            popup.show();
        }


        /*
         * Format thời gian thành dạng relative (VD: "2 giờ trước")
         */
        private String formatTimeRelative(String dateString) {
            if (dateString == null || dateString.isEmpty()) return "";

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = sdf.parse(dateString);

                if (date == null) return "";

                long diff = System.currentTimeMillis() - date.getTime();
                long seconds = diff / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;
                long days = hours / 24;

                if (days > 0) {
                    return days + " ngày trước";
                } else if (hours > 0) {
                    return hours + " giờ trước";
                } else if (minutes > 0) {
                    return minutes + " phút trước";
                } else {
                    return "Vừa xong";
                }
            } catch (ParseException e) {
                return dateString;
            }
        }
    }
}
