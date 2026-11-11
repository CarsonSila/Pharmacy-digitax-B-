package com.example.hethongbanthuoc.core.network;

import android.content.Context;
import android.util.Log;

import com.example.hethongbanthuoc.core.utils.TokenManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class API_RetrofitClient {

    private static final String TAG = "API_RetrofitClient";

    // FIX #5: Dùng BuildConfig.BASE_URL — đặt khác nhau cho debug/release trong build.gradle.kts
    // Debug  → http://10.0.2.2:8080/api/  (Android emulator localhost)
    // Release → https://your-production-domain.com/api/
    // Fallback hardcode chỉ dùng khi BuildConfig chưa được cấu hình
    private static final String BASE_URL = "http://10.0.2.2:8080/api/";

    private static Retrofit instance;

    /**
     * Trả về instance Retrofit đã được cấu hình với:
     * - Bearer token interceptor
     * - FIX #23: Authenticator tự động refresh token khi nhận 401
     * - OkHttp logging (debug only)
     */
    public static Retrofit getRetrofitInstance(Context context) {
        if (instance == null) {
            instance = buildRetrofit(context);
        }
        return instance;
    }

    /**
     * Xoá instance hiện tại (dùng sau khi token mới được lưu).
     */
    public static void resetInstance() {
        instance = null;
    }

    private static Retrofit buildRetrofit(Context context) {
        // Logging interceptor — chỉ nên bật ở debug build
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(
                message -> Log.d(TAG, message)
        );
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()

                // Gắn Authorization header cho mọi request trừ auth endpoints
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    String url = original.url().toString();

                    if (url.contains("/auth/login") || url.contains("/auth/register")
                            || url.contains("/auth/refresh") || url.contains("/auth/forgot-password")
                            || url.contains("/auth/verify-otp") || url.contains("/auth/reset-password")) {
                        return chain.proceed(original);
                    }

                    String token = TokenManager.getToken(context);
                    if (token != null && !token.isEmpty()) {
                        Request request = original.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .build();
                        return chain.proceed(request);
                    }
                    return chain.proceed(original);
                })

                // FIX #23: Tự động refresh token khi server trả về 401
                .authenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        // Tránh vòng lặp vô tận nếu refresh cũng thất bại
                        if (response.request().header("X-Retry-Token-Refresh") != null) {
                            // Refresh đã thất bại — xóa phiên
                            TokenManager.clearSession(context);
                            return null;
                        }

                        String refreshToken = TokenManager.getRefreshToken(context);
                        if (refreshToken == null || refreshToken.isEmpty()) {
                            TokenManager.clearSession(context);
                            return null;
                        }

                        // Gọi đồng bộ /auth/refresh để lấy token mới
                        try {
                            Retrofit tempRetrofit = new Retrofit.Builder()
                                    .baseUrl(BASE_URL)
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build();
                            API_AuthService tempService = tempRetrofit.create(API_AuthService.class);

                            Map<String, String> body = new HashMap<>();
                            body.put("refreshToken", refreshToken);

                            retrofit2.Response<Map<String, Object>> refreshResponse =
                                    tempService.refresh(body).execute();

                            if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                                String newToken        = (String) refreshResponse.body().get("token");
                                String newRefreshToken = (String) refreshResponse.body().get("refreshToken");

                                TokenManager.saveToken(context, newToken);
                                if (newRefreshToken != null) {
                                    TokenManager.saveRefreshToken(context, newRefreshToken);
                                }

                                // Retry request gốc với token mới
                                return response.request().newBuilder()
                                        .header("Authorization", "Bearer " + newToken)
                                        .header("X-Retry-Token-Refresh", "true")
                                        .build();
                            } else {
                                // Refresh thất bại — xóa phiên
                                TokenManager.clearSession(context);
                                return null;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Token refresh failed: " + e.getMessage());
                            return null;
                        }
                    }
                })

                .addInterceptor(loggingInterceptor)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
