package com.example.weather;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBhelper extends SQLiteOpenHelper {
    public static final String DB_NAME="mydb.db";
    public static final int VERSION=1;
    public static final String TABLE_NAME="Concern";

    public static final String CREATE_CONCERN = "create table Concern("
            + "city_code String primary key not null,"
            + "city_name String not null)";//not null

    public MyDBhelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context,name,factory,version);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_CONCERN);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}
