package com.bookmyseat.menu;

import com.bookmyseat.db.TransactionManager;
import com.bookmyseat.exception.SeatUnavailableException;
import com.bookmyseat.model.*;
import com.bookmyseat.service.BookingService;
import com.bookmyseat.service.CustomerService;
import com.bookmyseat.util.InputHelper;
import com.bookmyseat.util.SeatMatrixRenderer;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Customer Menu — Phases 5 & 6 full implementation.
 *
 * Flow:
 *   Browse Shows → Seat Matrix → Book Seats → Confirmation
 *   My Bookings  → Cancel Booking (within 2-hour window)
 */
public class CustomerMenu {

    private final User            customer;
    private final CustomerService customerService = new CustomerService();
    private final BookingService  bookingService  = new BookingService();

    public CustomerMenu(User customer) {
        this.customer = customer;
    }

    public void show(Connection conn) {
        boolean running = true;
        while (running) {
            printHeader();
            System.out.println("  1. Browse Shows & Book");
            System.out.println("  2. My Bookings");
            System.out.println("  3. Cancel a Booking");
            System.out.println("  0. Logout");
            InputHelper.divider();
            int choice = InputHelper.readInt("  Choose: ", 0, 3);
            InputHelper.blank();

            try {
                switch (choice) {
                    case 1 -> browseAndBook(conn);
                    case 2 -> myBookings(conn);
                    case 3 -> cancelBooking(conn);
                    case 0 -> {
                        System.out.println("  Goodbye, " + customer.getName() + "!");
                        running = false;
                    }
                }
            } catch (Exception e) {
                System.out.println("  ✘ Error: " + e.getMessage());
            }
            InputHelper.blank();
        }
    }

    // ── 1. Browse Shows & Book ────────────────────────────────

