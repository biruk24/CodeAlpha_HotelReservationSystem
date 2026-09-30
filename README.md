# CodeAlpha_HotelReservationSystem

A console-based Java hotel reservation system with **file-based persistence** — built for the **CodeAlpha Java Programming Internship (Task 4)**.

Search rooms by date and category, book with simulated payment, view and cancel reservations. Everything is saved automatically and restored on the next launch.

---

## Table of Contents
- [Features](#features)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [How to Run](#how-to-run)
- [Data Persistence (File I/O)](#data-persistence-file-io)
- [Example Session](#example-session)
- [Validation & Error Handling](#validation--error-handling)
- [Possible Future Improvements](#possible-future-improvements)
- [Author](#author)

---

## Features
- 🔍 Search available rooms by date range, optionally filtered by category (Standard / Deluxe / Suite)
- 🛏 7 preloaded rooms across all three categories, each with its own nightly price
- 📅 Book a room with guest details and simulated card payment
- 🚫 **No double-booking** — a room can never have two active reservations with overlapping dates
- 💳 Payment simulation — a valid-format card confirms the booking (`PAID`); an invalid one still creates the reservation but marks it `FAILED`, so the booking isn't silently lost
- ❌ Cancel a reservation — instantly frees the room for those dates again
- 📋 View full details of any reservation by ID, or list every reservation
- 💾 **Automatic saving** — every booking and cancellation is written to disk and restored on the next launch, including guest de-duplication (the same guest re-booking doesn't create a duplicate record)
- 🛡 Robust validation: bad dates, past check-in dates, unknown rooms, unknown reservations, and a damaged data file are all handled without crashing

## Architecture
Layered architecture, same principle used across all CodeAlpha tasks in this series:

```
┌──────────────────────┐
│  UI Layer              │  ConsoleUI — talks to the user only
├──────────────────────┤
│  Service Layer         │  HotelService — all booking rules
├──────────────────────┤
│  Model Layer           │  Room, Guest, Reservation (+ enums)
├──────────────────────┤
│  Repository Layer      │  RoomRepository, GuestRepository, ReservationRepository
│                        │  DataStorage  ◄── FileDataStorage (disk)
└──────────────────────┘
```

**Key design decisions:**
- **Storage is behind an interface.** `HotelService` depends on `DataStorage`, never on files directly — a database implementation could replace `FileDataStorage` without touching any business logic.
- **`RoomCategory`, `PaymentStatus`, and `ReservationStatus` are enums**, not raw strings, so an invalid state (like a typo'd category) cannot even compile.
- **The overlap check is the core booking rule**, and lives on `Reservation` itself: two date ranges overlap if each starts before the other ends. This one method is what makes double-booking impossible.
- **Reservation IDs are regenerated from existing data on load** (`ReservationRepository.nextId()` scans the highest existing ID), so restored bookings and brand-new ones can never collide.
- **A failed payment doesn't discard the booking.** The reservation is still created, just marked `FAILED`, so a front desk could follow up instead of losing the room hold.

## Project Structure
```
CodeAlpha_HotelReservationSystem/
├── src/com/codealpha/hotel/
│   ├── model/
│   │   ├── Room.java
│   │   ├── RoomCategory.java
│   │   ├── Guest.java
│   │   ├── Reservation.java
│   │   ├── PaymentStatus.java
│   │   └── ReservationStatus.java
│   ├── repository/
│   │   ├── RoomRepository.java
│   │   ├── GuestRepository.java
│   │   ├── ReservationRepository.java
│   │   ├── DataStorage.java          (interface)
│   │   ├── FileDataStorage.java      (file implementation)
│   │   └── StorageException.java
│   ├── service/
│   │   └── HotelService.java
│   ├── ui/
│   │   └── ConsoleUI.java
│   └── Main.java
├── data/                              (created on first run, git-ignored)
│   └── hotel_data.txt
├── .gitignore
└── README.md
```

## How to Run
Requires **JDK 17 or later**.

```bash
# 1. Clone the repo
git clone https://github.com/<biruk24>/CodeAlpha_HotelReservationSystem.git
cd CodeAlpha_HotelReservationSystem

# 2. Compile
find src -name "*.java" > sources.txt
javac -d bin @sources.txt

# 3. Run
java -cp bin com.codealpha.hotel.Main

# (optional) use a custom data file location
java -cp bin com.codealpha.hotel.Main path/to/my_data.txt
```

The hotel starts with 7 preloaded rooms:

| Room | Category | Price/night |
|---|---|---|
| 101, 102 | Standard | $80.00 |
| 103 | Standard | $85.00 |
| 201 | Deluxe | $150.00 |
| 202 | Deluxe | $160.00 |
| 301 | Suite | $300.00 |
| 302 | Suite | $320.00 |

## Data Persistence (File I/O)

On startup the app looks for `data/hotel_data.txt`:
- **Found** → rooms, guests, and reservations are all restored ("Loaded saved data from ...").
- **Not found** → a fresh hotel is set up with the default rooms above, and the file is written immediately.

After every booking and cancellation, the full state is saved again.

**File format** — plain, human-readable text, one record per line, fields separated by `|`:

```
# CodeAlpha Hotel Reservation System data file (v1)
ROOM|101|STANDARD|80.0
GUEST|5cdabc70|Alice Wonder|5551234567
RESERVATION|R001|5cdabc70|101|2026-10-01|2026-10-05|320.0|PAID|ACTIVE|2026-09-29T08:29:18.625609704
```

| Record | Fields |
|---|---|
| `ROOM` | number, category, price per night |
| `GUEST` | id, name, phone |
| `RESERVATION` | id, guest id, room number, check-in, check-out, total price, payment status, reservation status, created-at timestamp |

**Reliability details:**
- **Safe writes** — data is written to a temporary file and then swapped in, so a crash mid-save can never leave a half-written data file.
- **Strict loading** — every line is validated (correct field count, valid dates, positive prices, a reservation's room/guest must actually exist). A bad line reports its exact line number.
- **Corrupt files are never destroyed** — if a file can't be parsed, it's renamed to `hotel_data.txt.corrupt` and a fresh hotel starts, so nothing is silently overwritten.
- **Save failures don't crash the app** — if the disk is unwritable, the app reports the action succeeded but was *not saved*, and keeps running.

## Example Session
```
No saved data found - setting up a new hotel.
=== CodeAlpha Hotel Reservation System ===

1. Search available rooms
2. Book a room
3. View reservation details
4. Cancel a reservation
5. View all reservations
6. Exit
Choose an option: 2
Room number: 101
Check-in date (YYYY-MM-DD): 2026-10-01
Check-out date (YYYY-MM-DD): 2026-10-05
Guest name: Alice Wonder
Guest phone: 5551234567
Card number (16 digits, simulated - not real payment processing): 4111111111111111

Booking confirmed! Reservation ID: R001
Total: $320.00 for 4 night(s). Payment: PAID
```

## Validation & Error Handling
| Scenario | Behavior |
|---|---|
| Check-out on or before check-in | Rejected with a clear message |
| Check-in date in the past | Rejected |
| Booking a room already reserved for overlapping dates | Rejected, no double-booking possible |
| Booking an unknown room number | Rejected |
| Card number not 16 digits | Reservation created but marked `FAILED`; ID given for a retry |
| Viewing/cancelling an unknown reservation ID | Rejected with a clear message |
| Cancelling an already-cancelled reservation | Rejected, no duplicate cancellation |
| Data file is corrupt / has bad values | Exact line reported, file backed up as `.corrupt`, fresh hotel started |
| Data file location not writable | Warning shown, app keeps running |
| Same guest books again | Existing guest record reused, no duplicate |

## Possible Future Improvements
- Swap `FileDataStorage` for a database (SQLite/JDBC) — only a new `DataStorage` implementation is needed
- Real payment gateway integration
- Admin view to add/remove rooms
- Email/SMS booking confirmation (simulated)
- Search by price range or amenities

## Author
Built as part of the **CodeAlpha Java Programming Internship** — Task 4: Hotel Reservation System.
