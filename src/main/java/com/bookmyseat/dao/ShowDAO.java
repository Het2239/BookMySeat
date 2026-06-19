package com.bookmyseat.dao;

import com.bookmyseat.model.Show;
import com.bookmyseat.model.ShowAvailability;
import com.bookmyseat.model.enums.ShowStatus;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShowDAO {

    /** All AVAILABLE shows for a screen — used by seat matrix. */
    public List<Show> findAvailableByScreen(Connection conn, int screenId) throws SQLException {
        List<Show> list = new ArrayList<>();
        String sql = "SELECT s.*, m.title AS movie_title, sc.screen_name " +
                     "FROM Shows s " +
                     "JOIN Movies m ON s.movie_id = m.movie_id " +
                     "JOIN Screens sc ON s.screen_id = sc.screen_id " +
                     "WHERE s.screen_id = ? AND s.status = 'AVAILABLE' " +
                     "ORDER BY s.show_datetime";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, screenId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapFull(rs));
            }
        }
        return list;
    }

    /** All shows (any status) for a screen — used by SP dashboard. */
    public List<Show> findByScreen(Connection conn, int screenId) throws SQLException {
        List<Show> list = new ArrayList<>();
        String sql = "SELECT s.*, m.title AS movie_title, sc.screen_name " +
                     "FROM Shows s " +
                     "JOIN Movies m ON s.movie_id = m.movie_id " +
                     "JOIN Screens sc ON s.screen_id = sc.screen_id " +
                     "WHERE s.screen_id = ? ORDER BY s.show_datetime";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, screenId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapFull(rs));
            }
        }
        return list;
    }

    /** All AVAILABLE shows for a theatre — used by customer browse. */
    public List<Show> findAvailableByTheatre(Connection conn, int theatreId) throws SQLException {
        List<Show> list = new ArrayList<>();
        String sql = "SELECT s.*, m.title AS movie_title, sc.screen_name " +
                     "FROM Shows s " +
                     "JOIN Movies m ON s.movie_id = m.movie_id " +
                     "JOIN Screens sc ON s.screen_id = sc.screen_id " +
                     "WHERE sc.theatre_id = ? AND s.status = 'AVAILABLE' " +
                     "ORDER BY s.show_datetime";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, theatreId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapFull(rs));
            }
        }
        return list;
    }

    public Optional<Show> findById(Connection conn, int showId) throws SQLException {
        String sql = "SELECT s.*, m.title AS movie_title, sc.screen_name " +
                     "FROM Shows s " +
                     "JOIN Movies m ON s.movie_id = m.movie_id " +
                     "JOIN Screens sc ON s.screen_id = sc.screen_id " +
                     "WHERE s.show_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapFull(rs)) : Optional.empty();
            }
        }
    }

    public int save(Connection conn, Show show) throws SQLException {
        String sql = "INSERT INTO Shows (screen_id, movie_id, show_datetime, base_price, status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, show.getScreenId());
            ps.setInt(2, show.getMovieId());
            ps.setObject(3, show.getShowDatetime());
            ps.setBigDecimal(4, show.getBasePrice());
            ps.setString(5, show.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("No generated key for Show insert.");
            }
        }
    }

    public void updateStatus(Connection conn, int showId, ShowStatus status) throws SQLException {
        String sql = "UPDATE Shows SET status = ? WHERE show_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, showId);
            ps.executeUpdate();
        }
    }

    /**
     * Queries v_show_availability for all shows belonging to a given theatre.
     * Used by the SP booking report to display live seat availability.
     * Requires db_objects.sql to have been loaded first.
     */
    public List<ShowAvailability> findAvailabilityByTheatre(Connection conn, int theatreId)
            throws SQLException {
        List<ShowAvailability> list = new ArrayList<>();
        String sql = "SELECT * FROM v_show_availability WHERE theatre_id = ? ORDER BY show_datetime";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, theatreId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapAvailability(rs));
            }
        }
        return list;
    }

    // ── Mappers ───────────────────────────────────────────────

    private Show mapFull(ResultSet rs) throws SQLException {
        Show s = new Show(
            rs.getInt("show_id"),
            rs.getInt("screen_id"),
            rs.getInt("movie_id"),
            rs.getObject("show_datetime", java.time.LocalDateTime.class),
            rs.getBigDecimal("base_price"),
            ShowStatus.valueOf(rs.getString("status"))
        );
        s.setMovieTitle(rs.getString("movie_title"));
        s.setScreenName(rs.getString("screen_name"));
        return s;
    }

    private ShowAvailability mapAvailability(ResultSet rs) throws SQLException {
        ShowAvailability sa = new ShowAvailability();
        sa.setShowId(rs.getInt("show_id"));
        sa.setMovieTitle(rs.getString("movie_title"));
        sa.setShowDatetime(rs.getObject("show_datetime", java.time.LocalDateTime.class));
        sa.setBasePrice(rs.getBigDecimal("base_price"));
        sa.setStatus(ShowStatus.valueOf(rs.getString("status")));
        sa.setScreenId(rs.getInt("screen_id"));
        sa.setScreenName(rs.getString("screen_name"));
        sa.setTheatreId(rs.getInt("theatre_id"));
        sa.setTheatreName(rs.getString("theatre_name"));
        sa.setCityId(rs.getInt("city_id"));
        sa.setCityName(rs.getString("city_name"));
        sa.setTotalSeats(rs.getInt("total_seats"));
        sa.setBookedSeats(rs.getInt("booked_seats"));
        sa.setAvailableSeats(rs.getInt("available_seats"));
        return sa;
    }
}
