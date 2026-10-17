package com.example.hethongbanthuoc.feature_chat.models;

public class model_ChatResponse {
    private Integer maPhienTuVan;
    private model_ChatMessage userMessage;
    private model_ChatMessage aiMessage;

    public Integer getMaPhienTuVan() { return maPhienTuVan; }
    public void setMaPhienTuVan(Integer maPhienTuVan) { this.maPhienTuVan = maPhienTuVan; }

    public model_ChatMessage getUserMessage() { return userMessage; }
    public void setUserMessage(model_ChatMessage userMessage) { this.userMessage = userMessage; }

    public model_ChatMessage getAiMessage() { return aiMessage; }
    public void setAiMessage(model_ChatMessage aiMessage) { this.aiMessage = aiMessage; }
}
