package com.bookmyseat.menu;

import com.bookmyseat.db.TransactionManager;
import com.bookmyseat.model.*;
import com.bookmyseat.model.enums.SeatType;
import com.bookmyseat.model.enums.ShowStatus;
import com.bookmyseat.service.SPService;
import com.bookmyseat.util.InputHelper;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service Provider Menu — Phase 4 full implementation.
 */
public class SPMenu {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DT_FMT   = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final User      sp;
    private final SPService spService = new SPService();

    public SPMenu(User sp) {
        this.sp = sp;
    }

    public void show(Connection conn) {
        boolean running = true;
        while (running) {
            printHeader();
            System.out.println("  1. View My Theatre & Screens");
            System.out.println("  2. Add New Screen with Layout");
            System.out.println("  3. Add Movie to Catalogue");
            System.out.println("  4. Schedule a Show");
            System.out.println("  5. View Booking Report");
            System.out.println("  6. Cancel a Show");
            System.out.println("  7. Register a Theatre");
            System.out.println("  0. Logout");
            InputHelper.divider();
            int choice = InputHelper.readInt("  Choose: ", 0, 7);
            InputHelper.blank();

            try {
                switch (choice) {
                    case 1 -> viewTheatre(conn);
                    case 2 -> addScreen(conn);
                    case 3 -> addMovie(conn);
                    case 4 -> scheduleShow(conn);
                    case 5 -> viewBookingReport(conn);
                    case 6 -> cancelShow(conn);
                    case 7 -> registerTheatre(conn);
                    case 0 -> {
                        System.out.println("  Goodbye, " + sp.getName() + "!");
                        running = false;
                    }
                }
            } catch (Exception e) {
                System.out.println("  ✘ Error: " + e.getMessage());
            }
            InputHelper.blank();
        }
    }

    // ── 1. View Theatre ───────────────────────────────────────

    private void viewTheatre(Connection conn) throws SQLException {
        List<Theatre> theatres = spService.getTheatresBySP(conn, sp.getUserId());
        if (theatres.isEmpty()) {
            System.out.println("  No theatres registered under your account.");
            System.out.println("  Use option 7 to register your first theatre.");
            return;
        }

        System.out.println("  ── Your Theatres ──");
        for (Theatre t : theatres) {
            System.out.println("  " + t);
            List<Screen> screens = spService.getScreensByTheatre(conn, t.getTheatreId());
            if (screens.isEmpty()) {
                System.out.println("      (no screens configured)");
            } else {
                for (Screen sc : screens) {
                    List<SeatTypeZone> zones = spService.getZonesByScreen(conn, sc.getScreenId());
                    System.out.println("      Screen: " + sc.getScreenName() +
                            "  (" + sc.getTotalRows() + " rows × " + sc.getTotalCols() + " cols)");
                    zones.forEach(z ->
                        System.out.println("        Zone: " + z));
                }
            }
        }
    }

    // ── 2. Add Screen with Layout ─────────────────────────────

