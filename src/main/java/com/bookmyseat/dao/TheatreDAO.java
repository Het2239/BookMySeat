package com.bookmyseat.dao;

import com.bookmyseat.model.Theatre;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TheatreDAO {

    public List<Theatre> findByCity(Connection conn, int cityId) throws SQLException {
        List<Theatre> list = new ArrayList<>();
        String sql = "SELECT * FROM Theatres WHERE city_id = ? ORDER BY name";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Returns all theatres managed by a given SP user. */
    public List<Theatre> findBySP(Connection conn, int spUserId) throws SQLException {
        List<Theatre> list = new ArrayList<>();
        String sql = "SELECT * FROM Theatres WHERE sp_user_id = ? ORDER BY name";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, spUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Optional<Theatre> findById(Connection conn, int theatreId) throws SQLException {
        String sql = "SELECT * FROM Theatres WHERE theatre_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, theatreId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public int save(Connection conn, Theatre t) throws SQLException {
        String sql = "INSERT INTO Theatres (name, address, city_id, sp_user_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getName());
            ps.setString(2, t.getAddress());
            ps.setInt(3, t.getCityId());
            ps.setInt(4, t.getSpUserId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("No generated key for Theatre insert.");
            }
        }
    }

    private Theatre map(ResultSet rs) throws SQLException {
        return new Theatre(
            rs.getInt("theatre_id"),
            rs.getString("name"),
            rs.getString("address"),
            rs.getInt("city_id"),
            rs.getInt("sp_user_id")
        );
    }
}
