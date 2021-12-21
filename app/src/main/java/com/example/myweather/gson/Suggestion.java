package com.example.myweather.gson;

import com.google.gson.annotations.SerializedName;

public class Suggestion {

    @SerializedName("comf")
    public Comf comf;
    public class Comf{
        @SerializedName("txt")
        public String tipForComfort;
    }

    @SerializedName("sport")
    public Sport sport;
    public class Sport{
        @SerializedName("txt")
        public String tipForSport;
    }

    @SerializedName("cw")
    public Cw cw;
    public class Cw{
        @SerializedName("txt")
        public String tipForCarWash;
    }




}
