package com.example.weather;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.weather.db.City;
import com.example.weather.db.County;
import com.example.weather.db.Province;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import com.example.weather.util.HttpUtil;
import com.example.weather.util.Utility;

public class ChooseAreaFragment extends Fragment {
    // 设置地区的级别
    private static final int LEVEL_PROVINCE = 0;
    private static final int LEVEL_CITY = 1;
    private static final int LEVEL_COUNTY = 2;

    // 使用datalist列表
    // 存储保存的地区的 省、市、县和天气 数据
    private List<String> dataList = new ArrayList<>();

    private ArrayAdapter<String> adapter;
    private TextView titleText;
    private Button backButton;
    private ListView listView;

    // 省级列表
    private List<Province> provinceList;
    // 市级列表
    private List<City> cityList;
    // 县级列表
    private List<County> countyList;
    // 当前等级
    // 区分 省 市 县
    private int currentLevel;

    // 所选中的省份
    private Province selectedProvince;
    // 所选中的市区
    private City selectedCity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 获得布局填充器
        View view = inflater.inflate(R.layout.choose_area,container,false);
        // 获得当前布局的各个组件
        titleText = view.findViewById(R.id.title_text);
        backButton = view.findViewById(R.id.back_button);
        listView = view.findViewById(R.id.list_view);

        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 用于侧滑菜单：
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 若点击区域的等级为省级
                if (currentLevel == LEVEL_PROVINCE) {
                    // 获取当前省级地区的位置
                    selectedProvince = provinceList.get(position);
                    // 查询数据库或网络资源
                    // 获取当前省份中的市区信息
                    queryCities();
                }
                // 若点击区域的等级为市级
                else if (currentLevel == LEVEL_CITY) {
                    // 获取当前省级地区的位置
                    selectedCity = cityList.get(position);
                    // 获取当前市区中的县级信息
                    queryCounties();
                }
                // 若点击区域的等级为县级 (需要最后显示的资源)
                else if (currentLevel == LEVEL_COUNTY) {
                    // 获得当前县级的名称和代码
                    String countyCode = countyList.get(position).getCountyCode();
                    String countyName = countyList.get(position).getCountyName();

                    // 判断当前执行的活动是否为 主活动类型
                    // 即是否正在选取 市、区、县
                    if (getActivity() instanceof MainActivity) {
                        // 准备启动该活动
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        // 将当前最后一级的 县级名称和代码 进行保存 并传入活动
                        intent.putExtra( "adcode",countyCode);
                        intent.putExtra("city",countyName);
                        startActivity(intent);
                        getActivity().finish();
                    }
                    // 判断当前执行的活动是否为 查看天气的活动类型
                    else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        // 关闭左侧滑动菜单
                        activity.drawerLayout.closeDrawers();
                        // 启用下拉刷新进度条
                        activity.swipeRefresh.setRefreshing(true);
                        // 显示当前 县级 的天气情况
                        activity.requestWeather(countyCode);
                    }
                }
            }
        });

        // 返回主页按钮：
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 判断当前页面的等级
                if (currentLevel == LEVEL_COUNTY) {
                    // 若当前页面停留在 县级
                    // 则返回至上一级的 市级 页面
                    queryCities();
                }
                else if (currentLevel == LEVEL_CITY) {
                    // 若当前页面停留在 市级
                    // 则返回至上一级的 省级 页面
                    queryProvinces();
                }
            }
        });

        // 默认停留在 省级 页面
        queryProvinces();
    }

    // 查询所有省份
    // 优先从当前数据库查询
    // 若不存在则再去服务器上查询
    private void queryProvinces(){
        titleText.setText("中 国 地 区 省 份");
        // 隐藏返回按钮
        backButton.setVisibility(View.GONE);

        // 当前数据库(LitePal)中查询：
        provinceList = LitePal.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            // 将datalist更新为中国的所有省份
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            // 刷新ListView (显示更新后的内容)
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            // 将页面等级调整为上一级
            currentLevel = LEVEL_PROVINCE;
        }
        // 若当前数据库没有数据
        // 则跳调用API进行访问
        else{
            String address = "https://restapi.amap.com/v3/config/district?keywords=中国&subdistrict=1&key=c132591390eb44402d93af2e08065fe5";
            // 查询 省份 信息
            queryFromServer(address,"province");
        }
    }

    // 查询所有市区
    // 优先从当前数据库查询
    // 若不存在则再去服务器上查询
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        // 显示返回按钮
        backButton.setVisibility(View.VISIBLE);

        // 需要根据特定 省份 来查询 市区
        cityList = LitePal.where("provinceCode = ?",
                String.valueOf(selectedProvince.getProvinceCode())).find(City.class);
        // 当前数据库(LitePal)中查询： (以下操作同 queryProvinces())
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }
        else {
            // 需要获取先前保存的省份信息
            String provinceName = selectedProvince.getProvinceName();
            String address = "https://restapi.amap.com/v3/config/district?keywords="+provinceName+"&subdistrict=1&key=c132591390eb44402d93af2e08065fe5";
            // 查询 市区 信息
            queryFromServer(address,"city");
        }
    }

    // 查询所有乡县
    // 优先从当前数据库查询
    // 若不存在则再去服务器上查询
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);

        countyList = LitePal.where("cityCode=?",
                String.valueOf(selectedCity.getCityCode())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }
        else {
            String cityName = selectedCity.getCityName();
            String address = "https://restapi.amap.com/v3/config/district?keywords="+cityName+"&subdistrict=1&key=c132591390eb44402d93af2e08065fe5";
            queryFromServer(address,"county");
        }
    }


    // 从传入的API服务器中获取 省 市 县 的数据
    private void queryFromServer(String address, final String type){
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "加载API失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                // 根据对应的 Key 值进行查询和传送不同的数据
                if("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                }
                else if("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getProvinceCode());
                }
                else if("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getCityCode());
                }
                // 若查询成功：
                // 则调用活动中的方法 将其保存到数据库中 方便下次使用
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if("province".equals(type)) {
                                queryProvinces();
                            }
                            else if("county".equals(type)) {
                                queryCities();
                            }
                            else if("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }
}