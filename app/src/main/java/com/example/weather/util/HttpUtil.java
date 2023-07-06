package com.example.weather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {

    // 向API发送http请求
    public static void sendOkHttpRequest(final String address, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).get().build();
        client.newCall(request).enqueue(callback);
    }
}