package com.example.myweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myweather.db.City;
import com.example.myweather.db.County;
import com.example.myweather.db.Province;
import com.example.myweather.gson.Weather;
import com.example.myweather.util.HttpUtil;
import com.example.myweather.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ChooseArea extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final int LEVEL_PROVINCE = 0;
    private static final int LEVEL_CITY = 1;
    private static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    //省市县列表
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    //选中的省县市
    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;

    //当前选中的级别
    private int currentLevel;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = view.findViewById(R.id.title_text);
        backButton = view.findViewById(R.id.back_button);
        listView = view.findViewById(R.id.list_view);

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //如果当前显示的是省
                if (currentLevel == LEVEL_PROVINCE){

                    selectedProvince = provinceList.get(position);
                    //查找这个省下面所有的市
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){

                    selectedCity = cityList.get(position);
                    //查找这个市下的所有县
                    queryCounties();

                }else if (currentLevel == LEVEL_COUNTY){
                    selectedCounty = countyList.get(position);
                    Intent intent = new Intent(getContext(), WeatherActivity2.class);
                    intent.putExtra("weather_id",selectedCounty.getWeatherId());
                    intent.putExtra("county_id",selectedCounty.getCountyName());
                    startActivity(intent);
                    getActivity().finish();

                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //当前显示的是县
                if (currentLevel==LEVEL_COUNTY){
                    //返回这个省所属的所有市
                    queryCities();

                }
                //如果是市的话，返回上一级到省，显示这个省的所有市的信息
                else if (currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces(){
        titleText.setText("中国");
        //设置为不可见
        backButton.setVisibility(View.GONE);
        //查找所有的省信息
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size()>0){
            //刷新数据列表
            dataList.clear();
            for (Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            //通知数据更新，进行页面刷新
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            //当前显示的是省一级的数据
            currentLevel = LEVEL_PROVINCE;
        }
        //如果没有数据就要到服务器上查询
        else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }

    }

    //查询省内内所有的市
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?",
                String.valueOf(selectedProvince.getProvinceCode())).find(City.class);
        if (cityList.size()>0){
            //刷新数据列表
            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityName());
            }
            //通知数据更新，进行页面刷新
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            //接下来显示市一级的数据
            currentLevel = LEVEL_CITY;
        }
        //如果没有数据就要到服务器上查询
        else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }


    //显示市内所有的县
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);

        countyList = DataSupport.where("cityid = ?",
                String.valueOf(selectedCity.getCityCode())).find(County.class);

        if (countyList.size()>0){
            //刷新数据列表
            dataList.clear();
            for (County county:countyList){
                dataList.add(county.getCountyName());
            }
            //通知数据更新，进行页面刷新
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            //接下来显示的是县一级的数据
            currentLevel = LEVEL_COUNTY;
        }
        //如果没有数据就要到服务器上查询
        else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }

    //从服务器中获取数据
    private void queryFromServer(String address,String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //关闭进度条，回到主线程
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_LONG);
                    }
                });
            }


            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                //将json数据解析后放到数据库中
                String resopnseText = response.body().string();
                boolean result = false;
                if (type.equals("province")){
                    result = Utility.handleProvinceResponse(resopnseText);
                }
                else if (type.equals("city")){
                    result = Utility.handleCityResponse(resopnseText,selectedProvince.getProvinceCode());
                }
                else if (type.equals("county")){
                    result = Utility.handleCountyResponse(resopnseText,selectedCity.getCityCode());
                }

                //拿到从服务器返回的数据是保存在了数据库中，
                // 因此再从数据库中将这些数据读取到相应的list中
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                    }
                    closeProgressDialog();
                }

        });
    }

    private void showProgressDialog() {
        //进度条还未创建
        if (progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载");
            //没有取消按钮
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
    }

    public void closeProgressDialog(){
        if (progressDialog!=null) progressDialog.dismiss();
    }
}