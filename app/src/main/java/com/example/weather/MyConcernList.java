package com.example.weather;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import static com.example.weather.MyDBhelper.DB_NAME;

public class MyConcernList extends AppCompatActivity {
    ArrayAdapter simpleAdapter;
    ListView MyConcernList;
    private Button goBack;
    private List<String> city_nameList = new ArrayList<>();
    private List<String> city_codeList = new ArrayList<>();

    // 将获取的数据填充到数据库中
    // 初始化列表
    private void InitConcern() {
        MyDBhelper dbHelper = new MyDBhelper(this, DB_NAME, null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor  = db.rawQuery("select * from Concern",null);
        while(cursor.moveToNext()){
            @SuppressLint("Range") String city_code = cursor.getString(cursor.getColumnIndex("city_code"));
            @SuppressLint("Range") String city_name = cursor.getString(cursor.getColumnIndex("city_name"));
            city_codeList.add(city_code);
            city_nameList.add(city_name);
        }
    }

    // 刷新列表 (先清除 再添加)
    public void RefreshList(){
        city_nameList.removeAll(city_nameList);
        city_codeList.removeAll(city_codeList);
        simpleAdapter.notifyDataSetChanged();

        MyDBhelper dbHelper = new MyDBhelper(this,DB_NAME,null,1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor  = db.rawQuery("select * from Concern",null);
        while(cursor.moveToNext()){
            @SuppressLint("Range") String city_code = cursor.getString(cursor.getColumnIndex("city_code"));
            @SuppressLint("Range") String city_name = cursor.getString(cursor.getColumnIndex("city_name"));
            city_codeList.add(city_code);
            city_nameList.add(city_name);
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        RefreshList();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myconcern_list);
        MyConcernList = findViewById(R.id.MyConcernList);
        goBack = findViewById(R.id.goback_btn);

        InitConcern();

        // 设置适配器
        simpleAdapter = new ArrayAdapter(MyConcernList.this,android.R.layout.simple_list_item_1,city_nameList);
        MyConcernList.setAdapter(simpleAdapter);

        // ArrayList点击事件
        // 即点击县级能够跳转至天气界面
        MyConcernList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view , int position , long id){
                String tran = city_codeList.get(position);
                Intent intent = new Intent(MyConcernList.this, WeatherActivity.class);
                intent.putExtra("adcode", tran);
                startActivity(intent);
            }
        });

        // 返回按钮
        goBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 启动主活动 (即返回主页面)
                Intent intent = new Intent(MyConcernList.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