    private void addScreen(Connection conn) throws SQLException {
        List<Theatre> theatres = spService.getTheatresBySP(conn, sp.getUserId());
        if (theatres.isEmpty()) {
            System.out.println("  No theatres found. Use option 7 to register a theatre first.");
            return;
        }

        System.out.println("  ── Select Theatre ──");
        theatres.forEach(t -> System.out.println("  [" + t.getTheatreId() + "] " + t.getName()));
        int theatreId = InputHelper.readInt("  Theatre ID: ",
                theatres.stream().mapToInt(Theatre::getTheatreId).min().orElse(1),
                theatres.stream().mapToInt(Theatre::getTheatreId).max().orElse(999));

        String screenName = InputHelper.readString("  Screen Name (e.g. Gold Audi): ");
        int totalCols = InputHelper.readInt("  Total columns per row (e.g. 10): ", 1, 30);
        int aisleAfterCol = InputHelper.readInt("  Aisle after column (0 = no aisle): ", 0, totalCols);

        System.out.println();
        System.out.println("  ── Define Seat Zones ──");
        System.out.println("  Zones divide rows into seat types (RECLINER, PREMIUM, REGULAR).");
        System.out.println("  Rows are labelled A, B, C … starting from the front.");
        System.out.println();

        List<SeatTypeZone> zones = new ArrayList<>();
        char nextRow = 'A';

        while (true) {
            System.out.println("  Zone " + (zones.size() + 1) + " starts at row " + nextRow + ".");
            System.out.println("  Type: 1=RECLINER  2=PREMIUM  3=REGULAR  0=Done");
            int typeChoice = InputHelper.readInt("  Zone type: ", 0, 3);
            if (typeChoice == 0) break;

            SeatType seatType = switch (typeChoice) {
                case 1 -> SeatType.RECLINER;
                case 2 -> SeatType.PREMIUM;
                default -> SeatType.REGULAR;
            };

            int numRows = InputHelper.readInt("  Number of rows for this zone: ", 1, 26 - (nextRow - 'A'));
            char rowEnd = (char) (nextRow + numRows - 1);

            String priceStr = InputHelper.readString("  Price per seat (₹): ");
            BigDecimal price;
            try {
                price = new BigDecimal(priceStr);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid price. Try again.");
                continue;
            }

            SeatTypeZone zone = new SeatTypeZone(0, 0, seatType, nextRow, rowEnd, price);
            zones.add(zone);
            nextRow = (char) (rowEnd + 1);
            System.out.println("  ✔ Zone added: " + zone);
            System.out.println();
        }

        if (zones.isEmpty()) {
            System.out.println("  No zones defined. Screen not created.");
            return;
        }

        System.out.println();
        System.out.println("  ── Summary ──");
        System.out.println("  Screen : " + screenName);
        System.out.println("  Cols   : " + totalCols);
        System.out.println("  Zones  : " + zones.size());
        zones.forEach(z -> System.out.println("    " + z));
        int totalSeats = zones.stream()
                .mapToInt(z -> (z.getRowEnd() - z.getRowStart() + 1) * totalCols)
                .sum();
        System.out.println("  Total seats: " + totalSeats);
        System.out.println();

        String confirm = InputHelper.readString("  Confirm? (y/n): ");
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("  Cancelled.");
            return;
        }

