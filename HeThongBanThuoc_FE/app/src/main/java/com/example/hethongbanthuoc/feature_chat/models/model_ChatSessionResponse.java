package com.example.hethongbanthuoc.feature_chat.models;

import java.util.List;

public class model_ChatSessionResponse {
    private List<model_PhienTuVan> sessions;
    private Integer total;

    public model_ChatSessionResponse() {}

    public List<model_PhienTuVan> getSessions() { return sessions; }
    public void setSessions(List<model_PhienTuVan> sessions) { this.sessions = sessions; }

    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
}
