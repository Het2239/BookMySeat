package com.bookmyseat.model;

import com.bookmyseat.model.enums.ShowStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Maps a row from the v_show_availability view.
 * Used by the SP booking report dashboard.
 */
public class ShowAvailability {

    private int showId;
    private String movieTitle;
    private LocalDateTime showDatetime; // UTC
    private BigDecimal basePrice;
    private ShowStatus status;
    private int screenId;
    private String screenName;
    private int theatreId;
    private String theatreName;
    private int cityId;
    private String cityName;
    private int totalSeats;
    private int bookedSeats;
    private int availableSeats;

    public ShowAvailability() {}

    /** Convert UTC stored datetime → IST for display */
    public String getShowTimeIST() {
        ZonedDateTime utc = showDatetime.atZone(ZoneId.of("UTC"));
        ZonedDateTime ist = utc.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
        return ist.format(DateTimeFormatter.ofPattern("dd MMM yyyy  hh:mm a"));
    }

    public int getShowId()                  { return showId; }
    public void setShowId(int v)            { this.showId = v; }
    public String getMovieTitle()           { return movieTitle; }
    public void setMovieTitle(String v)     { this.movieTitle = v; }
    public LocalDateTime getShowDatetime()  { return showDatetime; }
    public void setShowDatetime(LocalDateTime v){ this.showDatetime = v; }
    public BigDecimal getBasePrice()        { return basePrice; }
    public void setBasePrice(BigDecimal v)  { this.basePrice = v; }
    public ShowStatus getStatus()           { return status; }
    public void setStatus(ShowStatus v)     { this.status = v; }
    public int getScreenId()                { return screenId; }
    public void setScreenId(int v)          { this.screenId = v; }
    public String getScreenName()           { return screenName; }
    public void setScreenName(String v)     { this.screenName = v; }
    public int getTheatreId()               { return theatreId; }
    public void setTheatreId(int v)         { this.theatreId = v; }
    public String getTheatreName()          { return theatreName; }
    public void setTheatreName(String v)    { this.theatreName = v; }
    public int getCityId()                  { return cityId; }
    public void setCityId(int v)            { this.cityId = v; }
    public String getCityName()             { return cityName; }
    public void setCityName(String v)       { this.cityName = v; }
    public int getTotalSeats()              { return totalSeats; }
    public void setTotalSeats(int v)        { this.totalSeats = v; }
    public int getBookedSeats()             { return bookedSeats; }
    public void setBookedSeats(int v)       { this.bookedSeats = v; }
    public int getAvailableSeats()          { return availableSeats; }
    public void setAvailableSeats(int v)    { this.availableSeats = v; }

    /** Occupancy % for display */
    public String getOccupancy() {
        if (totalSeats == 0) return "N/A";
        return String.format("%.0f%%", (bookedSeats * 100.0) / totalSeats);
    }
}