        try (TransactionManager tx = new TransactionManager()) {
            tx.begin();
            int screenId = spService.addScreenWithLayout(
                    tx.getConnection(), theatreId, screenName, zones, totalCols, aisleAfterCol);
            tx.commit();
            System.out.println("  ✔ Screen created! Screen ID: " + screenId +
                    "  (" + totalSeats + " seats)");
        }
    }

    // ── 3. Add Movie ──────────────────────────────────────────

    private void addMovie(Connection conn) throws SQLException {
        System.out.println("  ── Add Movie to Catalogue ──");

        String title    = InputHelper.readString("  Title       : ");
        String genre    = InputHelper.readString("  Genre       : ");
        int    duration = InputHelper.readInt("  Duration (mins): ", 1, 500);
        String language = InputHelper.readString("  Language    : ");

        System.out.println("  Rating: 1=U  2=UA  3=A  4=S");
        int ratingChoice = InputHelper.readInt("  Rating: ", 1, 4);
        String rating = switch (ratingChoice) {
            case 1 -> "U";
            case 2 -> "UA";
            case 3 -> "A";
            default -> "S";
        };

        LocalDate releaseDate = null;
        while (releaseDate == null) {
            String dateStr = InputHelper.readString("  Release Date (yyyy-MM-dd): ");
            try {
                releaseDate = LocalDate.parse(dateStr, DATE_FMT);
            } catch (DateTimeParseException e) {
                System.out.println("  [!] Invalid date format. Use yyyy-MM-dd");
            }
        }

        String desc = InputHelper.readOptionalString("  Description (optional): ");

        int movieId = spService.addMovie(conn, title, genre, duration,
                language, rating, releaseDate, desc.isBlank() ? "" : desc);
        System.out.println("  ✔ Movie added! Movie ID: " + movieId);
    }

    // ── 4. Schedule Show ──────────────────────────────────────

    private void scheduleShow(Connection conn) throws SQLException {
        System.out.println("  ── Schedule a Show ──");

        // Pick theatre → screen
        List<Theatre> theatres = spService.getTheatresBySP(conn, sp.getUserId());
        if (theatres.isEmpty()) { System.out.println("  No theatres found."); return; }

        theatres.forEach(t -> System.out.println("  [" + t.getTheatreId() + "] " + t.getName()));
        int theatreId = InputHelper.readInt("  Theatre ID: ",
                theatres.stream().mapToInt(Theatre::getTheatreId).min().orElse(1),
                theatres.stream().mapToInt(Theatre::getTheatreId).max().orElse(999));

        List<Screen> screens = spService.getScreensByTheatre(conn, theatreId);
        if (screens.isEmpty()) { System.out.println("  No screens for this theatre."); return; }

        System.out.println();
        screens.forEach(s -> System.out.println("  [" + s.getScreenId() + "] " + s.getScreenName()));
        int screenId = InputHelper.readInt("  Screen ID: ",
                screens.stream().mapToInt(Screen::getScreenId).min().orElse(1),
                screens.stream().mapToInt(Screen::getScreenId).max().orElse(999));

        // Pick movie
        System.out.println();
        List<Movie> movies = spService.getAllMovies(conn);
        if (movies.isEmpty()) { System.out.println("  No movies in catalogue. Add one first."); return; }

        movies.forEach(m -> System.out.println("  " + m));
        int movieId = InputHelper.readInt("  Movie ID: ",
                movies.stream().mapToInt(Movie::getMovieId).min().orElse(1),
                movies.stream().mapToInt(Movie::getMovieId).max().orElse(999));

        // Show date/time in IST
        System.out.println();
        LocalDateTime showDatetime = null;
        while (showDatetime == null) {
            String dtStr = InputHelper.readString("  Show Date & Time IST (yyyy-MM-dd HH:mm): ");
            try {
                showDatetime = LocalDateTime.parse(dtStr, DT_FMT);
            } catch (DateTimeParseException e) {
                System.out.println("  [!] Invalid format. Use: yyyy-MM-dd HH:mm");
            }
        }

        String priceStr = InputHelper.readString("  Base Price (₹): ");
        BigDecimal basePrice;
        try {
            basePrice = new BigDecimal(priceStr);
        } catch (NumberFormatException e) {
            System.out.println("  [!] Invalid price.");
            return;
        }

        int showId = spService.scheduleShow(conn, screenId, movieId, showDatetime, basePrice);
        System.out.println("  ✔ Show scheduled! Show ID: " + showId);
    }

    // ── 5. Booking Report ─────────────────────────────────────

    private void viewBookingReport(Connection conn) throws SQLException {
        List<Theatre> theatres = spService.getTheatresBySP(conn, sp.getUserId());
        if (theatres.isEmpty()) { System.out.println("  No theatres found."); return; }

        System.out.println("  ── Booking Report (Live) ──");
        System.out.println();

        for (Theatre t : theatres) {
            System.out.println("  Theatre: " + t.getName() + "  (" + t.getAddress() + ")");
            InputHelper.divider();

            List<ShowAvailability> report = spService.getAvailabilityReport(conn, t.getTheatreId());
            if (report.isEmpty()) {
                System.out.println("  (no shows scheduled)");
            } else {
                System.out.printf("  %-5s %-24s %-22s %-5s %-5s %-5s %-5s %s%n",
                        "ID", "Movie", "Time (IST)", "Tot", "Bkd", "Avl", "Occ", "Status");
                InputHelper.divider();
                String prevScreen = null;
                for (ShowAvailability sa : report) {
                    if (!sa.getScreenName().equals(prevScreen)) {
                        System.out.println("  ── Screen: " + sa.getScreenName() + " ──");
                        prevScreen = sa.getScreenName();
                    }
                    System.out.printf("  %-5d %-24s %-22s %-5d %-5d %-5d %-5s %s%n",
                            sa.getShowId(),
                            truncate(sa.getMovieTitle(), 23),
                            sa.getShowTimeIST(),
                            sa.getTotalSeats(),
                            sa.getBookedSeats(),
                            sa.getAvailableSeats(),
                            sa.getOccupancy(),
                            sa.getStatus());
                }
            }
            System.out.println();
        }
    }


    // ── 6. Cancel Show ────────────────────────────────────────

    private void cancelShow(Connection conn) throws SQLException {
        List<Theatre> theatres = spService.getTheatresBySP(conn, sp.getUserId());
        if (theatres.isEmpty()) { System.out.println("  No theatres found."); return; }

        System.out.println("  ── Available Shows ──");
        System.out.println();

        List<Show> allShows = new ArrayList<>();
        for (Theatre t : theatres) {
            List<Screen> screens = spService.getScreensByTheatre(conn, t.getTheatreId());
            for (Screen sc : screens) {
                List<Show> shows = spService.getShowsByScreen(conn, sc.getScreenId());
                shows.stream()
                     .filter(s -> s.getStatus() == ShowStatus.AVAILABLE ||
                                  s.getStatus() == ShowStatus.HOUSEFULL)
                     .forEach(s -> {
                         System.out.println("  [" + s.getShowId() + "] " + s);
                         allShows.add(s);
                     });
            }
        }

        if (allShows.isEmpty()) {
            System.out.println("  No active shows to cancel.");
            return;
        }

        int showId = InputHelper.readInt("  Show ID to cancel (0 to go back): ", 0,
                allShows.stream().mapToInt(Show::getShowId).max().orElse(0));
        if (showId == 0) return;

        boolean valid = allShows.stream().anyMatch(s -> s.getShowId() == showId);
        if (!valid) { System.out.println("  [!] Invalid Show ID."); return; }

        String confirm = InputHelper.readString("  Cancel Show #" + showId + "? (y/n): ");
        if (!confirm.equalsIgnoreCase("y")) { System.out.println("  Aborted."); return; }

        spService.cancelShow(conn, showId);
        System.out.println("  ✔ Show #" + showId + " cancelled.");
    }

    // ── 7. Register Theatre ───────────────────────────────────

    private void registerTheatre(Connection conn) throws SQLException {
        System.out.println("  ── Register a New Theatre ──");

        List<City> cities = spService.getAllCities(conn);
        if (cities.isEmpty()) { System.out.println("  No cities available."); return; }

        cities.forEach(c -> System.out.println("  [" + c.getCityId() + "] " + c.getCityName()));
        int cityId = InputHelper.readInt("  City ID: ",
                cities.stream().mapToInt(City::getCityId).min().orElse(1),
                cities.stream().mapToInt(City::getCityId).max().orElse(999));

        String name    = InputHelper.readString("  Theatre Name   : ");
        String address = InputHelper.readString("  Address        : ");

        String confirm = InputHelper.readString("  Register '" + name + "'? (y/n): ");
        if (!confirm.equalsIgnoreCase("y")) { System.out.println("  Cancelled."); return; }

        int theatreId = spService.addTheatre(conn, name, address, cityId, sp.getUserId());
        System.out.println("  ✔ Theatre registered! Theatre ID: " + theatreId);
        System.out.println("  Now use option 2 to add screens to your theatre.");
    }

    // ── Helpers ───────────────────────────────────────────────

    private void printHeader() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════╗");
        System.out.println("  ║       🏢  SERVICE PROVIDER PANEL         ║");
        System.out.println("  ║  Welcome, " + padRight(sp.getName(), 31) + "║");
        System.out.println("  ╚══════════════════════════════════════════╝");
        InputHelper.blank();
    }

    private static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s.length() > n ? s.substring(0, n - 1) + "…" : s);
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}
