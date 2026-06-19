package com.bookmyseat.menu;

import com.bookmyseat.model.User;
import com.bookmyseat.model.enums.Role;
import com.bookmyseat.service.AuthService;
import com.bookmyseat.util.InputHelper;
import com.bookmyseat.db.DBConnection;

import java.sql.Connection;
import java.util.Optional;

/**
 * MainMenu — application entry point.
 *
 * Flow:
 *   Welcome screen → Register | Login | Exit
 *   On login: detect role → CustomerMenu | SPMenu
 */
public class MainMenu {

    private final AuthService authService = new AuthService();

    public void start() {
        printBanner();

        boolean running = true;
        while (running) {
            System.out.println("  1.  Register");
            System.out.println("  2.  Login");
            System.out.println("  0.  Exit");
            InputHelper.divider();

            int choice = InputHelper.readInt("  Choose: ", 0, 2);
            InputHelper.blank();

            switch (choice) {
                case 1 -> handleRegister();
                case 2 -> handleLogin();
                case 0 -> {
                    System.out.println("  Thank you for using BookMySeat. Goodbye!");
                    InputHelper.blank();
                    running = false;
                }
            }
        }
    }

    // ── Register ─────────────────────────────────────────────

    private void handleRegister() {
        System.out.println("  ── New Account Registration ──");
        InputHelper.blank();

        String name  = InputHelper.readString("  Full Name   : ");
        String email = InputHelper.readString("  Email       : ");
        String pwd   = InputHelper.readPassword("  Password   : ");
        String phone = InputHelper.readOptionalString("  Phone (optional): ");

        System.out.println();
        System.out.println("  Register as:");
        System.out.println("  1. Customer");
        System.out.println("  2. Service Provider (Theatre Owner)");
        int roleChoice = InputHelper.readInt("  Choose: ", 1, 2);
        Role role = (roleChoice == 1) ? Role.CUSTOMER : Role.SERVICE_PROVIDER;

        InputHelper.blank();

        try (Connection conn = DBConnection.getInstance().getConnection()) {
            int userId = authService.register(conn, name, email, pwd,
                                              phone.isBlank() ? null : phone, role);
            System.out.println("  ✔ Account created! Your User ID: " + userId);
            System.out.println("  You can now log in with your email and password.");
        } catch (IllegalArgumentException e) {
            System.out.println("  ✘ Registration failed: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  ✘ Unexpected error: " + e.getMessage());
        }
        InputHelper.blank();
    }

    // ── Login ─────────────────────────────────────────────────

    private void handleLogin() {
        System.out.println("  ── Login ──");
        InputHelper.blank();

        String email = InputHelper.readString("  Email   : ");
        String pwd   = InputHelper.readPassword("  Password: ");
        InputHelper.blank();

        try (Connection conn = DBConnection.getInstance().getConnection()) {
            Optional<User> result = authService.login(conn, email, pwd);

            if (result.isEmpty()) {
                System.out.println("  ✘ Invalid email or password. Please try again.");
                InputHelper.blank();
                return;
            }

            User user = result.get();
            System.out.println("  ✔ Login successful! Welcome back, " + user.getName() + ".");
            InputHelper.blank();

            // Route based on role
            if (user.getRole() == Role.CUSTOMER) {
                new CustomerMenu(user).show(conn);
            } else {
                new SPMenu(user).show(conn);
            }

        } catch (Exception e) {
            System.out.println("  ✘ Unexpected error: " + e.getMessage());
        }
        InputHelper.blank();
    }

    // ── Banner ────────────────────────────────────────────────

    private void printBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════╗");
        System.out.println("  ║                                              ║");
        System.out.println("  ║    🎟  Welcome to  B O O K M Y S E A T      ║");
        System.out.println("  ║         Your Movie. Your Seat. Done.         ║");
        System.out.println("  ║                                              ║");
        System.out.println("  ╚══════════════════════════════════════════════╝");
        System.out.println();
        InputHelper.divider();
        InputHelper.blank();
    }
}
