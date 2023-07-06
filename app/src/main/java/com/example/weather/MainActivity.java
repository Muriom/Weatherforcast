package com.example.weather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    // 查找按钮
    private Button searchButton;
    // 通过县级代码 查询天气
    private EditText chengShi;
    // 我的关注按钮
    private Button myConcern;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 添加组件
        setContentView(R.layout.activity_main);
        chengShi = findViewById(R.id.chengshi_text);
        searchButton = findViewById(R.id.search_button);
        myConcern = findViewById(R.id.concern_text);

        chengShi.setHint("citycode i.e.340200");
        // 搜索按钮监听
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchCountyCode = String.valueOf(chengShi.getText());
                // 高德天气中 城市代码为 6位
                if(searchCountyCode.length() != 6) {
                    Toast.makeText(MainActivity.this,"城市ID长度为6位!",Toast.LENGTH_LONG).show();
                }
                else {
                    Intent intent = new Intent(MainActivity.this, WeatherActivity.class);
                    intent.putExtra("adcode",searchCountyCode);
                    startActivity(intent);
                }
            }
        });

        // 我的关注按钮监听
        myConcern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,MyConcernList.class);
                startActivity(intent);
            }
        });

        // SharedPreferences 存储数据
        SharedPreferences pres = getSharedPreferences(String.valueOf(this), Context.MODE_PRIVATE);
        if (pres.getString("weather", null) != null){
            Intent intent = new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
        }
    }
}