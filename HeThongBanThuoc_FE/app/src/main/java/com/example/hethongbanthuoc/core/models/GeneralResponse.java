package com.example.hethongbanthuoc.core.models;

import org.json.JSONObject;

public class GeneralResponse {
    private boolean success;
    private String message;
    private Integer updatedCount;
    private Integer deletedCount;

    public static GeneralResponse fromJson(JSONObject json) {
        GeneralResponse resp = new GeneralResponse();
        resp.success = json.optBoolean("success", false);
        resp.message = json.optString("message", "");
        resp.updatedCount = json.has("updatedCount") ? json.optInt("updatedCount") : null;
        resp.deletedCount = json.has("deletedCount") ? json.optInt("deletedCount") : null;
        return resp;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Integer getUpdatedCount() { return updatedCount; }
    public Integer getDeletedCount() { return deletedCount; }
}
