package com.example.myweather.gson;

import com.google.gson.annotations.SerializedName;

public class Now {
    @SerializedName("tmp")
    public String temperature;
    @SerializedName("cond")
    public Cond cond;
    public class Cond{
        @SerializedName("txt")
        public String info;
    }
}
