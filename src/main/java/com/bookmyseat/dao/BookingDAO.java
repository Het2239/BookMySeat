package com.bookmyseat.dao;

import com.bookmyseat.model.Booking;
import com.bookmyseat.model.enums.BookingStatus;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingDAO {

    /** Insert booking record. Returns generated booking_id. */
    public int save(Connection conn, Booking b) throws SQLException {
        String sql = "INSERT INTO Bookings (customer_id, show_id, total_amount, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, b.getCustomerId());
            ps.setInt(2, b.getShowId());
            ps.setBigDecimal(3, b.getTotalAmount());
            ps.setString(4, b.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("No generated key for Booking insert.");
            }
        }
    }

    public List<Booking> findByCustomer(Connection conn, int customerId) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM Bookings WHERE customer_id = ? ORDER BY booked_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Optional<Booking> findById(Connection conn, int bookingId) throws SQLException {
        String sql = "SELECT * FROM Bookings WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    /** Set booking status to CANCELLED. */
    public void cancel(Connection conn, int bookingId) throws SQLException {
        String sql = "UPDATE Bookings SET status = 'CANCELLED' WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.executeUpdate();
        }
    }

    private Booking map(ResultSet rs) throws SQLException {
        return new Booking(
            rs.getInt("booking_id"),
            rs.getInt("customer_id"),
            rs.getInt("show_id"),
            rs.getBigDecimal("total_amount"),
            BookingStatus.valueOf(rs.getString("status")),
            rs.getObject("booked_at", java.time.LocalDateTime.class)
        );
    }
}
