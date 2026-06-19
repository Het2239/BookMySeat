-- =============================================================
--  BookMySeat — db_objects.sql
--  Phase 7: Trigger + View + Additional Indexes
--
--  Load with:
--    mysql -u root -p bookmyseat < sql/db_objects.sql
--
--  Safe to re-run: uses DROP IF EXISTS before each object.
-- =============================================================

USE bookmyseat;

-- ─────────────────────────────────────────────────────────────
-- 1. TRIGGER: trg_check_housefull
--    Fires AFTER every INSERT on BookedSeats.
--    If all active seats for a show are now booked, the show
--    status is automatically flipped to 'HOUSEFULL'.
-- ─────────────────────────────────────────────────────────────

DROP TRIGGER IF EXISTS trg_check_housefull;

DELIMITER $$

CREATE TRIGGER trg_check_housefull
AFTER INSERT ON BookedSeats
FOR EACH ROW
BEGIN
    DECLARE booked_count INT DEFAULT 0;
    DECLARE total_seats  INT DEFAULT 0;

    -- Count seats booked for this show (only active seat positions)
    SELECT COUNT(*) INTO booked_count
    FROM BookedSeats bs
    JOIN ScreenLayouts sl ON bs.seat_id = sl.layout_id
    WHERE bs.show_id = NEW.show_id
      AND sl.is_active = 1;

    -- Count total active seats for this show's screen
    SELECT COUNT(*) INTO total_seats
    FROM ScreenLayouts sl
    JOIN Shows s ON sl.screen_id = s.screen_id
    WHERE s.show_id = NEW.show_id
      AND sl.is_active = 1;

    -- Flip to HOUSEFULL when last seat is taken
    IF booked_count >= total_seats THEN
        UPDATE Shows SET status = 'HOUSEFULL' WHERE show_id = NEW.show_id;
    END IF;
END $$

DELIMITER ;

SELECT 'Trigger trg_check_housefull created.' AS status;


-- ─────────────────────────────────────────────────────────────
-- 2. VIEW: v_show_availability
--    Read-only view used by:
--      • SP booking dashboard
--      • Customer browse (available_seats check)
--
--    NOTE: BookedSeats rows are DELETED on cancellation, so
--    the count is always for CONFIRMED bookings only.
-- ─────────────────────────────────────────────────────────────

DROP VIEW IF EXISTS v_show_availability;

CREATE VIEW v_show_availability AS
SELECT
    s.show_id,
    m.title                                               AS movie_title,
    s.show_datetime,
    s.base_price,
    s.status,
    sc.screen_id,
    sc.screen_name,
    t.theatre_id,
    t.name                                                AS theatre_name,
    c.city_id,
    c.city_name,
    COUNT(sl.layout_id)                                   AS total_seats,
    COUNT(bs.id)                                          AS booked_seats,
    COUNT(sl.layout_id) - COUNT(bs.id)                   AS available_seats
FROM Shows s
JOIN Movies        m  ON s.movie_id   = m.movie_id
JOIN Screens       sc ON s.screen_id  = sc.screen_id
JOIN Theatres      t  ON sc.theatre_id = t.theatre_id
JOIN Cities        c  ON t.city_id    = c.city_id
JOIN ScreenLayouts sl ON sl.screen_id = sc.screen_id  AND sl.is_active = 1
LEFT JOIN BookedSeats bs ON bs.show_id = s.show_id AND bs.seat_id = sl.layout_id
GROUP BY
    s.show_id, m.title, s.show_datetime, s.base_price, s.status,
    sc.screen_id, sc.screen_name, t.theatre_id, t.name,
    c.city_id, c.city_name;

SELECT 'View v_show_availability created.' AS status;


-- ─────────────────────────────────────────────────────────────
-- 3. ADDITIONAL INDEXES
--    Supplement those already in schema.sql.
-- ─────────────────────────────────────────────────────────────

-- Speed up booked-seat lookups during seat matrix rendering
ALTER TABLE BookedSeats
    ADD INDEX IF NOT EXISTS idx_bs_show_seat (show_id, seat_id);

-- Speed up payment refund lookups
ALTER TABLE Payments
    ADD INDEX IF NOT EXISTS idx_payment_booking_status (booking_id, status);

-- Composite index for show browse: theatre → datetime filter
ALTER TABLE Shows
    ADD INDEX IF NOT EXISTS idx_show_screen_status_dt (screen_id, status, show_datetime);

-- Speed up layout fetches by screen + active flag
ALTER TABLE ScreenLayouts
    ADD INDEX IF NOT EXISTS idx_layout_screen_active (screen_id, is_active);

SELECT 'Additional indexes created.' AS status;


-- ─────────────────────────────────────────────────────────────
-- 4. VERIFY
-- ─────────────────────────────────────────────────────────────

SHOW TRIGGERS LIKE 'trg_check_housefull';

SELECT table_name, view_definition IS NOT NULL AS view_exists
FROM information_schema.VIEWS
WHERE table_schema = 'bookmyseat' AND table_name = 'v_show_availability';

-- Quick sanity query on the view
SELECT show_id, movie_title, status, total_seats, booked_seats, available_seats
FROM v_show_availability
ORDER BY show_id;
