package com.bookmyseat.service;

import com.bookmyseat.dao.*;
import com.bookmyseat.model.*;
import java.sql.*;
import java.util.List;
import java.util.Optional;

/**
 * CustomerService — read-only queries for browsing.
 * All write operations go through BookingService.
 */
public class CustomerService {

    private final CityDAO    cityDAO    = new CityDAO();
    private final TheatreDAO theatreDAO = new TheatreDAO();
    private final ShowDAO    showDAO    = new ShowDAO();
    private final ScreenDAO  screenDAO  = new ScreenDAO();

    public List<City> getAllCities(Connection conn) throws SQLException {
        return cityDAO.findAll(conn);
    }

    public List<Theatre> getTheatresByCity(Connection conn, int cityId) throws SQLException {
        return theatreDAO.findByCity(conn, cityId);
    }

    public List<Show> getAvailableShowsByTheatre(Connection conn, int theatreId) throws SQLException {
        return showDAO.findAvailableByTheatre(conn, theatreId);
    }

    public Optional<Show> getShowById(Connection conn, int showId) throws SQLException {
        return showDAO.findById(conn, showId);
    }

    /** Returns all active seat layouts for the screen of the given show. */
    public List<ScreenLayout> getLayoutForShow(Connection conn, int screenId) throws SQLException {
        return screenDAO.findLayoutByScreen(conn, screenId);
    }

    /** Returns layout_ids of seats booked for this show (CONFIRMED only). */
    public List<Integer> getBookedSeatIds(Connection conn, int showId) throws SQLException {
        return screenDAO.findBookedSeatIds(conn, showId);
    }

    public List<SeatTypeZone> getZonesForScreen(Connection conn, int screenId) throws SQLException {
        return screenDAO.findZonesByScreen(conn, screenId);
    }

    /**
     * Resolves a seat code (e.g. "A3") to the layout_id.
     * Returns -1 if not found.
     */
    public int resolveSeatCode(List<ScreenLayout> layouts, String seatCode) {
        if (seatCode == null || seatCode.length() < 2) return -1;
        char row = Character.toUpperCase(seatCode.charAt(0));
        int col;
        try {
            col = Integer.parseInt(seatCode.substring(1).trim());
        } catch (NumberFormatException e) {
            return -1;
        }
        for (ScreenLayout sl : layouts) {
            if (sl.getRowLabel() == row && sl.getColNum() == col) return sl.getLayoutId();
        }
        return -1;
    }

    /**
     * Returns the price for a given seat layout from its zone.
     */
    public java.math.BigDecimal getPriceForSeat(List<SeatTypeZone> zones, ScreenLayout seat) {
        for (SeatTypeZone z : zones) {
            if (z.getZoneType() == seat.getSeatType()) return z.getPrice();
        }
        return java.math.BigDecimal.ZERO;
    }
}
