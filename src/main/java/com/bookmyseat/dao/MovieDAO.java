package com.bookmyseat.dao;

import com.bookmyseat.model.Movie;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MovieDAO {

    public List<Movie> findAll(Connection conn) throws SQLException {
        List<Movie> list = new ArrayList<>();
        String sql = "SELECT * FROM Movies ORDER BY title";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Optional<Movie> findById(Connection conn, int movieId) throws SQLException {
        String sql = "SELECT * FROM Movies WHERE movie_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public int save(Connection conn, Movie m) throws SQLException {
        String sql = "INSERT INTO Movies (title, genre, duration_mins, language, rating, release_date, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getTitle());
            ps.setString(2, m.getGenre());
            ps.setInt(3, m.getDurationMins());
            ps.setString(4, m.getLanguage());
            ps.setString(5, m.getRating());
            ps.setDate(6, Date.valueOf(m.getReleaseDate()));
            ps.setString(7, m.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("No generated key for Movie insert.");
            }
        }
    }

    private Movie map(ResultSet rs) throws SQLException {
        return new Movie(
            rs.getInt("movie_id"),
            rs.getString("title"),
            rs.getString("genre"),
            rs.getInt("duration_mins"),
            rs.getString("language"),
            rs.getString("rating"),
            rs.getDate("release_date").toLocalDate(),
            rs.getString("description")
        );
    }
}
