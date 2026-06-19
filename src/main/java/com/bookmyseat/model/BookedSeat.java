package com.bookmyseat.model;

public class BookedSeat {
    private int id;
    private int bookingId;
    private int showId;
    private int seatId;      // FK → ScreenLayouts.layout_id

    // Denormalised for display
    private String seatCode; // e.g. "A3"

    public BookedSeat() {}

    public BookedSeat(int id, int bookingId, int showId, int seatId) {
        this.id = id;
        this.bookingId = bookingId;
        this.showId = showId;
        this.seatId = seatId;
    }

    public int getId()              { return id; }
    public void setId(int v)        { this.id = v; }
    public int getBookingId()       { return bookingId; }
    public void setBookingId(int v) { this.bookingId = v; }
    public int getShowId()          { return showId; }
    public void setShowId(int v)    { this.showId = v; }
    public int getSeatId()          { return seatId; }
    public void setSeatId(int v)    { this.seatId = v; }
    public String getSeatCode()     { return seatCode; }
    public void setSeatCode(String v){ this.seatCode = v; }

    @Override
    public String toString() {
        return "BookedSeat{seatId=" + seatId + ", code=" + seatCode + "}";
    }
}
