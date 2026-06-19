package com.bookmyseat.service;

import com.bookmyseat.dao.CityDAO;
import com.bookmyseat.dao.MovieDAO;
import com.bookmyseat.dao.ScreenDAO;
import com.bookmyseat.dao.ShowDAO;
import com.bookmyseat.dao.TheatreDAO;
import com.bookmyseat.model.*;
import com.bookmyseat.model.enums.ShowStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SPService — business logic for Service Provider operations.
 */
public class SPService {

    private final CityDAO    cityDAO    = new CityDAO();
    private final TheatreDAO theatreDAO = new TheatreDAO();
    private final ScreenDAO  screenDAO  = new ScreenDAO();
    private final MovieDAO   movieDAO   = new MovieDAO();
    private final ShowDAO    showDAO    = new ShowDAO();

    // ── City ────────────────────────────────────────────

    public List<City> getAllCities(Connection conn) throws SQLException {
        return cityDAO.findAll(conn);
    }

    // ── Theatre ──────────────────────────────────────────────

    public List<Theatre> getTheatresBySP(Connection conn, int spUserId) throws SQLException {
        return theatreDAO.findBySP(conn, spUserId);
    }

    public Theatre getTheatreById(Connection conn, int theatreId) throws SQLException {
        return theatreDAO.findById(conn, theatreId)
                .orElseThrow(() -> new IllegalArgumentException("Theatre not found: " + theatreId));
    }

    public int addTheatre(Connection conn, String name, String address,
                          int cityId, int spUserId) throws SQLException {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Theatre name cannot be blank.");
        Theatre t = new Theatre(0, name.trim(), address.trim(), cityId, spUserId);
        return theatreDAO.save(conn, t);
    }

    // ── Screen & Layout ───────────────────────────────────────

    public List<Screen> getScreensByTheatre(Connection conn, int theatreId) throws SQLException {
        return screenDAO.findByTheatre(conn, theatreId);
    }

    public List<ScreenLayout> getLayoutByScreen(Connection conn, int screenId) throws SQLException {
        return screenDAO.findLayoutByScreen(conn, screenId);
    }

    public List<SeatTypeZone> getZonesByScreen(Connection conn, int screenId) throws SQLException {
        return screenDAO.findZonesByScreen(conn, screenId);
    }

    /**
     * Add a new screen with zones and auto-generated seat layout.
     *
     * @param conn          transaction connection
     * @param theatreId     parent theatre
     * @param screenName    name of the new screen
     * @param zones         zone definitions (screenId will be set after insert)
     * @param totalCols     number of columns per row
     * @param aisleAfterCol column after which an aisle gap is marked (0 = no aisle)
     * @return the new screen_id
     */
    public int addScreenWithLayout(Connection conn, int theatreId, String screenName,
                                   List<SeatTypeZone> zones, int totalCols, int aisleAfterCol)
            throws SQLException {

        // Compute total rows from zone definitions
        char maxRow = 'A';
        for (SeatTypeZone z : zones) {
            if (z.getRowEnd() > maxRow) maxRow = z.getRowEnd();
        }
        int totalRows = maxRow - 'A' + 1;

        // Insert Screen record
        Screen screen = new Screen(0, theatreId, screenName, totalRows, totalCols);
        int screenId = screenDAO.saveScreen(conn, screen);

        // Set screenId on zones and persist them
        zones.forEach(z -> z.setScreenId(screenId));
        screenDAO.saveZones(conn, zones);

        // Auto-generate individual seat layout from zone definitions
        List<ScreenLayout> layouts = new ArrayList<>();
        for (SeatTypeZone zone : zones) {
            for (char row = zone.getRowStart(); row <= zone.getRowEnd(); row++) {
                for (int col = 1; col <= totalCols; col++) {
                    boolean isAisle = (aisleAfterCol > 0 && col == aisleAfterCol);
                    layouts.add(new ScreenLayout(0, screenId, row, col,
                                                 zone.getZoneType(), isAisle, true));
                }
            }
        }
        screenDAO.saveLayouts(conn, layouts);

        return screenId;
    }

    // ── Movie ─────────────────────────────────────────────────

    public List<Movie> getAllMovies(Connection conn) throws SQLException {
        return movieDAO.findAll(conn);
    }

    public int addMovie(Connection conn, String title, String genre, int durationMins,
                        String language, String rating, LocalDate releaseDate, String description)
            throws SQLException {
        if (title == null || title.isBlank())
            throw new IllegalArgumentException("Movie title cannot be blank.");
        if (durationMins <= 0)
            throw new IllegalArgumentException("Duration must be > 0 minutes.");

        Movie m = new Movie();
        m.setTitle(title.trim());
        m.setGenre(genre.trim());
        m.setDurationMins(durationMins);
        m.setLanguage(language.trim());
        m.setRating(rating.trim().toUpperCase());
        m.setReleaseDate(releaseDate);
        m.setDescription(description.trim());
        return movieDAO.save(conn, m);
    }

    // ── Show ──────────────────────────────────────────────────

    public List<Show> getShowsByScreen(Connection conn, int screenId) throws SQLException {
        return showDAO.findByScreen(conn, screenId);
    }

    /**
     * Queries v_show_availability for rich SP booking report.
     * Requires db_objects.sql to be loaded in MySQL first.
     */
    public List<ShowAvailability> getAvailabilityReport(Connection conn, int theatreId)
            throws SQLException {
        return showDAO.findAvailabilityByTheatre(conn, theatreId);
    }

    /**
     * Schedule a show. Datetime is accepted in IST and converted to UTC for storage.
     */
    public int scheduleShow(Connection conn, int screenId, int movieId,
                            LocalDateTime showDatetimeIST, BigDecimal basePrice)
            throws SQLException {
        if (basePrice.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Base price must be positive.");

        // Convert IST → UTC
        ZonedDateTime ist = showDatetimeIST.atZone(ZoneId.of("Asia/Kolkata"));
        ZonedDateTime utc = ist.withZoneSameInstant(ZoneId.of("UTC"));
        LocalDateTime showDatetimeUTC = utc.toLocalDateTime();

        Show show = new Show();
        show.setScreenId(screenId);
        show.setMovieId(movieId);
        show.setShowDatetime(showDatetimeUTC);
        show.setBasePrice(basePrice);
        show.setStatus(ShowStatus.AVAILABLE);
        return showDAO.save(conn, show);
    }

    public void cancelShow(Connection conn, int showId) throws SQLException {
        showDAO.updateStatus(conn, showId, ShowStatus.CANCELLED);
    }
}
