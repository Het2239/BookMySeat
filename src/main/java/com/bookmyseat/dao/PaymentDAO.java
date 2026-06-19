package com.bookmyseat.dao;

import com.bookmyseat.model.Payment;
import com.bookmyseat.model.enums.PaymentStatus;
import java.sql.*;
import java.util.Optional;

public class PaymentDAO {

    /** Insert a payment record. Returns generated payment_id. */
    public int save(Connection conn, Payment p) throws SQLException {
        String sql = "INSERT INTO Payments (booking_id, amount, status) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getBookingId());
            ps.setBigDecimal(2, p.getAmount());
            ps.setString(3, p.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("No generated key for Payment insert.");
            }
        }
    }

    public Optional<Payment> findByBooking(Connection conn, int bookingId) throws SQLException {
        String sql = "SELECT * FROM Payments WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    /** Mark payment as REFUNDED — used during cancellation. */
    public void refund(Connection conn, int bookingId) throws SQLException {
        String sql = "UPDATE Payments SET status = 'REFUNDED' WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.executeUpdate();
        }
    }

    private Payment map(ResultSet rs) throws SQLException {
        return new Payment(
            rs.getInt("payment_id"),
            rs.getInt("booking_id"),
            rs.getBigDecimal("amount"),
            PaymentStatus.valueOf(rs.getString("status")),
            rs.getObject("paid_at", java.time.LocalDateTime.class)
        );
    }
}
