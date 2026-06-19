package com.bookmyseat.dao;

import com.bookmyseat.model.City;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CityDAO {

    /** Returns all cities ordered by name. */
    public List<City> findAll(Connection conn) throws SQLException {
        List<City> cities = new ArrayList<>();
        String sql = "SELECT city_id, city_name FROM Cities ORDER BY city_name";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cities.add(map(rs));
            }
        }
        return cities;
    }

    public City findById(Connection conn, int cityId) throws SQLException {
        String sql = "SELECT city_id, city_name FROM Cities WHERE city_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cityId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private City map(ResultSet rs) throws SQLException {
        return new City(
            rs.getInt("city_id"),
            rs.getString("city_name")
        );
    }
}
