package com.example.hethongbanthuoc.core.models;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StatisticsResponse {
    private boolean success;
    private Map<String, Statistics> statistics;
    private String message;

    public static StatisticsResponse fromJson(JSONObject json) {
        StatisticsResponse resp = new StatisticsResponse();
        resp.success = json.optBoolean("success", false);
        resp.message = json.optString("message", "");
        resp.statistics = new HashMap<>();
        JSONObject statObj = json.optJSONObject("statistics");
        if (statObj != null) {
            Iterator<String> keys = statObj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject val = statObj.optJSONObject(key);
                if (val != null) {
                    resp.statistics.put(key, Statistics.fromJson(val));
                }
            }
        }
        return resp;
    }

    public boolean isSuccess() { return success; }
    public Map<String, Statistics> getStatistics() { return statistics; }
    public String getMessage() { return message; }

    public static class Statistics {
        private int total;
        private int unread;

        public static Statistics fromJson(JSONObject json) {
            Statistics s = new Statistics();
            s.total = json.optInt("total", 0);
            s.unread = json.optInt("unread", 0);
            return s;
        }

        public int getTotal() { return total; }
        public int getUnread() { return unread; }
    }
}
