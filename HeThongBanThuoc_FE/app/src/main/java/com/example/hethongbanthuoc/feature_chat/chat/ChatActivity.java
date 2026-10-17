package com.example.hethongbanthuoc.feature_chat.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.feature_chat.models.model_ChatHistoryResponse;
import com.example.hethongbanthuoc.feature_chat.models.model_PhienTuVan;
import com.example.hethongbanthuoc.feature_chat.models.model_TinNhan;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.core.utils.TokenManager;
import com.google.android.material.card.MaterialCardView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
 * Activity chat với AI tư vấn thuốc
 */
public class ChatActivity extends AppCompatActivity
        implements ChatSessionActivity.OnSessionSelectedListener,
        ChatSessionActivity.OnSessionDeletedListener {

    // UI Components
    private RecyclerView recyclerViewChat;
    private EditText editTextMessage;
    private MaterialCardView buttonSend;
    private ProgressBar progressBar;
    private ImageView btnBack;
    private ImageView btnMenu;
    private LinearLayout emptyState;

    // Adapter & API
    private Adapter_Chat chatAdapter;
    private API_AuthService apiChatService;

    // Session adapter reference để thêm phiên mới
    private Adapter_ChatSession sessionAdapter;

    // Data
    private String token;
    private Integer currentSessionId;
    private Integer thinkingIndex = null;

    private static final String TAG = "ChatActivity";
    private static final String THINKING_MESSAGE = "💭 AI đang phản hồi...";
    private static final String DONE_SIGNAL = "[DONE]";
    private static final int SCROLL_DELAY_MS = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Ánh xạ view
        EditText edtMessage = findViewById(R.id.editTextMessage);

        // Nhận dữ liệu quà
        Intent intent = getIntent();
        if (intent != null) {
            String tenQua = intent.getStringExtra("TEN_QUA_TANG");
            String moTa = intent.getStringExtra("MO_TA");

//            // Gợi ý tin nhắn sẵn
//            String loiChao = "Xin chào, tôi muốn được tư vấn về quà \"" + tenQua + "\"";
//            if (moTa != null && !moTa.isEmpty()) {
//                loiChao += "\nMô tả: " + moTa;
//            }
//            edtMessage.setText(loiChao);
        }

        initViews();
        setupRecyclerView();
        setupAPI();
        loadCurrentSession();
        setupListeners();
    }

    private void initViews() {
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
        btnMenu = findViewById(R.id.btnMenu);
        emptyState = findViewById(R.id.emptyState);
    }

    private void setupRecyclerView() {
        chatAdapter = new Adapter_Chat(null);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(chatAdapter);
    }

    private void setupAPI() {
        token = "Bearer " + TokenManager.getToken(this);
        apiChatService = API_RetrofitClient.getRetrofitInstance(this)
                .create(API_AuthService.class);
    }

    private void setupListeners() {
        buttonSend.setOnClickListener(v -> sendMessage());
        btnBack.setOnClickListener(v -> finish());
        btnMenu.setOnClickListener(v -> openSessionManager());
    }

    // SESSION MANAGER

    private void openSessionManager() {
        ChatSessionActivity bottomSheet = ChatSessionActivity.newInstance();

        // Set callbacks
        bottomSheet.setSessionListener(this);
        bottomSheet.setDeleteListener(this);

        // Lấy reference adapter từ bottom sheet để add session mới trực tiếp
        bottomSheet.show(getSupportFragmentManager(), "chat_sessions");
    }

    @Override
    public void onSessionSelected(int maPhienTuVan) {
        if (maPhienTuVan == -1) {
            // Tạo phiên mới qua API
            createNewSession();
        } else {
            loadChatHistoryById(maPhienTuVan);
        }
    }

    @Override
    public void onSessionDeleted(int deletedSessionId) {
        if (currentSessionId != null && currentSessionId.equals(deletedSessionId)) {
            showEmptyChatState();
            showToast("Phiên đang xem đã bị xóa");
        }
    }

    /*
     * Tạo phiên mới bằng API, đồng bộ vào adapter session
     */
    private void createNewSession() {
        showLoading(true);

        apiChatService.createNewChatSession(token).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    int newSessionId = ((Number) data.get("maPhienTuVan")).intValue();

                    // Reset chat UI cho phiên mới
                    showEmptyChatState();
                    currentSessionId = newSessionId;

                    // Đồng bộ thêm vào sessionAdapter nếu đã có reference
                    if (sessionAdapter != null) {
                        model_PhienTuVan newSession = new model_PhienTuVan();
                        newSession.setMaPhienTuVan(newSessionId);
                        newSession.setDangMo(true);
                        newSession.setTrangThai("Mới");
                        sessionAdapter.addSessionAtTop(newSession);
                    }

                    showToast("Đã tạo phiên mới");
                } else {
                    showToast("Không thể tạo phiên mới");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showLoading(false);
                showToast("Lỗi: " + t.getMessage());
            }
        });
    }

    //  Hiên thị danh sách lịch sử phiên tư vấn

    private void loadCurrentSession() {
        showLoading(true);

        apiChatService.getCurrentSession(token).enqueue(new Callback<model_ChatHistoryResponse>() {
            @Override
            public void onResponse(Call<model_ChatHistoryResponse> call, Response<model_ChatHistoryResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    handleSessionLoaded(response.body());
                } else {
                    showToast("Không thể tải phiên chat hiện tại");
                }
            }

            @Override
            public void onFailure(Call<model_ChatHistoryResponse> call, Throwable t) {
                showLoading(false);
                showToast("Lỗi: " + t.getMessage());
            }
        });
    }

    private void handleSessionLoaded(model_ChatHistoryResponse response) {
        currentSessionId = response.getMaPhienTuVan();
        chatAdapter.setMessages(response.getTinNhan());
        updateEmptyState();
        recyclerViewChat.postDelayed(this::scrollToBottom, SCROLL_DELAY_MS);
    }

    private void loadChatHistoryById(int sessionId) {
        showLoading(true);

        apiChatService.getChatHistory(token, sessionId, 50).enqueue(new Callback<model_ChatHistoryResponse>() {
            @Override
            public void onResponse(Call<model_ChatHistoryResponse> call, Response<model_ChatHistoryResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    currentSessionId = response.body().getMaPhienTuVan();
                    chatAdapter.setMessages(response.body().getTinNhan());
                    updateEmptyState();
                    scrollToBottom();
                } else {
                    showToast("Không thể tải lịch sử phiên");
                }
            }

            @Override
            public void onFailure(Call<model_ChatHistoryResponse> call, Throwable t) {
                showLoading(false);
                showToast("Lỗi: " + t.getMessage());
            }
        });
    }

    // Xử lý tin nhắn và phản hồi tin nhắn

    private void sendMessage() {
        String message = editTextMessage.getText().toString().trim();
        if (message.isEmpty()) {
            showToast("Vui lòng nhập tin nhắn");
            return;
        }

        setInputEnabled(false);
        showLoading(true);
        addUserMessage(message);
        addThinkingMessage();
        editTextMessage.setText("");
        callStreamAPI(message);
    }

    private void addUserMessage(String message) {
        model_TinNhan userMsg = new model_TinNhan(currentSessionId, message, true, getCurrentTime());
        chatAdapter.addMessage(userMsg);
        updateEmptyState();
        scrollToBottom();
    }

    private void addThinkingMessage() {
        model_TinNhan thinking = new model_TinNhan(currentSessionId, THINKING_MESSAGE, false, getCurrentTime());
        thinkingIndex = chatAdapter.addMessage(thinking);
        scrollToBottom();
    }

    private void callStreamAPI(String message) {
        Map<String, String> request = new HashMap<>();
        request.put("message", message);

        apiChatService.streamChat(token, request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                setInputEnabled(true);
                showLoading(false);
                if (!response.isSuccessful() || response.body() == null) {
                    updateThinkingMessage("Lỗi phản hồi AI.");
                    return;
                }
                processStreamResponse(response.body());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleStreamError(t);
            }
        });
    }

    private void processStreamResponse(ResponseBody responseBody) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()))) {
                StringBuilder aiText = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    if (line.startsWith("data:")) {
                        String chunk = line.substring(5).trim();
                        if (chunk.equals(DONE_SIGNAL)) break;
                        aiText.append(chunk);
                        updateThinkingMessageOnUI(aiText.toString());
                    }
                }
                finalizeStream(aiText.toString());
            } catch (IOException e) {
                handleStreamIOError(e);
            }
        }).start();
    }

    private void finalizeStream(String finalText) {
        runOnUiThread(() -> {
            if (thinkingIndex != null) {
                chatAdapter.updateMessageContentAt(thinkingIndex, finalText.isEmpty() ? "⚠️ AI không trả về phản hồi" : finalText);
                thinkingIndex = null;
            }
        });
    }

    private void updateThinkingMessageOnUI(String text) {
        runOnUiThread(() -> {
            if (thinkingIndex != null) {
                chatAdapter.updateMessageContentAt(thinkingIndex, text);
                scrollToBottom();
            }
        });
    }

    private void handleStreamIOError(IOException e) {
        runOnUiThread(() -> {
            updateThinkingMessage("Lỗi đọc stream: " + e.getMessage());
            thinkingIndex = null;
        });
    }

    private void handleStreamError(Throwable t) {
        runOnUiThread(() -> {
            setInputEnabled(true);
            showLoading(false);
            updateThinkingMessage("Kết nối thất bại.");
            thinkingIndex = null;
            showToast("Lỗi: " + t.getMessage());
        });
    }

    private void updateThinkingMessage(String text) {
        if (thinkingIndex != null) {
            chatAdapter.updateMessageContentAt(thinkingIndex, text);
        }
    }

    // Cập nhật giao diện

    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }

    private void scrollToBottom() {
        int itemCount = chatAdapter.getItemCount();
        if (itemCount > 0) recyclerViewChat.smoothScrollToPosition(itemCount - 1);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void setInputEnabled(boolean enabled) {
        buttonSend.setEnabled(enabled);
        editTextMessage.setEnabled(enabled);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateEmptyState() {
        boolean isEmpty = chatAdapter.getItemCount() == 0;
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerViewChat.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showEmptyChatState() {
        currentSessionId = null;
        thinkingIndex = null;
        chatAdapter.setMessages(new ArrayList<>());
        updateEmptyState();
        showToast("Bắt đầu phiên mới");
    }
}