    private void browseAndBook(Connection conn) throws SQLException, SeatUnavailableException {

        // Step A: Choose City
        List<City> cities = customerService.getAllCities(conn);
        if (cities.isEmpty()) { System.out.println("  No cities available."); return; }

        System.out.println("  ── Select City ──");
        cities.forEach(c -> System.out.println("  [" + c.getCityId() + "] " + c.getCityName()));
        int cityId = InputHelper.readInt("  City: ",
                cities.stream().mapToInt(City::getCityId).min().orElse(1),
                cities.stream().mapToInt(City::getCityId).max().orElse(999));
        InputHelper.blank();

        // Step B: Choose Theatre
        List<Theatre> theatres = customerService.getTheatresByCity(conn, cityId);
        if (theatres.isEmpty()) { System.out.println("  No theatres in this city."); return; }

        System.out.println("  ── Select Theatre ──");
        theatres.forEach(t -> System.out.println("  [" + t.getTheatreId() + "] " + t.getName()));
        int theatreId = InputHelper.readInt("  Theatre: ",
                theatres.stream().mapToInt(Theatre::getTheatreId).min().orElse(1),
                theatres.stream().mapToInt(Theatre::getTheatreId).max().orElse(999));
        InputHelper.blank();

        // Step C: Choose Show
        List<Show> shows = customerService.getAvailableShowsByTheatre(conn, theatreId);
        if (shows.isEmpty()) { System.out.println("  No available shows at this theatre."); return; }

        System.out.println("  ── Available Shows ──");
        System.out.printf("  %-5s %-28s %-22s %-12s%n", "ID", "Movie", "Time (IST)", "Screen");
        InputHelper.divider();
        shows.forEach(s -> System.out.printf("  %-5d %-28s %-22s %-12s%n",
                s.getShowId(), truncate(s.getMovieTitle(), 27),
                s.getShowTimeIST(), s.getScreenName()));
        InputHelper.blank();

        int showId = InputHelper.readInt("  Show ID (0 to go back): ", 0,
                shows.stream().mapToInt(Show::getShowId).max().orElse(0));
        if (showId == 0) return;

        Show selectedShow = shows.stream()
                .filter(s -> s.getShowId() == showId).findFirst().orElse(null);
        if (selectedShow == null) { System.out.println("  [!] Invalid Show ID."); return; }

        // Step D: Show Seat Matrix (re-fetched fresh)
        List<ScreenLayout> layouts = customerService.getLayoutForShow(conn, selectedShow.getScreenId());
        List<SeatTypeZone> zones   = customerService.getZonesForScreen(conn, selectedShow.getScreenId());
        List<Integer>      booked  = customerService.getBookedSeatIds(conn, showId);
        Set<Integer> bookedSet = new HashSet<>(booked);

        System.out.println("  Show  : " + selectedShow.getMovieTitle() +
                "  |  " + selectedShow.getShowTimeIST() +
                "  |  " + selectedShow.getScreenName());
        SeatMatrixRenderer.render(layouts, zones, bookedSet);

        // Step E: Select Seats
        System.out.println("  Enter seat codes separated by commas (e.g. A1,A2,B4)");
        System.out.println("  or 0 to go back.");
        String input = InputHelper.readString("  Seats: ").trim();
        if (input.equals("0")) return;

        String[] codes = input.split(",");
        List<Integer> selectedIds = new ArrayList<>();
        List<String>  selectedCodes = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (String code : codes) {
            String trimmed = code.trim().toUpperCase();
            int layoutId = customerService.resolveSeatCode(layouts, trimmed);
            if (layoutId == -1) {
                System.out.println("  [!] Unknown seat: " + trimmed + ". Try again.");
                return;
            }
            if (bookedSet.contains(layoutId)) {
                System.out.println("  [!] Seat " + trimmed + " is already booked.");
                return;
            }
            if (selectedIds.contains(layoutId)) {
                System.out.println("  [!] Duplicate seat: " + trimmed);
                return;
            }
            // Find layout and compute price
            ScreenLayout sl = layouts.stream()
                    .filter(l -> l.getLayoutId() == layoutId).findFirst().orElseThrow();
            BigDecimal price = customerService.getPriceForSeat(zones, sl);
            total = total.add(price);
            selectedIds.add(layoutId);
            selectedCodes.add(trimmed);
        }

        // Step F: Confirm
        InputHelper.blank();
        System.out.println("  ── Booking Summary ──");
        System.out.println("  Movie  : " + selectedShow.getMovieTitle());
        System.out.println("  Show   : " + selectedShow.getShowTimeIST());
        System.out.println("  Seats  : " + String.join(", ", selectedCodes));
        System.out.println("  Total  : ₹" + total);
        InputHelper.blank();

        String confirm = InputHelper.readString("  Confirm booking? (y/n): ");
        if (!confirm.equalsIgnoreCase("y")) { System.out.println("  Booking cancelled."); return; }

        // Step G: ACID Transaction — BEGIN → lock → insert → commit
        try (TransactionManager tx = new TransactionManager()) {
            tx.begin(); // SERIALIZABLE isolation

            int bookingId = bookingService.bookSeats(
                    tx.getConnection(), customer.getUserId(), showId, selectedIds, total);
            tx.commit();

            System.out.println();
            System.out.println("  ╔══════════════════════════════════════════╗");
            System.out.println("  ║        ✅  BOOKING CONFIRMED!            ║");
            System.out.println("  ║  Booking ID : #" + padRight(String.valueOf(bookingId), 26) + "║");
            System.out.println("  ║  Seats      : " + padRight(String.join(", ", selectedCodes), 26) + "║");
            System.out.println("  ║  Amount Paid: ₹" + padRight(total.toPlainString(), 25) + "║");
            System.out.println("  ╚══════════════════════════════════════════╝");

        } catch (SeatUnavailableException e) {
            System.out.println("  ✘ " + e.getMessage() + " Please pick another seat.");
        }
    }

    // ── 2. My Bookings ────────────────────────────────────────

