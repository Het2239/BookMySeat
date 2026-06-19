package com.bookmyseat.dao;

import com.bookmyseat.model.BookedSeat;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookedSeatDAO {

    /**
     * Insert a single booked seat row.
     * All inserts in a booking must share the same Connection (TransactionManager).
     */
    public void save(Connection conn, BookedSeat bs) throws SQLException {
        String sql = "INSERT INTO BookedSeats (booking_id, show_id, seat_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bs.getBookingId());
            ps.setInt(2, bs.getShowId());
            ps.setInt(3, bs.getSeatId());
            ps.executeUpdate();
        }
    }

    /** Batch insert for multi-seat bookings. */
    public void saveBatch(Connection conn, List<BookedSeat> seats) throws SQLException {
        String sql = "INSERT INTO BookedSeats (booking_id, show_id, seat_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (BookedSeat bs : seats) {
                ps.setInt(1, bs.getBookingId());
                ps.setInt(2, bs.getShowId());
                ps.setInt(3, bs.getSeatId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Returns all booked seat_ids for a show — used by seat matrix renderer. */
    public List<Integer> findSeatIdsByShow(Connection conn, int showId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT bs.seat_id FROM BookedSeats bs " +
                     "JOIN Bookings b ON bs.booking_id = b.booking_id " +
                     "WHERE bs.show_id = ? AND b.status = 'CONFIRMED'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("seat_id"));
            }
        }
        return ids;
    }

    /** Returns booked seats for a booking — used in booking confirmation display. */
    public List<BookedSeat> findByBooking(Connection conn, int bookingId) throws SQLException {
        List<BookedSeat> list = new ArrayList<>();
        String sql = "SELECT bs.*, CONCAT(sl.row_label, sl.col_num) AS seat_code " +
                     "FROM BookedSeats bs " +
                     "JOIN ScreenLayouts sl ON bs.seat_id = sl.layout_id " +
                     "WHERE bs.booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookedSeat bs = new BookedSeat(
                        rs.getInt("id"),
                        rs.getInt("booking_id"),
                        rs.getInt("show_id"),
                        rs.getInt("seat_id")
                    );
                    bs.setSeatCode(rs.getString("seat_code"));
                    list.add(bs);
                }
            }
        }
        return list;
    }

    /** Delete all booked seats for a booking — used in cancellation. */
    public void deleteByBooking(Connection conn, int bookingId) throws SQLException {
        String sql = "DELETE FROM BookedSeats WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.executeUpdate();
        }
    }
}
