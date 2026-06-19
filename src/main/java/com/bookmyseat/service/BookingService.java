package com.bookmyseat.service;

import com.bookmyseat.dao.*;
import com.bookmyseat.exception.SeatUnavailableException;
import com.bookmyseat.model.*;
import com.bookmyseat.model.enums.BookingStatus;
import com.bookmyseat.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * BookingService — ACID booking engine.
 *
 * Critical rules enforced here:
 *  1. All DAO calls share the SAME Connection (passed in via TransactionManager).
 *  2. Seats are locked with SELECT ... FOR UPDATE before checking availability.
 *  3. TRANSACTION_SERIALIZABLE isolation must be set by the caller (TransactionManager.begin()).
 *  4. On SeatUnavailableException the caller must rollback.
 */
public class BookingService {

    private final BookingDAO    bookingDAO    = new BookingDAO();
    private final BookedSeatDAO bookedSeatDAO = new BookedSeatDAO();
    private final PaymentDAO    paymentDAO    = new PaymentDAO();

    /**
     * Book seats atomically.
     *
     * @param conn       connection from TransactionManager (must be in active transaction)
     * @param customerId the booking customer
     * @param showId     the target show
     * @param seatIds    layout_ids to book
     * @param total      pre-calculated total amount
     * @return the new booking_id
     * @throws SeatUnavailableException if any seat is already taken (caller must rollback)
     */
    public int bookSeats(Connection conn, int customerId, int showId,
                         List<Integer> seatIds, BigDecimal total)
            throws SQLException, SeatUnavailableException {

        // ── Step 1: Lock the target seats with FOR UPDATE ─────────────────
        // Prevents concurrent transactions from booking the same seats.
        String lockSQL = buildInClause(
                "SELECT bs.seat_id FROM BookedSeats bs " +
                "JOIN Bookings b ON bs.booking_id = b.booking_id " +
                "WHERE bs.show_id = ? AND bs.seat_id IN (",
                seatIds.size()) + " AND b.status = 'CONFIRMED' FOR UPDATE";

        try (PreparedStatement ps = conn.prepareStatement(lockSQL)) {
            ps.setInt(1, showId);
            for (int i = 0; i < seatIds.size(); i++) ps.setInt(i + 2, seatIds.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // At least one seat is already booked
                    int takenSeatId = rs.getInt("seat_id");
                    String seatCode = resolveSeatCode(conn, takenSeatId);
                    throw new SeatUnavailableException(seatCode);
                }
            }
        }

        // ── Step 2: Insert Booking ─────────────────────────────────────────
        Booking booking = new Booking();
        booking.setCustomerId(customerId);
        booking.setShowId(showId);
        booking.setTotalAmount(total);
        booking.setStatus(BookingStatus.CONFIRMED);
        int bookingId = bookingDAO.save(conn, booking);

        // ── Step 3: Insert BookedSeats (batch) ────────────────────────────
        List<BookedSeat> bookedSeats = new ArrayList<>();
        for (int seatId : seatIds) {
            BookedSeat bs = new BookedSeat(0, bookingId, showId, seatId);
            bookedSeats.add(bs);
        }
        bookedSeatDAO.saveBatch(conn, bookedSeats);

        // ── Step 4: Insert Payment ─────────────────────────────────────────
        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setAmount(total);
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentDAO.save(conn, payment);

        return bookingId;
    }

    /** Returns a customer's bookings with seat details. */
    public List<Booking> getMyBookings(Connection conn, int customerId) throws SQLException {
        return bookingDAO.findByCustomer(conn, customerId);
    }

    /** Returns seat codes booked under a booking. */
    public List<BookedSeat> getBookedSeats(Connection conn, int bookingId) throws SQLException {
        return bookedSeatDAO.findByBooking(conn, bookingId);
    }

    /**
     * Cancel a booking within the allowed window.
     * Caller is responsible for the 2-hour window check before calling this.
     */
    public void cancelBooking(Connection conn, int bookingId) throws SQLException {
        bookedSeatDAO.deleteByBooking(conn, bookingId);
        paymentDAO.refund(conn, bookingId);
        bookingDAO.cancel(conn, bookingId);
    }

    public Optional<Booking> getBookingById(Connection conn, int bookingId) throws SQLException {
        return bookingDAO.findById(conn, bookingId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String buildInClause(String prefix, int count) {
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(",");
            sb.append("?");
        }
        sb.append(")");
        return sb.toString();
    }

    private String resolveSeatCode(Connection conn, int layoutId) {
        try {
            String sql = "SELECT CONCAT(row_label, col_num) AS code FROM ScreenLayouts WHERE layout_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, layoutId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getString("code");
                }
            }
        } catch (SQLException ignored) {}
        return "seat#" + layoutId;
    }
}
