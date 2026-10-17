package com.example.hethongbanthuoc.feature_notification.models;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class model_ThongBaoListResponse {

    private boolean success;
    private List<model_ThongBao> data;
    private Pagination pagination;
    private String message;

    public model_ThongBaoListResponse() {
        data = new ArrayList<>();
    }

    public static model_ThongBaoListResponse fromJson(JSONObject json) {
        model_ThongBaoListResponse resp = new model_ThongBaoListResponse();
        resp.success = json.optBoolean("success", false);
        resp.message = json.optString("message", "");

        JSONArray dataArray = json.optJSONArray("data");
        if (dataArray != null) {
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject obj = dataArray.optJSONObject(i);
                if (obj != null) {
                    resp.data.add(model_ThongBao.fromJson(obj));
                }
            }
        }

        JSONObject pagObj = json.optJSONObject("pagination");
        if (pagObj != null) {
            resp.pagination = Pagination.fromJson(pagObj);
        }

        return resp;
    }

    public boolean isSuccess() { return success; }
    public List<model_ThongBao> getData() { return data; }
    public Pagination getPagination() { return pagination; }
    public String getMessage() { return message; }

    public static class Pagination {
        private int page;
        private int size;
        private int total;
        private int unread;

        public static Pagination fromJson(JSONObject json) {
            Pagination p = new Pagination();
            p.page = json.optInt("page", 1);
            p.size = json.optInt("size", 0);
            p.total = json.optInt("total", 0);
            p.unread = json.optInt("unread", 0);
            return p;
        }

        public int getPage() { return page; }
        public int getSize() { return size; }
        public int getTotal() { return total; }
        public int getUnread() { return unread; }
    }
}
