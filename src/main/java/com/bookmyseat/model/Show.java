package com.bookmyseat.model;

import com.bookmyseat.model.enums.ShowStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Show {
    private int showId;
    private int screenId;
    private int movieId;
    private LocalDateTime showDatetime;  // stored in UTC
    private BigDecimal basePrice;
    private ShowStatus status;

    // Denormalised fields for display (populated by DAO joins)
    private String movieTitle;
    private String screenName;

    public Show() {}

    public Show(int showId, int screenId, int movieId, LocalDateTime showDatetime,
                BigDecimal basePrice, ShowStatus status) {
        this.showId = showId;
        this.screenId = screenId;
        this.movieId = movieId;
        this.showDatetime = showDatetime;
        this.basePrice = basePrice;
        this.status = status;
    }

    /** Convert UTC datetime → IST for display */
    public String getShowTimeIST() {
        ZonedDateTime utc = showDatetime.atZone(ZoneId.of("UTC"));
        ZonedDateTime ist = utc.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
        return ist.format(DateTimeFormatter.ofPattern("dd MMM yyyy  hh:mm a"));
    }

    public int getShowId()                  { return showId; }
    public void setShowId(int v)            { this.showId = v; }
    public int getScreenId()                { return screenId; }
    public void setScreenId(int v)          { this.screenId = v; }
    public int getMovieId()                 { return movieId; }
    public void setMovieId(int v)           { this.movieId = v; }
    public LocalDateTime getShowDatetime()  { return showDatetime; }
    public void setShowDatetime(LocalDateTime v){ this.showDatetime = v; }
    public BigDecimal getBasePrice()        { return basePrice; }
    public void setBasePrice(BigDecimal v)  { this.basePrice = v; }
    public ShowStatus getStatus()           { return status; }
    public void setStatus(ShowStatus v)     { this.status = v; }
    public String getMovieTitle()           { return movieTitle; }
    public void setMovieTitle(String v)     { this.movieTitle = v; }
    public String getScreenName()           { return screenName; }
    public void setScreenName(String v)     { this.screenName = v; }

    @Override
    public String toString() {
        return "[" + showId + "] " + movieTitle + " | " + screenName +
                " | " + getShowTimeIST() + " | " + status;
    }
}
