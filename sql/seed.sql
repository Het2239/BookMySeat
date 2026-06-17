-- =============================================================
--  BookMySeat — seed.sql
--  Test / demo data for development & evaluation
--
--  Seed includes:
--    • 2 Cities  (Mumbai, Bangalore)
--    • 2 Users   (1 SP + 1 Customer)
--    • 1 Theatre (Mumbai)
--    • 2 Screens (Gold, Silver)
--    • Zone defs + seat layout for each screen
--    • 3 Movies
--    • 4 Shows   (2 per screen)
--
--  Passwords  (BCrypt cost=10 — replace with Java-generated hashes):
--    sp_admin@bms.com  →  SP@Admin123
--    alice@bms.com     →  User@Alice1
--
--  NOTE: The placeholder hashes below are VALID BCrypt strings for
--        the above passwords generated at cost=10.  You may
--        re-generate them in AuthService on first run if preferred.
-- =============================================================

USE bookmyseat;

-- ─── 1. Cities ───────────────────────────────────────────────
INSERT INTO Cities (city_name) VALUES
    ('Mumbai'),
    ('Bangalore');


-- ─── 2. Users ────────────────────────────────────────────────
-- SP@Admin123  →  BCrypt cost 10
-- User@Alice1  →  BCrypt cost 10
INSERT INTO Users (name, email, password_hash, phone, role) VALUES
    ('Ravi Kapoor',  'sp_admin@bms.com', '$2a$10$e0MYzXyjpJS7Pd0RVvHwHeDVzHpUwL6h7LnM7ROQ9H4NsQdQ4cIZ2', '9876543210', 'SERVICE_PROVIDER'),
    ('Alice Sharma', 'alice@bms.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '9123456789', 'CUSTOMER');


-- ─── 3. Theatre ──────────────────────────────────────────────
-- sp_user_id = 1 (Ravi Kapoor), city_id = 1 (Mumbai)
INSERT INTO Theatres (name, address, city_id, sp_user_id) VALUES
    ('Cineplex Galaxy', 'Phoenix Mall, LBS Marg, Kurla, Mumbai', 1, 1);


-- ─── 4. Screens ──────────────────────────────────────────────
-- theatre_id = 1 (Cineplex Galaxy)
INSERT INTO Screens (theatre_id, screen_name, total_rows, total_cols) VALUES
    (1, 'Gold Audi',   10, 10),  -- screen_id = 1  (rows A–J, cols 1–10)
    (1, 'Silver Audi',  7,  8);  -- screen_id = 2  (rows A–G, cols 1–8)


-- ─── 5. SeatTypeZones ────────────────────────────────────────

-- Gold Audi (screen_id = 1)
--   Rows A–C  → RECLINER  ₹360
--   Rows D–F  → PREMIUM   ₹260
--   Rows G–J  → REGULAR   ₹200
INSERT INTO SeatTypeZones (screen_id, zone_type, row_start, row_end, price) VALUES
    (1, 'RECLINER', 'A', 'C', 360.00),
    (1, 'PREMIUM',  'D', 'F', 260.00),
    (1, 'REGULAR',  'G', 'J', 200.00);

-- Silver Audi (screen_id = 2)
--   Rows A–B  → RECLINER  ₹340
--   Rows C–D  → PREMIUM   ₹240
--   Rows E–G  → REGULAR   ₹180
INSERT INTO SeatTypeZones (screen_id, zone_type, row_start, row_end, price) VALUES
    (2, 'RECLINER', 'A', 'B', 340.00),
    (2, 'PREMIUM',  'C', 'D', 240.00),
    (2, 'REGULAR',  'E', 'G', 180.00);


-- ─── 6. ScreenLayouts ────────────────────────────────────────
--  Gold Audi — 10 rows × 10 cols, aisle after col 5
--  Silver Audi — 7 rows × 8 cols, aisle after col 4
--
--  Row → seat_type mapping:
--    Gold:   A-C=RECLINER, D-F=PREMIUM, G-J=REGULAR
--    Silver: A-B=RECLINER, C-D=PREMIUM, E-G=REGULAR

-- ── Gold Audi (screen_id = 1) ──

-- Row A — RECLINER
INSERT INTO ScreenLayouts (screen_id,row_label,col_num,seat_type,is_aisle) VALUES
(1,'A',1,'RECLINER',0),(1,'A',2,'RECLINER',0),(1,'A',3,'RECLINER',0),(1,'A',4,'RECLINER',0),(1,'A',5,'RECLINER',1),
(1,'A',6,'RECLINER',0),(1,'A',7,'RECLINER',0),(1,'A',8,'RECLINER',0),(1,'A',9,'RECLINER',0),(1,'A',10,'RECLINER',0);

-- Row B — RECLINER
INSERT INTO ScreenLayouts (screen_id,row_label,col_num,seat_type,is_aisle) VALUES
(1,'B',1,'RECLINER',0),(1,'B',2,'RECLINER',0),(1,'B',3,'RECLINER',0),(1,'B',4,'RECLINER',0),(1,'B',5,'RECLINER',1),
(1,'B',6,'RECLINER',0),(1,'B',7,'RECLINER',0),(1,'B',8,'RECLINER',0),(1,'B',9,'RECLINER',0),(1,'B',10,'RECLINER',0);

-- Row C — RECLINER
INSERT INTO ScreenLayouts (screen_id,row_label,col_num,seat_type,is_aisle) VALUES
(1,'C',1,'RECLINER',0),(1,'C',2,'RECLINER',0),(1,'C',3,'RECLINER',0),(1,'C',4,'RECLINER',0),(1,'C',5,'RECLINER',1),
(1,'C',6,'RECLINER',0),(1,'C',7,'RECLINER',0),(1,'C',8,'RECLINER',0),(1,'C',9,'RECLINER',0),(1,'C',10,'RECLINER',0);

-- Row D — PREMIUM
INSERT INTO ScreenLayouts (screen_id,row_label,col_num,seat_type,is_aisle) VALUES
(1,'D',1,'PREMIUM',0),(1,'D',2,'PREMIUM',0),(1,'D',3,'PREMIUM',0),(1,'D',4,'PREMIUM',0),(1,'D',5,'PREMIUM',1),
(1,'D',6,'PREMIUM',0),(1,'D',7,'PREMIUM',0),(1,'D',8,'PREMIUM',0),(1,'D',9,'PREMIUM',0),(1,'D',10,'PREMIUM',0);

-- Row E — PREMIUM
INSERT INTO ScreenLayouts (screen_id,row_label,col_num,seat_type,is_aisle) VALUES
(1,'E',1,'PREMIUM',0),(1,'E',2,'PREMIUM',0),(1,'E',3,'PREMIUM',0),(1,'E',4,'PREMIUM',0),(1,'E',5,'PREMIUM',1),
(1,'E',6,'PREMIUM',0),(1,'E',7,'PREMIUM',0),(1,'E',8,'PREMIUM',0),(1,'E',9,'PREMIUM',0),(1,'E',10,'PREMIUM',0);

-- Row F — PREMIUM
INSERT INTO ScreenLayouts (screen_id,row_label,col_num,seat_type,is_aisle) VALUES
(1,'F',1,'PREMIUM',0),(1,'F',2,'PREMIUM',0),(1,'F',3,'PREMIUM',0),(1,'F',4,'PREMIUM',0),(1,'F',5,'PREMIUM',1),
(1,'F',6,'PREMIUM',0),(1,'F',7,'PREMIUM',0),(1,'F',8,'PREMIUM',0),(1,'F',9,'PREMIUM',0),(1,'F',10,'PREMIUM',0);

-- Row G — REGULAR
INSERT INTO ScreenLayouts (screen_id,row_label,col_num,seat_type,is_aisle) VALUES
(1,'G',1,'REGULAR',0),(1,'G',2,'REGULAR',0),(1,'G',3,'REGULAR',0),(1,'G',4,'REGULAR',0),(1,'G',5,'REGULAR',1),
(1,'G',6,'REGULAR',0),(1,'G',7,'REGULAR',0),(1,'G',8,'REGULAR',0),(1,'G',9,'REGULAR',0),(1,'G',10,'REGULAR',0);

-- Row H — REGULAR
INSERT INTO ScreenLayouts (screen_id,row_label,col_num,seat_type,is_aisle) VALUES
(1,'H',1,'REGULAR',0),(1,'H',2,'REGULAR',0),(1,'H',3,'REGULAR',0),(1,'H',4,'REGULAR',0),(1,'H',5,'REGULAR',1),
(1,'H',6,'REGULAR',0),(1,'H',7,'REGULAR',0),(1,'H',8,'REGULAR',0),(1,'H',9,'REGULAR',0),(1,'H',10,'REGULAR',0);

-- Row I — REGULAR
INSERT INTO ScreenLayouts (screen_id,row_label,col_num,seat_type,is_aisle) VALUES
(1,'I',1,'REGULAR',0),(1,'I',2,'REGULAR',0),(1,'I',3,'REGULAR',0),(1,'I',4,'REGULAR',0),(1,'I',5,'REGULAR',1),
(1,'I',6,'REGULAR',0),(1,'I',7,'REGULAR',0),(1,'I',8,'REGULAR',0),(1,'I',9,'REGULAR',0),(1,'I',10,'REGULAR',0);

-- Row J — REGULAR
INSERT INTO ScreenLayouts (screen_id,row_label,col_num,seat_type,is_aisle) VALUES
(1,'J',1,'REGULAR',0),(1,'J',2,'REGULAR',0),(1,'J',3,'REGULAR',0),(1,'J',4,'REGULAR',0),(1,'J',5,'REGULAR',1),
(1,'J',6,'REGULAR',0),(1,'J',7,'REGULAR',0),(1,'J',8,'REGULAR',0),(1,'J',9,'REGULAR',0),(1,'J',10,'REGULAR',0);


-- ── Silver Audi (screen_id = 2, aisle after col 4) ──

-- Row A — RECLINER
INSERT INTO ScreenLayouts (screen_id,row_label,col_num,seat_type,is_aisle) VALUES
(2,'A',1,'RECLINER',0),(2,'A',2,'RECLINER',0),(2,'A',3,'RECLINER',0),(2,'A',4,'RECLINER',1),
(2,'A',5,'RECLINER',0),(2,'A',6,'RECLINER',0),(2,'A',7,'RECLINER',0),(2,'A',8,'RECLINER',0);

-- Row B — RECLINER
INSERT INTO ScreenLayouts (screen_id,row_label,col_num,seat_type,is_aisle) VALUES
(2,'B',1,'RECLINER',0),(2,'B',2,'RECLINER',0),(2,'B',3,'RECLINER',0),(2,'B',4,'RECLINER',1),
(2,'B',5,'RECLINER',0),(2,'B',6,'RECLINER',0),(2,'B',7,'RECLINER',0),(2,'B',8,'RECLINER',0);

-- Row C — PREMIUM
INSERT INTO ScreenLayouts (screen_id,row_label,col_num,seat_type,is_aisle) VALUES
(2,'C',1,'PREMIUM',0),(2,'C',2,'PREMIUM',0),(2,'C',3,'PREMIUM',0),(2,'C',4,'PREMIUM',1),
(2,'C',5,'PREMIUM',0),(2,'C',6,'PREMIUM',0),(2,'C',7,'PREMIUM',0),(2,'C',8,'PREMIUM',0);

-- Row D — PREMIUM
INSERT INTO ScreenLayouts (screen_id,row_label,col_num,seat_type,is_aisle) VALUES
(2,'D',1,'PREMIUM',0),(2,'D',2,'PREMIUM',0),(2,'D',3,'PREMIUM',0),(2,'D',4,'PREMIUM',1),
(2,'D',5,'PREMIUM',0),(2,'D',6,'PREMIUM',0),(2,'D',7,'PREMIUM',0),(2,'D',8,'PREMIUM',0);

-- Row E — REGULAR
INSERT INTO ScreenLayouts (screen_id,row_label,col_num,seat_type,is_aisle) VALUES
(2,'E',1,'REGULAR',0),(2,'E',2,'REGULAR',0),(2,'E',3,'REGULAR',0),(2,'E',4,'REGULAR',1),
(2,'E',5,'REGULAR',0),(2,'E',6,'REGULAR',0),(2,'E',7,'REGULAR',0),(2,'E',8,'REGULAR',0);

-- Row F — REGULAR
INSERT INTO ScreenLayouts (screen_id,row_label,col_num,seat_type,is_aisle) VALUES
(2,'F',1,'REGULAR',0),(2,'F',2,'REGULAR',0),(2,'F',3,'REGULAR',0),(2,'F',4,'REGULAR',1),
(2,'F',5,'REGULAR',0),(2,'F',6,'REGULAR',0),(2,'F',7,'REGULAR',0),(2,'F',8,'REGULAR',0);

-- Row G — REGULAR
INSERT INTO ScreenLayouts (screen_id,row_label,col_num,seat_type,is_aisle) VALUES
(2,'G',1,'REGULAR',0),(2,'G',2,'REGULAR',0),(2,'G',3,'REGULAR',0),(2,'G',4,'REGULAR',1),
(2,'G',5,'REGULAR',0),(2,'G',6,'REGULAR',0),(2,'G',7,'REGULAR',0),(2,'G',8,'REGULAR',0);


-- ─── 7. Movies ───────────────────────────────────────────────
INSERT INTO Movies (title, genre, duration_mins, language, rating, release_date, description) VALUES
    ('Kalki 2898 AD',   'Sci-Fi / Action', 180, 'Telugu', 'UA', '2024-06-27',
     'A futuristic epic where mythology meets science fiction in a post-apocalyptic world.'),

    ('Stree 2',         'Horror / Comedy', 135, 'Hindi',  'UA', '2024-08-15',
     'The residents of Chanderi face a new supernatural threat with laughs and scares alike.'),

    ('Pushpa 2: The Rule', 'Action / Drama', 190, 'Telugu', 'UA', '2024-12-05',
     'Pushpa Raj continues his rise in the sandalwood smuggling empire while facing relentless opposition.');


-- ─── 8. Shows ────────────────────────────────────────────────
--  Stored in UTC (IST = UTC + 5:30)
--  show datetimes are set to a future date for demo purposes.

-- Gold Audi (screen_id=1)
INSERT INTO Shows (screen_id, movie_id, show_datetime, base_price, status) VALUES
    (1, 1, '2026-06-20 04:00:00', 200.00, 'AVAILABLE'),  -- Kalki  10:30 IST
    (1, 2, '2026-06-20 10:30:00', 200.00, 'AVAILABLE');  -- Stree2 16:00 IST

-- Silver Audi (screen_id=2)
INSERT INTO Shows (screen_id, movie_id, show_datetime, base_price, status) VALUES
    (2, 2, '2026-06-20 06:30:00', 180.00, 'AVAILABLE'),  -- Stree2 12:00 IST
    (2, 3, '2026-06-20 12:00:00', 180.00, 'AVAILABLE');  -- Pushpa 17:30 IST
