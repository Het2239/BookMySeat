package com.bookmyseat.model;

import com.bookmyseat.model.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private int paymentId;
    private int bookingId;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime paidAt;

    public Payment() {}

    public Payment(int paymentId, int bookingId, BigDecimal amount,
                   PaymentStatus status, LocalDateTime paidAt) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.amount = amount;
        this.status = status;
        this.paidAt = paidAt;
    }

    public int getPaymentId()               { return paymentId; }
    public void setPaymentId(int v)         { this.paymentId = v; }
    public int getBookingId()               { return bookingId; }
    public void setBookingId(int v)         { this.bookingId = v; }
    public BigDecimal getAmount()           { return amount; }
    public void setAmount(BigDecimal v)     { this.amount = v; }
    public PaymentStatus getStatus()        { return status; }
    public void setStatus(PaymentStatus v)  { this.status = v; }
    public LocalDateTime getPaidAt()        { return paidAt; }
    public void setPaidAt(LocalDateTime v)  { this.paidAt = v; }

    @Override
    public String toString() {
        return "Payment#" + paymentId + " | ₹" + amount + " | " + status;
    }
}
