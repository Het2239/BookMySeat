# 🎟 BookMySeat

A **terminal-based movie seat booking system** built with Java, JDBC, and MySQL — demonstrating DBMS concepts including ACID transactions, stored procedures, triggers, views, and connection pooling.

---

## 📋 Project Overview

BookMySeat is a role-based CLI application with two user types:

| Role | Capabilities |
|---|---|
| **Customer** | Browse cities → theatres → shows, view live seat matrix, book & cancel seats |
| **Service Provider** | Register theatre, configure screen layout & zones, add movies, schedule shows, view live booking report |

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17+ |
| Database | MySQL 8.x |
| Build Tool | Maven |
| Connection Pool | HikariCP |
| Password Hashing | jBCrypt (cost=12) |
| Logging | SLF4J (NOP) |

---

## 📂 Project Structure

```
BookMySeat/
├── sql/
│   ├── setup.sql            # DB + user creation (run first)
│   ├── schema.sql           # 11-table schema
│   ├── seed.sql             # Demo data (cities, movies, shows)
│   ├── db_objects.sql       # Trigger + view + indexes (Phase 7)
│   └── stored_procedure.sql # sp_book_seats (Phase 8)
│
├── src/main/java/com/bookmyseat/
│   ├── Main.java            # Application entry point
│   ├── db/
│   │   ├── DBConnection.java        # HikariCP singleton pool
│   │   └── TransactionManager.java  # AutoCloseable ACID wrapper
│   ├── model/               # 11 domain POJOs + enums
│   ├── dao/                 # 9 DAOs (JDBC, PreparedStatements)
│   ├── service/
│   │   ├── AuthService.java         # BCrypt register/login
│   │   ├── CustomerService.java     # Browse + seat resolution
│   │   ├── SPService.java           # Theatre/screen/movie/show mgmt
│   │   └── BookingService.java      # ACID booking engine
│   ├── menu/
│   │   ├── MainMenu.java    # Auth routing
│   │   ├── CustomerMenu.java
│   │   └── SPMenu.java
│   ├── util/
│   │   ├── InputHelper.java         # Terminal I/O wrapper
│   │   └── SeatMatrixRenderer.java  # ASCII seat matrix
│   └── exception/
│       └── SeatUnavailableException.java
│
└── src/main/resources/
    └── db.properties        # JDBC connection config
```

---

## ⚙️ Setup

### Prerequisites
- Java 17+
- MySQL 8.x
- Maven 3.8+

### 1. Configure Database

```bash
# Create DB and load schema
mysql -u root -p < sql/schema.sql
mysql -u root -p bookmyseat < sql/seed.sql
mysql -u root -p bookmyseat < sql/db_objects.sql
mysql -u root -p bookmyseat < sql/stored_procedure.sql
```

### 2. Configure Connection

Edit `src/main/resources/db.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/bookmyseat?serverTimezone=UTC
db.username=root
db.password=YOUR_MYSQL_PASSWORD
```

### 3. Build & Run

```bash
mvn package -q
java -jar target/BookMySeat-1.0-SNAPSHOT.jar
```

---

## 🗄️ Database Schema

```
Cities ──< Theatres ──< Screens ──< ScreenLayouts
                                 └─< SeatTypeZones
Users (CUSTOMER / SERVICE_PROVIDER)
Shows (links Movies ↔ Screens)
Bookings ──< BookedSeats (links Bookings ↔ ScreenLayouts)
         └─< Payments
```

### Key DB Objects

| Object | Type | Purpose |
|---|---|---|
| `v_show_availability` | View | Live seat counts per show (total / booked / available) |
| `trg_check_housefull` | Trigger | Auto-flips show status to `HOUSEFULL` when last seat booked |
| `sp_book_seats` | Stored Proc | MySQL-side ACID booking (mirrors Java BookingService) |

---

## 🔐 Key Design Decisions

### ACID Booking Engine
All booking operations share a single JDBC `Connection` via `TransactionManager` (SERIALIZABLE isolation). Seats are locked with `SELECT ... FOR UPDATE` before availability is checked, preventing race conditions.

```
BEGIN TRANSACTION (SERIALIZABLE)
  SELECT seat_ids FOR UPDATE  ← prevents concurrent booking
  INSERT INTO Bookings
  INSERT INTO BookedSeats     ← triggers trg_check_housefull
  INSERT INTO Payments
COMMIT
```

### Password Security
Passwords are hashed with **BCrypt at cost=12** via jBCrypt. Plain-text passwords are never stored or logged.

### Timezone Handling
All datetimes are stored in **UTC** in MySQL. The application layer converts to **IST (Asia/Kolkata)** for all display output using `ZonedDateTime`.

---

## 🎬 User Flows

### Customer
```
Login → Browse City → Browse Theatre → Browse Show
     → View Seat Matrix [RC][PP][__][XX]
     → Select Seats (e.g. A1,A2,B4)
     → Confirm → ✅ Booking Confirmed
     → My Bookings → Cancel (within 2-hour window)
```

### Service Provider
```
Login → Register Theatre → Add Screen (define zones)
     → Add Movie → Schedule Show → View Booking Report
     → Cancel Show
```

---

## 📊 Sample Seat Matrix Output

```
  Legend: [RC]=Recliner ₹400  [PP]=Premium ₹350  [__]=Regular ₹250  [XX]=Booked

       1    2    3    4    |    5    6    7    8
  A  [RC] [RC] [XX] [RC]  |  [RC] [RC] [RC] [RC]
  ──────────────────────────────────────────────
  B  [PP] [PP] [PP] [PP]  |  [PP] [PP] [PP] [PP]
  C  [PP] [XX] [PP] [PP]  |  [PP] [PP] [PP] [PP]
  ──────────────────────────────────────────────
  G  [__] [__] [__] [__]  |  [__] [__] [__] [__]
  H  [__] [__] [__] [__]  |  [__] [__] [__] [__]

  ▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄
                    🎬  S C R E E N
```

---

## 🌱 Seed Data

| Entity | Count |
|---|---|
| Cities | 2 (Mumbai, Bangalore) |
| Theatres | 1 (Cineplex Galaxy, Mumbai) |
| Screens | 2 (Gold Audi 100 seats, Silver Audi 56 seats) |
| Movies | 3 (Kalki 2898 AD, Stree 2, Pushpa 2) |
| Shows | 4 scheduled |
| Users | 2 (1 SP, 1 Customer) |

Seed passwords are BCrypt-hashed. Register fresh accounts for testing.

---

## 📝 Development Phases

| Phase | Description | Status |
|---|---|---|
| 1 | Database schema + seed data | ✅ |
| 2 | Infrastructure (HikariCP, TransactionManager, DAOs) | ✅ |
| 3 | Authentication (BCrypt, role-based routing) | ✅ |
| 4 | Service Provider flow (theatre → screen → movie → show) | ✅ |
| 5 | Customer browse (city → theatre → show → seat matrix) | ✅ |
| 6 | Booking engine (ACID, FOR UPDATE lock) | ✅ |
| 7 | DB objects (HOUSEFULL trigger, availability view, indexes) | ✅ |
| 8 | Stored procedure + final polish | ✅ |

---

## 📄 License

Academic/educational project. Not for production use.
