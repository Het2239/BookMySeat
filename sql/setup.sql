-- =============================================================
--  BookMySeat — setup.sql
--  Run this FIRST as root/admin MySQL user.
--  Then run schema.sql, then seed.sql.
-- =============================================================

-- 1. Create database
CREATE DATABASE IF NOT EXISTS bookmyseat
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 2. Create application user (change password as needed)
CREATE USER IF NOT EXISTS 'bms_user'@'localhost' IDENTIFIED BY 'BMS@Secure2026';
GRANT ALL PRIVILEGES ON bookmyseat.* TO 'bms_user'@'localhost';
FLUSH PRIVILEGES;

-- 3. Verify
SHOW DATABASES LIKE 'bookmyseat';

-- =============================================================
--  After running this file, execute in order:
--    mysql -u bms_user -p bookmyseat < sql/schema.sql
--    mysql -u bms_user -p bookmyseat < sql/seed.sql
-- =============================================================
