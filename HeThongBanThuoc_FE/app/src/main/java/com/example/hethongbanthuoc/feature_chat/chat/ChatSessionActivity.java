package com.example.hethongbanthuoc.feature_chat.chat;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.feature_chat.models.model_ChatSessionResponse;
import com.example.hethongbanthuoc.feature_chat.models.model_PhienTuVan;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.core.utils.TokenManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * Bottom Sheet hiển thị danh sách phiên chat
 * - Xem lịch sử
 * - Tạo phiên mới
 * - Xóa phiên
 */
public class ChatSessionActivity extends BottomSheetDialogFragment
        implements Adapter_ChatSession.OnSessionActionListener {

    // Views
    private RecyclerView recyclerViewSessions;
    private LinearLayout emptyState;
    private ProgressBar progressBar;
    private ImageView btnClose;

    // Data
    private Adapter_ChatSession adapter;
    private API_AuthService apiService;
    private String token;

    // Callbacks để notify ChatActivity
    private OnSessionSelectedListener sessionListener;
    private OnSessionDeletedListener deleteListener;

    /*
     * Interface callback khi chọn session
     */
    public interface OnSessionSelectedListener {
        void onSessionSelected(int maPhienTuVan);
    }

    /*
     * Interface callback khi xóa session
     */
    public interface OnSessionDeletedListener {
        void onSessionDeleted(int maPhienTuVan);
    }

    public static ChatSessionActivity newInstance() {
        return new ChatSessionActivity();
    }

    /*
     * Set listener cho callback chọn session
     */
    public void setSessionListener(OnSessionSelectedListener listener) {
        this.sessionListener = listener;
    }

    /*
     * Set listener cho callback xóa session
     */
    public void setDeleteListener(OnSessionDeletedListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        // Set behavior để hiển thị 60% màn hình
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            View bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setPeekHeight((int) (getResources().getDisplayMetrics().heightPixels * 0.6));
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_chat_session, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupAPI();
        setupRecyclerView();
        setupListeners();
        loadSessions();
    }

    /*
     * Khởi tạo views
     */
    private void initViews(View view) {
        recyclerViewSessions = view.findViewById(R.id.recyclerViewSessions);
        emptyState = view.findViewById(R.id.emptyStateSessions);
        progressBar = view.findViewById(R.id.progressBarSessions);
        btnClose = view.findViewById(R.id.btnClose);
    }

    /*
     * Setup API service
     */
    private void setupAPI() {
        token = "Bearer " + TokenManager.getToken(requireContext());
        apiService = API_RetrofitClient.getRetrofitInstance(requireContext())
                .create(API_AuthService.class);
    }

    /*
     * Setup RecyclerView
     */
    private void setupRecyclerView() {
        adapter = new Adapter_ChatSession(this);
        recyclerViewSessions.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewSessions.setAdapter(adapter);
    }

    /*
     * Setup listeners
     */
    private void setupListeners() {
        btnClose.setOnClickListener(v -> dismiss());
    }

    // hiển thị dữ liẹu vào adapter

    /*
     * Tải danh sách phiên từ API
     */
    private void loadSessions() {
        showLoading(true);

        apiService.getAllSessions(token).enqueue(new Callback<model_ChatSessionResponse>() {
            @Override
            public void onResponse(Call<model_ChatSessionResponse> call, Response<model_ChatSessionResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    handleSessionsLoaded(response.body());
                } else {
                    showError("Không thể tải danh sách phiên");
                }
            }

            @Override
            public void onFailure(Call<model_ChatSessionResponse> call, Throwable t) {
                showLoading(false);
                showError("Lỗi: " + t.getMessage());
            }
        });
    }

    /*
     * Xử lý khi load sessions thành công
     */
    private void handleSessionsLoaded(model_ChatSessionResponse response) {
        adapter.setSessions(response.getSessions());
        updateEmptyState(response.getSessions() == null || response.getSessions().isEmpty());
    }

    // Thủ tục callback từ adapter

    /*
     * Callback khi click vào session item
     */
    @Override
    public void onSessionClick(model_PhienTuVan session) {
        if (sessionListener != null) {
            sessionListener.onSessionSelected(session.getMaPhienTuVan());
        } else if (getActivity() instanceof OnSessionSelectedListener) {
            ((OnSessionSelectedListener) getActivity()).onSessionSelected(session.getMaPhienTuVan());
        }
        dismiss();
    }

    /*
     * Callback khi click "Kết thúc phiên" (popup sẽ dùng làm "Thêm mới")
     */
    @Override
    public void onEndSession(model_PhienTuVan session) {
        // Dialog tạo phiên mới
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Tạo phiên tư vấn mới")
                .setMessage("Bạn có muốn bắt đầu một phiên tư vấn mới không?")
                .setPositiveButton("Thêm mới", (dialog, which) -> performNewSession())
                .setNegativeButton("Hủy", null)
                .show();
    }

    /*
     * Callback khi click "Xóa phiên"
     */
    @Override
    public void onDeleteSession(model_PhienTuVan session) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa phiên")
                .setMessage("Bạn có chắc muốn xóa phiên này? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> performDeleteSession(session))
                .setNegativeButton("Hủy", null)
                .show();
    }


    private void performNewSession() {
        showLoading(true);

        apiService.createNewChatSession(token)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            Map<String, Object> body = response.body();
                            int maPhienTuVan = ((Double) body.get("maPhienTuVan")).intValue(); // Retrofit trả Double
                            String message = (String) body.get("message");

                            showToast(message);

                            // Cập nhật adapter với phiên mới nếu muốn hiển thị ngay
                            model_PhienTuVan newSession = new model_PhienTuVan();
                            newSession.setMaPhienTuVan(maPhienTuVan);
                            newSession.setTinNhanCuoi("Phiên tư vấn mới");
                            newSession.setDangMo(true); // đánh dấu đang mở
                            adapter.addSessionAtTop(newSession);
                            updateEmptyState(adapter.getItemCount() == 0);

                            // Callback về ChatActivity nếu cần
                            if (sessionListener != null) {
                                sessionListener.onSessionSelected(maPhienTuVan);
                            } else if (getActivity() instanceof OnSessionSelectedListener) {
                                ((OnSessionSelectedListener) getActivity()).onSessionSelected(maPhienTuVan);
                            }

                            dismiss();
                        } else {
                            showError("Không thể tạo phiên mới");
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        showLoading(false);
                        showError("Lỗi: " + t.getMessage());
                    }
                });
    }

    /*
     * Xóa phiên qua API
     */
    private void performDeleteSession(model_PhienTuVan session) {
        showLoading(true);

        int deletedSessionId = session.getMaPhienTuVan();

        apiService.deleteChatSession(token, deletedSessionId)
                .enqueue(new Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                        showLoading(false);

                        if (response.isSuccessful()) {
                            showToast("Đã xóa phiên");

                            // Xóa khỏi adapter
                            adapter.removeSession(session);
                            updateEmptyState(adapter.getItemCount() == 0);

                            // Callback về ChatActivity để clear nếu đang xem phiên này
                            if (deleteListener != null) {
                                deleteListener.onSessionDeleted(deletedSessionId);
                            } else if (getActivity() instanceof OnSessionDeletedListener) {
                                ((OnSessionDeletedListener) getActivity()).onSessionDeleted(deletedSessionId);
                            }
                        } else {
                            showError("Không thể xóa phiên");
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, String>> call, Throwable t) {
                        showLoading(false);
                        showError("Lỗi: " + t.getMessage());
                    }
                });
    }

    // Thiết lập cập nhật giao diện

    /*
     * Hiển thị/ẩn loading
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewSessions.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /*
     * Hiển thị/ẩn empty state
     */
    private void updateEmptyState(boolean isEmpty) {
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerViewSessions.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    /*
     * Hiển thị toast
     */
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    /*
     * Hiển thị lỗi
     */
    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}