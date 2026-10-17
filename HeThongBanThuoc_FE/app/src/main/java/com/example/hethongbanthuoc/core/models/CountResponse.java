package com.example.hethongbanthuoc.core.models;

import org.json.JSONObject;

public class CountResponse {
    private boolean success;
    private int unreadCount;
    private String message;

    public static CountResponse fromJson(JSONObject json) {
        CountResponse resp = new CountResponse();
        resp.success = json.optBoolean("success", false);
        resp.unreadCount = json.optInt("unreadCount", 0);
        resp.message = json.optString("message", "");
        return resp;
    }

    public boolean isSuccess() { return success; }
    public int getUnreadCount() { return unreadCount; }
    public String getMessage() { return message; }
}
