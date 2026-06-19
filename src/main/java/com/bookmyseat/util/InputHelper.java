package com.bookmyseat.util;

import java.util.Scanner;

/**
 * Thread-safe terminal input helper.
 * Wraps Scanner to handle invalid input gracefully.
 */
public class InputHelper {

    private static final Scanner scanner = new Scanner(System.in);

    private InputHelper() {}

    /**
     * Prompt the user and read a trimmed non-empty string.
     * Re-prompts on blank input.
     */
    public static String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) return line;
            System.out.println("  [!] Input cannot be blank.");
        }
    }

    /**
     * Prompt the user and read an optional string (blank allowed).
     */
    public static String readOptionalString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /**
     * Prompt the user and read a valid integer in [min, max].
     * Re-prompts on invalid input.
     */
    public static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(line);
                if (val >= min && val <= max) return val;
                System.out.println("  [!] Enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid input — please enter a number.");
            }
        }
    }

    /**
     * Read a password without echoing it (uses Console if available, else falls back to Scanner).
     */
    public static String readPassword(String prompt) {
        java.io.Console console = System.console();
        if (console != null) {
            char[] pwd = console.readPassword(prompt);
            return new String(pwd);
        }
        // Fallback for IDEs / exec:java (no Console)
        System.out.print(prompt + " [warning: input visible] ");
        return scanner.nextLine().trim();
    }

    /** Print a section divider. */
    public static void divider() {
        System.out.println("  ─────────────────────────────────────────────");
    }

    /** Print a blank line. */
    public static void blank() {
        System.out.println();
    }
}
