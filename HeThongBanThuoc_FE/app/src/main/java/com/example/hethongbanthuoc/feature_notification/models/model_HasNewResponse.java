package com.example.hethongbanthuoc.feature_notification.models;

import org.json.JSONObject;

public class model_HasNewResponse {
    private boolean success;
    private boolean hasNew;
    private String message;

    public static model_HasNewResponse fromJson(JSONObject json) {
        model_HasNewResponse resp = new model_HasNewResponse();
        resp.success = json.optBoolean("success", false);
        resp.hasNew = json.optBoolean("hasNew", false);
        resp.message = json.optString("message", "");
        return resp;
    }

    public boolean isSuccess() { return success; }
    public boolean isHasNew() { return hasNew; }
    public String getMessage() { return message; }
}
