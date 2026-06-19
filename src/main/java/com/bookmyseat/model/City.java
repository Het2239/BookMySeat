package com.bookmyseat.model;

public class City {
    private int cityId;
    private String cityName;

    public City() {}

    public City(int cityId, String cityName) {
        this.cityId = cityId;
        this.cityName = cityName;
    }

    public int getCityId()          { return cityId; }
    public void setCityId(int v)    { this.cityId = v; }
    public String getCityName()     { return cityName; }
    public void setCityName(String v){ this.cityName = v; }

    @Override
    public String toString() {
        return "[" + cityId + "] " + cityName;
    }
}
