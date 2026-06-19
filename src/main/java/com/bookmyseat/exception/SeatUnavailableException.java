package com.bookmyseat.exception;

/**
 * Thrown when a customer attempts to book a seat that has already been taken.
 * This is a checked exception so the booking flow is forced to handle it explicitly.
 */
public class SeatUnavailableException extends Exception {

    private final String seatCode;

    public SeatUnavailableException(String seatCode) {
        super("Seat " + seatCode + " is no longer available.");
        this.seatCode = seatCode;
    }

    public String getSeatCode() {
        return seatCode;
    }
}
