package com.example.myweather;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.ArraySet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myweather.db.FavCity;
import com.example.myweather.gson.Forecast;
import com.example.myweather.gson.Now;
import com.example.myweather.gson.Weather;
import com.example.myweather.util.HttpUtil;
import com.example.myweather.util.Utility;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WeatherActivity2 extends AppCompatActivity {


    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfo;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView confortT;
    private TextView carWashT;
    private TextView sportT;
    private SwipeRefreshLayout refreshLayout;
    private Button addAsFav;
    private Button backHome;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfo = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        confortT = findViewById(R.id.tip_for_confort);
        carWashT = findViewById(R.id.tip_for_wash_car);
        sportT = findViewById(R.id.tip_for_sport);



        //获得weatherid和countyname;
        String weather_id = getIntent().getStringExtra("weather_id");
        String countyName = getIntent().getStringExtra("county_id");

        weatherLayout.setVisibility(View.INVISIBLE);
        requestWeather(weather_id);


        //设置下拉刷新
        refreshLayout = findViewById(R.id.refresh);
        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.design_default_color_primary, null));

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(WeatherActivity2.this,"正在刷新",Toast.LENGTH_LONG).show();
                requestWeather(weather_id);
            }
        });
        //设置关注按钮
        addAsFav = findViewById(R.id.add_as_fav);
        addAsFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获得原有的
                FavCity favCity = new FavCity(weather_id, countyName);
                favCity.save();
                Toast.makeText(WeatherActivity2.this,"添加关注成功",Toast.LENGTH_LONG).show();

            }
        });

        //设置返回主页按钮
        backHome = findViewById(R.id.home);
        backHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeatherActivity2.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }

    private void requestWeather(String weather_id) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+weather_id+
                "&key=1fa30ba32cc94e428a1858df393200b2";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

                Toast.makeText(WeatherActivity2.this,"获取天气失败",Toast.LENGTH_LONG).show();
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String res = response.body().string();
                Weather weather = Utility.handleWeatherResponse(res);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //如果天气信息正常
                        if (weather!=null &&"ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity2.this).edit();
                            //查询到的数据加入到缓存中
                            editor.putString("weather",res);
                            editor.apply();
                            showWeatherInfo(weather);

                        }else {
                            Toast.makeText(WeatherActivity2.this,"获取天气失败",Toast.LENGTH_LONG).show();

                        }
                        refreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature+"度";
        String weatherinfo = weather.now.cond.info;

        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfo.setText(weatherinfo);


        //动态动管理forcast下的所有view
        forecastLayout.removeAllViews();

        for (Forecast forecast:weather.forecasts){
//            创建一个forecast_item 的view对象
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);

            TextView dateText  = view.findViewById(R.id.date_text);
            TextView infotext  = view.findViewById(R.id.info_text);
            TextView max_text  = view.findViewById(R.id.max_text);
            TextView min_text  = view.findViewById(R.id.min_text);

            //设置数据
            dateText.setText(forecast.date);
            infotext.setText(forecast.condition.condition);
            max_text.setText(forecast.temperature.max);
            min_text.setText(forecast.temperature.min);
            //将forecast_item 加到forecastlayout下
            forecastLayout.addView(view);
        }

        if (weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }

        String comfort = "舒适度"+weather.suggestion.comf.tipForComfort;
        String carWash = "洗车指数"+weather.suggestion.cw.tipForCarWash;
        String sport = "运动建议"+weather.suggestion.sport.tipForSport;
        confortT.setText(comfort);
        carWashT.setText(carWash);
        sportT.setText(sport);

        //数据加载完以后让这个页面可见
        weatherLayout.setVisibility(View.VISIBLE);

    }
}