-- =============================================================
--  BookMySeat — schema.sql
--  MySQL 8.x · ENGINE=InnoDB · utf8mb4
--  Run this AFTER: CREATE DATABASE bookmyseat ... (see setup.sql)
-- =============================================================

USE bookmyseat;

-- -------------------------------------------------------------
-- 1. Cities
-- -------------------------------------------------------------
CREATE TABLE Cities (
    city_id   INT          NOT NULL AUTO_INCREMENT,
    city_name VARCHAR(100) NOT NULL,

    PRIMARY KEY (city_id),
    UNIQUE KEY uq_city_name (city_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Master list of cities where theatres operate';


-- -------------------------------------------------------------
-- 2. Users  (both Customers and Service Providers)
-- -------------------------------------------------------------
CREATE TABLE Users (
    user_id       INT          NOT NULL AUTO_INCREMENT,
    name          VARCHAR(120) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt hash (cost ≥ 10)',
    phone         VARCHAR(15)  DEFAULT NULL,
    role          ENUM('CUSTOMER','SERVICE_PROVIDER') NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id),
    UNIQUE KEY uq_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Unified auth table; role determines menu routing';


-- -------------------------------------------------------------
-- 3. Theatres  (owned by a Service Provider)
-- -------------------------------------------------------------
CREATE TABLE Theatres (
    theatre_id INT          NOT NULL AUTO_INCREMENT,
    name       VARCHAR(150) NOT NULL,
    address    VARCHAR(255) NOT NULL,
    city_id    INT          NOT NULL,
    sp_user_id INT          NOT NULL COMMENT 'Owner SP user',

    PRIMARY KEY (theatre_id),
    CONSTRAINT fk_theatre_city  FOREIGN KEY (city_id)    REFERENCES Cities(city_id)  ON UPDATE CASCADE,
    CONSTRAINT fk_theatre_sp    FOREIGN KEY (sp_user_id) REFERENCES Users(user_id)   ON UPDATE CASCADE,
    INDEX idx_theatre_city   (city_id),
    INDEX idx_theatre_sp     (sp_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Cinema theatres; each belongs to one SP';


-- -------------------------------------------------------------
-- 4. Screens  (inside a Theatre)
-- -------------------------------------------------------------
CREATE TABLE Screens (
    screen_id   INT         NOT NULL AUTO_INCREMENT,
    theatre_id  INT         NOT NULL,
    screen_name VARCHAR(80) NOT NULL,
    total_rows  INT         NOT NULL COMMENT 'Number of seat rows (A, B, C …)',
    total_cols  INT         NOT NULL COMMENT 'Number of seat columns per row',

    PRIMARY KEY (screen_id),
    CONSTRAINT fk_screen_theatre FOREIGN KEY (theatre_id) REFERENCES Theatres(theatre_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX idx_screen_theatre (theatre_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Individual auditoriums within a theatre';


-- -------------------------------------------------------------
-- 5. SeatTypeZones  (zone definitions per screen)
--    Maps a contiguous row range → seat type + override price
-- -------------------------------------------------------------
CREATE TABLE SeatTypeZones (
    zone_id   INT NOT NULL AUTO_INCREMENT,
    screen_id INT NOT NULL,
    zone_type ENUM('RECLINER','PREMIUM','REGULAR') NOT NULL,
    row_start CHAR(1) NOT NULL COMMENT 'First row label, e.g. A',
    row_end   CHAR(1) NOT NULL COMMENT 'Last  row label, e.g. C',
    price     DECIMAL(8,2) NOT NULL,

    PRIMARY KEY (zone_id),
    CONSTRAINT fk_zone_screen FOREIGN KEY (screen_id) REFERENCES Screens(screen_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX idx_zone_screen (screen_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Defines RECLINER/PREMIUM/REGULAR zones and prices per screen';


-- -------------------------------------------------------------
-- 6. ScreenLayouts  (one row per physical seat)
-- -------------------------------------------------------------
CREATE TABLE ScreenLayouts (
    layout_id INT  NOT NULL AUTO_INCREMENT,
    screen_id INT  NOT NULL,
    row_label CHAR(1)  NOT NULL COMMENT 'Row letter: A, B, C …',
    col_num   TINYINT  NOT NULL COMMENT 'Column number: 1 … N',
    seat_type ENUM('RECLINER','PREMIUM','REGULAR') NOT NULL,
    is_aisle  TINYINT(1) NOT NULL DEFAULT 0 COMMENT '1 = aisle gap after this seat',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '0 = seat removed/broken',

    PRIMARY KEY (layout_id),
    CONSTRAINT fk_layout_screen FOREIGN KEY (screen_id) REFERENCES Screens(screen_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    -- Enforce unique seat position per screen
    UNIQUE KEY uq_seat_pos (screen_id, row_label, col_num),
    INDEX idx_layout_screen (screen_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Individual seat rows/cols; drives the seat matrix renderer';


-- -------------------------------------------------------------
-- 7. Movies
-- -------------------------------------------------------------
CREATE TABLE Movies (
    movie_id      INT          NOT NULL AUTO_INCREMENT,
    title         VARCHAR(200) NOT NULL,
    genre         VARCHAR(80)  NOT NULL,
    duration_mins SMALLINT     NOT NULL,
    language      VARCHAR(50)  NOT NULL DEFAULT 'Hindi',
    rating        ENUM('U','UA','A','S') NOT NULL DEFAULT 'UA',
    release_date  DATE         NOT NULL,
    description   TEXT         DEFAULT NULL,

    PRIMARY KEY (movie_id),
    INDEX idx_movie_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Movie catalogue';


-- -------------------------------------------------------------
-- 8. Shows  (a movie scheduled on a screen at a datetime)
-- -------------------------------------------------------------
CREATE TABLE Shows (
    show_id       INT            NOT NULL AUTO_INCREMENT,
    screen_id     INT            NOT NULL,
    movie_id      INT            NOT NULL,
    show_datetime DATETIME       NOT NULL COMMENT 'Stored in UTC; convert to IST for display',
    base_price    DECIMAL(8,2)   NOT NULL COMMENT 'Used as multiplier base; actual price from SeatTypeZones',
    status        ENUM('AVAILABLE','HOUSEFULL','CANCELLED') NOT NULL DEFAULT 'AVAILABLE',

    PRIMARY KEY (show_id),
    CONSTRAINT fk_show_screen FOREIGN KEY (screen_id) REFERENCES Screens(screen_id)
        ON UPDATE CASCADE,
    CONSTRAINT fk_show_movie  FOREIGN KEY (movie_id)  REFERENCES Movies(movie_id)
        ON UPDATE CASCADE,
    -- No two shows on the same screen can overlap (application-level enforcement)
    INDEX idx_show_screen   (screen_id),
    INDEX idx_show_movie    (movie_id),
    INDEX idx_show_datetime (show_datetime),
    INDEX idx_show_status   (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Scheduled show — links screen + movie + time slot';


-- -------------------------------------------------------------
-- 9. Bookings
-- -------------------------------------------------------------
CREATE TABLE Bookings (
    booking_id   INT            NOT NULL AUTO_INCREMENT,
    customer_id  INT            NOT NULL,
    show_id      INT            NOT NULL,
    total_amount DECIMAL(10,2)  NOT NULL,
    status       ENUM('CONFIRMED','CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    booked_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (booking_id),
    CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id) REFERENCES Users(user_id)
        ON UPDATE CASCADE,
    CONSTRAINT fk_booking_show     FOREIGN KEY (show_id)     REFERENCES Shows(show_id)
        ON UPDATE CASCADE,
    INDEX idx_booking_customer (customer_id),
    INDEX idx_booking_show     (show_id),
    INDEX idx_booking_status   (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Master booking record per customer per show';


-- -------------------------------------------------------------
-- 10. BookedSeats  (junction: which seat in which booking)
-- -------------------------------------------------------------
CREATE TABLE BookedSeats (
    id         INT NOT NULL AUTO_INCREMENT,
    booking_id INT NOT NULL,
    show_id    INT NOT NULL COMMENT 'Denormalised for fast per-show seat lookup',
    seat_id    INT NOT NULL COMMENT 'FK → ScreenLayouts.layout_id',

    PRIMARY KEY (id),
    CONSTRAINT fk_bs_booking FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_bs_show    FOREIGN KEY (show_id)    REFERENCES Shows(show_id)
        ON UPDATE CASCADE,
    CONSTRAINT fk_bs_seat    FOREIGN KEY (seat_id)    REFERENCES ScreenLayouts(layout_id)
        ON UPDATE CASCADE,
    -- A seat can only be booked once per show
    UNIQUE KEY uq_show_seat (show_id, seat_id),
    INDEX idx_bs_show    (show_id),
    INDEX idx_bs_booking (booking_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Maps individual seats to a booking; unique(show_id, seat_id) prevents double-booking';


-- -------------------------------------------------------------
-- 11. Payments
-- -------------------------------------------------------------
CREATE TABLE Payments (
    payment_id INT           NOT NULL AUTO_INCREMENT,
    booking_id INT           NOT NULL,
    amount     DECIMAL(10,2) NOT NULL,
    status     ENUM('PENDING','SUCCESS','REFUNDED') NOT NULL DEFAULT 'PENDING',
    paid_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (payment_id),
    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX idx_payment_booking (booking_id),
    INDEX idx_payment_status  (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Payment record per booking; status can be refunded on cancellation';
