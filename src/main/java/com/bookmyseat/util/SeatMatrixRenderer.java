package com.bookmyseat.util;

import com.bookmyseat.model.ScreenLayout;
import com.bookmyseat.model.SeatTypeZone;
import com.bookmyseat.model.enums.SeatType;

import java.util.*;

/**
 * Renders an ASCII seat matrix in the terminal.
 *
 * Output example:
 *
 *   Legend: [RC]=Recliner ₹360  [PP]=Premium ₹260  [__]=Regular ₹200  [XX]=Booked
 *
 *        1    2    3    4    5    |    6    7    8    9   10
 *   A  [RC] [RC] [XX] [RC] [RC]  |  [RC] [RC] [RC] [XX] [RC]
 *   B  [PP] [PP] [PP] [PP] [PP]  |  [PP] [PP] [PP] [PP] [PP]
 *   ─────────────────────────────────────────────────────────
 *   H  [__] [__] [XX] [__] [__]  |  [__] [__] [__] [__] [__]
 *
 * KEY RULE: Re-fetch layout and booked seats on every call — never cache availability.
 */
public class SeatMatrixRenderer {

    private SeatMatrixRenderer() {}

    /**
     * Print the full seat matrix.
     *
     * @param layouts       all active ScreenLayout rows for this screen (sorted row asc, col asc)
     * @param zones         zone definitions (for price legend)
     * @param bookedSeatIds set of layout_ids already booked for this show
     */
    public static void render(List<ScreenLayout> layouts,
                               List<SeatTypeZone> zones,
                               Set<Integer> bookedSeatIds) {

        if (layouts.isEmpty()) {
            System.out.println("  (No seat layout configured for this screen.)");
            return;
        }

        // Build price map: SeatType → price string
        Map<SeatType, String> priceMap = new LinkedHashMap<>();
        for (SeatTypeZone z : zones) {
            priceMap.put(z.getZoneType(), "₹" + z.getPrice().stripTrailingZeros().toPlainString());
        }

        // Print legend
        System.out.println();
        System.out.print("  Legend: ");
        System.out.print("[RC]=Recliner " + priceMap.getOrDefault(SeatType.RECLINER, "") + "  ");
        System.out.print("[PP]=Premium "  + priceMap.getOrDefault(SeatType.PREMIUM,  "") + "  ");
        System.out.print("[__]=Regular "  + priceMap.getOrDefault(SeatType.REGULAR,  "") + "  ");
        System.out.println("[XX]=Booked");
        System.out.println();

        // Group by row
        Map<Character, List<ScreenLayout>> byRow = new LinkedHashMap<>();
        for (ScreenLayout sl : layouts) {
            byRow.computeIfAbsent(sl.getRowLabel(), k -> new ArrayList<>()).add(sl);
        }

        // Find max col for header
        int maxCol = layouts.stream().mapToInt(ScreenLayout::getColNum).max().orElse(10);

        // Find aisle-after column (first seat marked is_aisle in first row)
        int aisleAfterCol = 0;
        if (!layouts.isEmpty()) {
            for (ScreenLayout sl : layouts) {
                if (sl.isAisle()) { aisleAfterCol = sl.getColNum(); break; }
            }
        }

        // Print column header
        System.out.print("       ");
        for (int col = 1; col <= maxCol; col++) {
            if (col == aisleAfterCol + 1 && aisleAfterCol > 0) System.out.print("   |");
            System.out.printf(" %3d ", col);
        }
        System.out.println();

        // Previous zone type — for zone separator line
        SeatType prevType = null;

        for (Map.Entry<Character, List<ScreenLayout>> entry : byRow.entrySet()) {
            char row = entry.getKey();
            List<ScreenLayout> seats = entry.getValue();

            // Zone separator between different seat types
            SeatType currentType = seats.get(0).getSeatType();
            if (prevType != null && !currentType.equals(prevType)) {
                System.out.println("  " + "─".repeat(6 + maxCol * 5 + (aisleAfterCol > 0 ? 4 : 0)));
            }
            prevType = currentType;

            // Print row
            System.out.printf("  %c  ", row);
            for (ScreenLayout seat : seats) {
                if (seat.getColNum() == aisleAfterCol + 1 && aisleAfterCol > 0) {
                    System.out.print("  |");
                }
                String symbol = bookedSeatIds.contains(seat.getLayoutId())
                        ? "[XX]"
                        : switch (seat.getSeatType()) {
                            case RECLINER -> "[RC]";
                            case PREMIUM  -> "[PP]";
                            case REGULAR  -> "[__]";
                        };
                System.out.print(" " + symbol);
            }
            System.out.println();
        }
        System.out.println();

        // Print screen (front indicator)
        int width = 6 + maxCol * 5 + (aisleAfterCol > 0 ? 4 : 0);
        System.out.println("  " + "▄".repeat(Math.min(width, 50)));
        System.out.println("                  🎬  S C R E E N");
        System.out.println();
    }
}
