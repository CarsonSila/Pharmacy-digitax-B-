package com.example.hethongbanthuoc.core.network;

import android.content.Context;

import com.example.hethongbanthuoc.core.utils.TokenManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class API_RetrofitClient {
    // Địa chỉ IP thật: 192.168.92.101
    // Địa chỉ Local: 10.0.2.2
    private static final String BASE_URL = "http://10.0.2.2:8080/api/";

    public static Retrofit getRetrofitInstance(Context context) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder();

                    String url = original.url().toString();
                    String token = TokenManager.getToken(context);

                    // Chỉ gắn token khi không phải login/register
                    if (!url.contains("/auth/login") && !url.contains("/auth/register")) {
                        if (token != null && !token.isEmpty()) {
                            builder.header("Authorization", "Bearer " + token);
                            android.util.Log.d("API_TOKEN", "Attach token: Bearer " + token);
                        } else {
                            android.util.Log.e("API_TOKEN", "Token is NULL hoặc EMPTY !!!");
                        }
                    }

                    Request request = builder.build();
                    return chain.proceed(request);
                })
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}


