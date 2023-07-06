package com.example.weather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.example.weather.gson.Weather;
import com.example.weather.util.HttpUtil;
import com.example.weather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        updateWeather();

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);//书中P469
        // 8小时的毫秒数
        int anHour =  1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void updateWeather(){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);// 尝试获取之前保存的缓存
        String weatherString = prefs.getString("weather", null);

        // 获取当前天气信息 并进行 更新
        if (weatherString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            assert weather != null;

            String weatherId = weather.adcodeName;
            String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=a4c1c7c9727942f9a73dc3bced0c130b";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                // 响应更新
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    if (weather != null){
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                    }
                }

                // 更新失败报错：
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
