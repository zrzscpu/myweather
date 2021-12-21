package com.example.myweather.util;

import android.text.TextUtils;

import com.example.myweather.db.City;
import com.example.myweather.db.County;
import com.example.myweather.db.Province;
import com.example.myweather.gson.Weather;
import com.google.gson.Gson;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;

public class Utility {

    //解析省级的数据
    public static boolean handleProvinceResponse(String resp)  {
        if (!TextUtils.isEmpty(resp)){
            try {
                JSONArray allProvince = new JSONArray(resp);
                for (int i = 0; i < allProvince.length(); i++) {
                    //拿到一个json对象，解析成province
                    JSONObject provinceObject = allProvince.getJSONObject(i);
                    Province province = new Province();
                    //从json中获得name和城市的编号
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }

        }
        return false;
    }

    //解析市级的数据
    public static boolean handleCityResponse(String resp,int provinceId)  {
        if (!TextUtils.isEmpty(resp)){
            try {
                JSONArray allProvince = new JSONArray(resp);
                for (int i = 0; i < allProvince.length(); i++) {
                    //拿到一个json对象，解析成province
                    JSONObject cityObject = allProvince.getJSONObject(i);
                    City city = new City();
                    //从json中获得name和城市的编号
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();

                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }

        }
        return false;
    }

    //解析县级的数据
    public static boolean handleCountyResponse(String resp,int cityId)  {
        if (!TextUtils.isEmpty(resp)){
            try {
                JSONArray allProvince = new JSONArray(resp);
                for (int i = 0; i < allProvince.length(); i++) {
                    //拿到一个json对象，解析成province
                    JSONObject countyObject = allProvince.getJSONObject(i);
                    County county = new County();
                    //从json中获得name和城市的编号
                    county.setCityId(cityId);
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }

        }
        return false;
    }

    //解析天气数据
    public static Weather handleWeatherResponse(String resp)  {
        if (!TextUtils.isEmpty(resp)){
            try {
                JSONObject jsonObject = new JSONObject(resp);
                JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
                String weatherContent = jsonArray.get(0).toString();
                return new Gson().fromJson(weatherContent, (Type) Weather.class);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        return null;
    }

}
