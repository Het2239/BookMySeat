-- =============================================================
--  BookMySeat — stored_procedure.sql
--  Phase 8: sp_book_seats stored procedure
--
--  This procedure mirrors the Java BookingService.bookSeats()
--  logic entirely inside MySQL — useful for direct DB testing
--  and serves as the atomic fallback if the Java layer is bypassed.
--
--  Load with:
--    mysql -u root -p bookmyseat < sql/stored_procedure.sql
-- =============================================================

USE bookmyseat;

DROP PROCEDURE IF EXISTS sp_book_seats;

DELIMITER $$

CREATE PROCEDURE sp_book_seats(
    IN  p_customer_id  INT,
    IN  p_show_id      INT,
    IN  p_seat_ids     TEXT,          -- comma-separated layout_ids e.g. '12,13,15'
    IN  p_total_amount DECIMAL(10,2),
    OUT p_booking_id   INT,
    OUT p_error_msg    VARCHAR(255)
)
sp_main: BEGIN
    DECLARE v_booked_count INT DEFAULT 0;
    DECLARE v_seat_id      INT;
    DECLARE v_pos          INT;
    DECLARE v_remaining    TEXT;
    DECLARE v_seat_code    VARCHAR(10);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_booking_id = -1;
        SET p_error_msg  = 'Transaction rolled back due to unexpected SQL error.';
    END;

    SET p_booking_id = -1;
    SET p_error_msg  = '';

    -- ── Step 1: Validate show status ──────────────────────────
    IF (SELECT COUNT(*) FROM Shows
        WHERE show_id = p_show_id AND status IN ('AVAILABLE', 'HOUSEFULL')) = 0 THEN
        SET p_error_msg = 'Show is not available for booking.';
        LEAVE sp_main;
    END IF;

    -- ── Step 2: Parse seat IDs and check availability ─────────
    -- Uses a temporary table to hold the requested seat IDs
    DROP TEMPORARY TABLE IF EXISTS tmp_requested_seats;
    CREATE TEMPORARY TABLE tmp_requested_seats (seat_id INT NOT NULL);

    SET v_remaining = CONCAT(p_seat_ids, ',');
    WHILE LOCATE(',', v_remaining) > 0 DO
        SET v_pos      = LOCATE(',', v_remaining);
        SET v_seat_id  = CAST(TRIM(SUBSTRING(v_remaining, 1, v_pos - 1)) AS UNSIGNED);
        SET v_remaining = SUBSTRING(v_remaining, v_pos + 1);
        IF v_seat_id > 0 THEN
            INSERT INTO tmp_requested_seats VALUES (v_seat_id);
        END IF;
    END WHILE;

    -- Check if any of the requested seats are already booked
    SELECT COUNT(*) INTO v_booked_count
    FROM BookedSeats bs
    WHERE bs.show_id = p_show_id
      AND bs.seat_id IN (SELECT seat_id FROM tmp_requested_seats);

    IF v_booked_count > 0 THEN
        -- Find the first conflicting seat code for the error message
        SELECT CONCAT(sl.row_label, sl.col_num) INTO v_seat_code
        FROM BookedSeats bs
        JOIN ScreenLayouts sl ON bs.seat_id = sl.layout_id
        WHERE bs.show_id = p_show_id
          AND bs.seat_id IN (SELECT seat_id FROM tmp_requested_seats)
        LIMIT 1;

        SET p_error_msg = CONCAT('Seat ', v_seat_code, ' is no longer available.');
        DROP TEMPORARY TABLE IF EXISTS tmp_requested_seats;
        LEAVE sp_main;
    END IF;

    -- ── Step 3: ACID transaction ───────────────────────────────
    START TRANSACTION;

    -- Insert booking
    INSERT INTO Bookings (customer_id, show_id, total_amount, status)
    VALUES (p_customer_id, p_show_id, p_total_amount, 'CONFIRMED');

    SET p_booking_id = LAST_INSERT_ID();

    -- Insert booked seats (batch from temp table)
    INSERT INTO BookedSeats (booking_id, show_id, seat_id)
    SELECT p_booking_id, p_show_id, seat_id
    FROM tmp_requested_seats;

    -- Insert payment record
    INSERT INTO Payments (booking_id, amount, status)
    VALUES (p_booking_id, p_total_amount, 'SUCCESS');

    COMMIT;

    -- Trigger trg_check_housefull fires automatically on BookedSeats insert above.

    DROP TEMPORARY TABLE IF EXISTS tmp_requested_seats;
    SET p_error_msg = 'OK';

END $$

DELIMITER ;

-- ── Verify ────────────────────────────────────────────────────
SHOW PROCEDURE STATUS WHERE Db = 'bookmyseat' AND Name = 'sp_book_seats';

SELECT 'sp_book_seats procedure created. Test with:' AS info;
SELECT 'CALL sp_book_seats(1, 1, "1,2,3", 600.00, @bid, @err); SELECT @bid, @err;' AS example_call;
