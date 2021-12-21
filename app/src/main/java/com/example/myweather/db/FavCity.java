package com.example.myweather.db;

import org.litepal.crud.DataSupport;

public class FavCity extends DataSupport {
    private String weather_id;
    private String countyName;

    public void setWeather_id(String weather_id) {
        this.weather_id = weather_id;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getWeather_id() {
        return weather_id;
    }

    public String getCountyName() {
        return countyName;
    }

    public FavCity(String weather_id, String countyName) {
        this.weather_id = weather_id;
        this.countyName = countyName;
    }
}
