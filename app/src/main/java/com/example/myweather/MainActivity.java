package com.example.myweather;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.ArraySet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.myweather.R;
import com.example.myweather.db.FavCity;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private TextView textView;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView view = findViewById(R.id.fav_city);
        textView = findViewById(R.id.fav_text);

        List<FavCity> all = DataSupport.findAll(FavCity.class);
        if (all.size()==0) textView.setVisibility(View.GONE);
        else {
            ArrayList<String> cityName = new ArrayList<>();
            ArrayList<String> weatherId = new ArrayList<>();
            for (FavCity favCity :all ){
                cityName.add(favCity.getCountyName());
                weatherId.add(favCity.getWeather_id());
            }
            ArrayAdapter arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, cityName);
            view.setAdapter(arrayAdapter);
            view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(MainActivity.this, WeatherActivity2.class);
                    intent.putExtra("weather_id",weatherId.get(position));
                    intent.putExtra("county_id",cityName.get(position));
                    startActivity(intent);
                    finish();
                }
            });
        }
    }


}
