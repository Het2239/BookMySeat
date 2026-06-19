package com.bookmyseat.dao;

import com.bookmyseat.model.Screen;
import com.bookmyseat.model.ScreenLayout;
import com.bookmyseat.model.SeatTypeZone;
import com.bookmyseat.model.enums.SeatType;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScreenDAO {

    public List<Screen> findByTheatre(Connection conn, int theatreId) throws SQLException {
        List<Screen> list = new ArrayList<>();
        String sql = "SELECT * FROM Screens WHERE theatre_id = ? ORDER BY screen_name";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, theatreId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapScreen(rs));
            }
        }
        return list;
    }

    public Optional<Screen> findById(Connection conn, int screenId) throws SQLException {
        String sql = "SELECT * FROM Screens WHERE screen_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, screenId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapScreen(rs)) : Optional.empty();
            }
        }
    }

    public int saveScreen(Connection conn, Screen s) throws SQLException {
        String sql = "INSERT INTO Screens (theatre_id, screen_name, total_rows, total_cols) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getTheatreId());
            ps.setString(2, s.getScreenName());
            ps.setInt(3, s.getTotalRows());
            ps.setInt(4, s.getTotalCols());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("No generated key for Screen insert.");
            }
        }
    }

    /** Fetch all active seat layouts for a screen, ordered by row then col. */
    public List<ScreenLayout> findLayoutByScreen(Connection conn, int screenId) throws SQLException {
        List<ScreenLayout> list = new ArrayList<>();
        String sql = "SELECT * FROM ScreenLayouts WHERE screen_id = ? AND is_active = 1 " +
                     "ORDER BY row_label, col_num";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, screenId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapLayout(rs));
            }
        }
        return list;
    }

    /** Fetch the set of booked seat IDs for a given show (for matrix rendering). */
    public List<Integer> findBookedSeatIds(Connection conn, int showId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT seat_id FROM BookedSeats " +
                     "JOIN Bookings b ON BookedSeats.booking_id = b.booking_id " +
                     "WHERE BookedSeats.show_id = ? AND b.status = 'CONFIRMED'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("seat_id"));
            }
        }
        return ids;
    }

    /** Batch insert seat layout rows. */
    public void saveLayouts(Connection conn, List<ScreenLayout> layouts) throws SQLException {
        String sql = "INSERT INTO ScreenLayouts (screen_id, row_label, col_num, seat_type, is_aisle) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (ScreenLayout sl : layouts) {
                ps.setInt(1, sl.getScreenId());
                ps.setString(2, String.valueOf(sl.getRowLabel()));
                ps.setInt(3, sl.getColNum());
                ps.setString(4, sl.getSeatType().name());
                ps.setBoolean(5, sl.isAisle());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Batch insert zone definitions. */
    public void saveZones(Connection conn, List<SeatTypeZone> zones) throws SQLException {
        String sql = "INSERT INTO SeatTypeZones (screen_id, zone_type, row_start, row_end, price) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (SeatTypeZone z : zones) {
                ps.setInt(1, z.getScreenId());
                ps.setString(2, z.getZoneType().name());
                ps.setString(3, String.valueOf(z.getRowStart()));
                ps.setString(4, String.valueOf(z.getRowEnd()));
                ps.setBigDecimal(5, z.getPrice());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<SeatTypeZone> findZonesByScreen(Connection conn, int screenId) throws SQLException {
        List<SeatTypeZone> list = new ArrayList<>();
        String sql = "SELECT * FROM SeatTypeZones WHERE screen_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, screenId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SeatTypeZone z = new SeatTypeZone();
                    z.setZoneId(rs.getInt("zone_id"));
                    z.setScreenId(rs.getInt("screen_id"));
                    z.setZoneType(SeatType.valueOf(rs.getString("zone_type")));
                    z.setRowStart(rs.getString("row_start").charAt(0));
                    z.setRowEnd(rs.getString("row_end").charAt(0));
                    z.setPrice(rs.getBigDecimal("price"));
                    list.add(z);
                }
            }
        }
        return list;
    }

    private Screen mapScreen(ResultSet rs) throws SQLException {
        return new Screen(
            rs.getInt("screen_id"),
            rs.getInt("theatre_id"),
            rs.getString("screen_name"),
            rs.getInt("total_rows"),
            rs.getInt("total_cols")
        );
    }

    private ScreenLayout mapLayout(ResultSet rs) throws SQLException {
        return new ScreenLayout(
            rs.getInt("layout_id"),
            rs.getInt("screen_id"),
            rs.getString("row_label").charAt(0),
            rs.getInt("col_num"),
            SeatType.valueOf(rs.getString("seat_type")),
            rs.getBoolean("is_aisle"),
            rs.getBoolean("is_active")
        );
    }
}