    private void myBookings(Connection conn) throws SQLException {
        List<Booking> bookings = bookingService.getMyBookings(conn, customer.getUserId());
        if (bookings.isEmpty()) {
            System.out.println("  You have no bookings yet.");
            return;
        }

        System.out.println("  ── My Bookings ──");
        System.out.println();
        for (Booking b : bookings) {
            List<BookedSeat> seats = bookingService.getBookedSeats(conn, b.getBookingId());
            String seatCodes = seats.stream()
                    .map(BookedSeat::getSeatCode)
                    .collect(Collectors.joining(", "));
            System.out.printf("  Booking #%-5d  Show:%-4d  Seats:%-12s  ₹%-8s  %s%n",
                    b.getBookingId(), b.getShowId(), seatCodes,
                    b.getTotalAmount().toPlainString(), b.getStatus());
        }
    }

    // ── 3. Cancel Booking ─────────────────────────────────────

    private void cancelBooking(Connection conn) throws SQLException {
        List<Booking> bookings = bookingService.getMyBookings(conn, customer.getUserId());
        List<Booking> confirmed = bookings.stream()
                .filter(b -> b.getStatus().name().equals("CONFIRMED"))
                .collect(Collectors.toList());

        if (confirmed.isEmpty()) {
            System.out.println("  No confirmed bookings to cancel.");
            return;
        }

        System.out.println("  ── Confirmed Bookings ──");
        confirmed.forEach(b -> System.out.printf(
                "  Booking #%-5d  Show:%-4d  ₹%s  Booked at: %s%n",
                b.getBookingId(), b.getShowId(),
                b.getTotalAmount().toPlainString(), b.getBookedAt()));

        int bookingId = InputHelper.readInt("  Booking ID to cancel (0 = back): ", 0,
                confirmed.stream().mapToInt(Booking::getBookingId).max().orElse(0));
        if (bookingId == 0) return;

        Booking target = confirmed.stream()
                .filter(b -> b.getBookingId() == bookingId).findFirst().orElse(null);
        if (target == null) { System.out.println("  [!] Invalid Booking ID."); return; }

        // 2-hour cancellation window check
        // Need the show_datetime — fetch via ShowDAO inside the connection
        CustomerService cs = new CustomerService();
        Optional<Show> showOpt = cs.getShowById(conn, target.getShowId());
        if (showOpt.isEmpty()) { System.out.println("  [!] Show not found."); return; }

        Show show = showOpt.get();
        // show_datetime is in UTC; compare with now in UTC
        ZonedDateTime showTimeUTC = show.getShowDatetime().atZone(ZoneId.of("UTC"));
        ZonedDateTime nowUTC = LocalDateTime.now().atZone(ZoneId.of("Asia/Kolkata"))
                .withZoneSameInstant(ZoneId.of("UTC"));

        long hoursUntilShow = java.time.Duration.between(nowUTC, showTimeUTC).toHours();
        if (hoursUntilShow < 2) {
            System.out.println("  ✘ Cannot cancel — show starts in less than 2 hours.");
            return;
        }

        String confirm = InputHelper.readString("  Cancel Booking #" + bookingId + "? (y/n): ");
        if (!confirm.equalsIgnoreCase("y")) { System.out.println("  Aborted."); return; }

        try (TransactionManager tx = new TransactionManager()) {
            tx.begin();
            bookingService.cancelBooking(tx.getConnection(), bookingId);
            tx.commit();
            System.out.println("  ✔ Booking #" + bookingId + " cancelled. Refund initiated.");
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    private void printHeader() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════╗");
        System.out.println("  ║         🎬  CUSTOMER DASHBOARD           ║");
        System.out.println("  ║  Welcome, " + padRight(customer.getName(), 31) + "║");
        System.out.println("  ╚══════════════════════════════════════════╝");
        InputHelper.blank();
    }

    private static String padRight(String s, int n) {
        if (s == null) s = "";
        return String.format("%-" + n + "s", s.length() > n ? s.substring(0, n - 1) + "…" : s);
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}

