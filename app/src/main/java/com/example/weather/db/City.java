package com.example.weather.db;

import org.litepal.crud.LitePalSupport;

public class City extends LitePalSupport {

    // 存放市级数据
    private String cityName;
    private String cityCode;
    private String provinceCode;

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getProvinceCode(){
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }
}