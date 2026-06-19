package com.bookmyseat.dao;

import com.bookmyseat.model.User;
import com.bookmyseat.model.enums.Role;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class UserDAO {

    /**
     * Inserts a new user. Returns the generated user_id.
     * password_hash must already be BCrypt-hashed by the service layer.
     */
    public int register(Connection conn, User user) throws SQLException {
        String sql = "INSERT INTO Users (name, email, password_hash, phone, role) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getRole().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("Insert succeeded but no generated key returned.");
            }
        }
    }

    /** Lookup by email — used for login. Returns empty if not found. */
    public Optional<User> findByEmail(Connection conn, String email) throws SQLException {
        String sql = "SELECT * FROM Users WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<User> findById(Connection conn, int userId) throws SQLException {
        String sql = "SELECT * FROM Users WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    /** Check if an email is already registered. */
    public boolean emailExists(Connection conn, String email) throws SQLException {
        String sql = "SELECT 1 FROM Users WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("user_id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password_hash"),
            rs.getString("phone"),
            Role.valueOf(rs.getString("role")),
            rs.getObject("created_at", LocalDateTime.class)
        );
    }
}
