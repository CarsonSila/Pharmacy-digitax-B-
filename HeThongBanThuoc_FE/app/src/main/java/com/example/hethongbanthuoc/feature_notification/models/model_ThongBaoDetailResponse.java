package com.example.hethongbanthuoc.feature_notification.models;

import org.json.JSONObject;

public class model_ThongBaoDetailResponse {
    private boolean success;
    private model_ThongBao data;
    private String message;

    public static model_ThongBaoDetailResponse fromJson(JSONObject json) {
        model_ThongBaoDetailResponse resp = new model_ThongBaoDetailResponse();
        resp.success = json.optBoolean("success", false);
        resp.message = json.optString("message", "");
        JSONObject dataObj = json.optJSONObject("data");
        if (dataObj != null) {
            resp.data = model_ThongBao.fromJson(dataObj);
        }
        return resp;
    }

    public boolean isSuccess() { return success; }
    public model_ThongBao getData() { return data; }
    public String getMessage() { return message; }
}
