package com.example.hethongbanthuoc.feature_chat.models;

import java.util.List;

public class model_ChatHistoryResponse {
    private Integer maPhienTuVan;
    private List<model_TinNhan> tinNhan;

    public model_ChatHistoryResponse() {}

    public Integer getMaPhienTuVan() { return maPhienTuVan; }
    public void setMaPhienTuVan(Integer maPhienTuVan) { this.maPhienTuVan = maPhienTuVan; }

    public List<model_TinNhan> getTinNhan() { return tinNhan; }
    public void setTinNhan(List<model_TinNhan> tinNhan) { this.tinNhan = tinNhan; }
}
