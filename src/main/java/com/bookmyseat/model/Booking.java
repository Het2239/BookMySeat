package com.bookmyseat.model;

import com.bookmyseat.model.enums.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Booking {
    private int bookingId;
    private int customerId;
    private int showId;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private LocalDateTime bookedAt;

    public Booking() {}

    public Booking(int bookingId, int customerId, int showId,
                   BigDecimal totalAmount, BookingStatus status, LocalDateTime bookedAt) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.showId = showId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.bookedAt = bookedAt;
    }

    public int getBookingId()               { return bookingId; }
    public void setBookingId(int v)         { this.bookingId = v; }
    public int getCustomerId()              { return customerId; }
    public void setCustomerId(int v)        { this.customerId = v; }
    public int getShowId()                  { return showId; }
    public void setShowId(int v)            { this.showId = v; }
    public BigDecimal getTotalAmount()      { return totalAmount; }
    public void setTotalAmount(BigDecimal v){ this.totalAmount = v; }
    public BookingStatus getStatus()        { return status; }
    public void setStatus(BookingStatus v)  { this.status = v; }
    public LocalDateTime getBookedAt()      { return bookedAt; }
    public void setBookedAt(LocalDateTime v){ this.bookedAt = v; }

    @Override
    public String toString() {
        return "Booking#" + bookingId + " | Show:" + showId +
                " | ₹" + totalAmount + " | " + status;
    }
}
