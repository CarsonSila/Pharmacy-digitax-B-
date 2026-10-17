package com.example.hethongbanthuoc.feature_chat.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hethongbanthuoc.feature_chat.models.model_TinNhan;
import com.example.hethongbanthuoc.R;

import java.util.ArrayList;
import java.util.List;

public class Adapter_Chat extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;

    private final List<model_TinNhan> messages = new ArrayList<>();

    public Adapter_Chat(List<model_TinNhan> initial) {
        if (initial != null) messages.addAll(initial);
    }

    // Trả về index của message mới (tiện để lưu thinkingIndex)
    public int addMessage(model_TinNhan message) {
        messages.add(message);
        int idx = messages.size() - 1;
        notifyItemInserted(idx);
        Log.d("CHAT_DEBUG", "addMessage idx=" + idx + " content=" + message.getNoiDung());
        return idx;
    }

    public void removeMessageAt(int index) {
        if (index >= 0 && index < messages.size()) {
            messages.remove(index);
            notifyItemRemoved(index);
            Log.d("CHAT_DEBUG", "removeMessageAt index=" + index);
        }
    }

    public void updateMessageAt(int index, model_TinNhan message) {
        if (index >= 0 && index < messages.size()) {
            messages.set(index, message);
            notifyItemChanged(index);
            Log.d("CHAT_DEBUG", "updateMessageAt index=" + index + " content=" + message.getNoiDung());
        }
    }

    public void updateMessageContentAt(int index, String newContent) {
        if (index >= 0 && index < messages.size()) {
            model_TinNhan m = messages.get(index);
            m.setNoiDung(newContent);
            notifyItemChanged(index);
            Log.d("CHAT_DEBUG", "updateMessageContentAt index=" + index + " newContent=" + newContent);
        }
    }

    public void setMessages(List<model_TinNhan> list) {
        messages.clear();
        if (list != null) messages.addAll(list);
        notifyDataSetChanged();
        Log.d("CHAT_DEBUG", "setMessages size=" + messages.size());
    }

    public int getItemCountSafe() { return messages.size(); }

    @Override
    public int getItemViewType(int position) {
        model_TinNhan message = messages.get(position);
        return message.isFromUser() ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new AIMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        model_TinNhan message = messages.get(position);
        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message);
        } else if (holder instanceof AIMessageViewHolder) {
            ((AIMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage, textTime;
        UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.tvMessageSent);
            textTime = itemView.findViewById(R.id.tvTimeSent);
        }

        void bind(model_TinNhan m) {
            textMessage.setText(m.getNoiDung() == null ? "" : m.getNoiDung());
            textTime.setText(formatTime(m.getThoiGian()));
        }

        private String formatTime(String time) {
            if (time == null) return "";
            if (time.length() >= 5) return time.length() >= 5 ? time.substring(0,5) : time;
            return time;
        }
    }

    static class AIMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage, textTime;
        AIMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.tvMessageReceived);
            textTime = itemView.findViewById(R.id.tvTimeReceived);
        }

        void bind(model_TinNhan m) {
            textMessage.setText(m.getNoiDung() == null ? "" : m.getNoiDung());
            textTime.setText(formatTime(m.getThoiGian()));
        }

        private String formatTime(String time) {
            if (time == null) return "";
            if (time.length() >= 5) return time.substring(0,5);
            return time;
        }
    }
}
