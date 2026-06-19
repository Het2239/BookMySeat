package com.bookmyseat.model;

public class Theatre {
    private int theatreId;
    private String name;
    private String address;
    private int cityId;
    private int spUserId;

    public Theatre() {}

    public Theatre(int theatreId, String name, String address, int cityId, int spUserId) {
        this.theatreId = theatreId;
        this.name = name;
        this.address = address;
        this.cityId = cityId;
        this.spUserId = spUserId;
    }

    public int getTheatreId()           { return theatreId; }
    public void setTheatreId(int v)     { this.theatreId = v; }
    public String getName()             { return name; }
    public void setName(String v)       { this.name = v; }
    public String getAddress()          { return address; }
    public void setAddress(String v)    { this.address = v; }
    public int getCityId()              { return cityId; }
    public void setCityId(int v)        { this.cityId = v; }
    public int getSpUserId()            { return spUserId; }
    public void setSpUserId(int v)      { this.spUserId = v; }

    @Override
    public String toString() {
        return "[" + theatreId + "] " + name + " — " + address;
    }
}
