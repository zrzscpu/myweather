package com.example.myweather.gson;

import com.google.gson.annotations.SerializedName;

public class Forecast {
    @SerializedName("date")
    public String date;
    @SerializedName("cond")
    public Condition condition;
    public class Condition{
        @SerializedName("txt_d")
        public String condition;
    }

    @SerializedName("tmp")
    public Temperature temperature;
    public class Temperature{
        
        @SerializedName("max")
        public String max;

        @SerializedName("min")
        public String min;
    }
}
