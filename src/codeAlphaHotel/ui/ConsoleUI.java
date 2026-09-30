package codeAlphaHotel.ui;

import codeAlphaHotel.model.Guest;
import codeAlphaHotel.model.Reservation;
import codeAlphaHotel.model.Room;
import codeAlphaHotel.model.RoomCategory;
import codeAlphaHotel.repository.StorageException;
import codeAlphaHotel.service.HotelService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {

    private final HotelService service;
    private final Scanner scanner;

    public ConsoleUI(HotelService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println(">>>>       CodeAlpha Hotel Reservation System <<<<");
        boolean running = true;
        while (running) {

            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> searchRooms();
                case "2" -> bookRoom();
                case "3" -> viewReservation();
                case "4" -> cancelReservation();
                case "5" -> viewAllReservations();
                case "6" -> {
                    System.out.println("Goodbye!");
                    running = false;

                }
                default -> System.out.println("Invalid option, please choose 1-6.");
            }
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
        scanner.close();
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1. Search available rooms");
        System.out.println("2. Book a room");
        System.out.println("3. View reservation details");
        System.out.println("4. Cancel a reservation");
        System.out.println("5. View all reservations");
        System.out.println("6. Exit");
        System.out.print("Choose an option: ");
    }

    private LocalDate readDate(String prompt) {
        System.out.print(prompt + " (YYYY-MM-DD): ");
        String raw = scanner.nextLine().trim();
        return LocalDate.parse(raw);
    }

    private void searchRooms() {
        try {
            LocalDate checkIn = readDate("Check-in date");
            LocalDate checkOut = readDate("Check-out date");
            System.out.print("Category filter (STANDARD / DELUXE / SUITE, blank for any): ");
            String rawCategory = scanner.nextLine().trim();
            RoomCategory category = rawCategory.isEmpty() ? null : RoomCategory.valueOf(rawCategory.toUpperCase());

            List<Room> available = service.searchAvailableRooms(checkIn, checkOut, category);
            System.out.println();
            if (available.isEmpty()) {
                System.out.println("No rooms available for those dates.");
            } else {
                System.out.println(">>> Available Rooms <<<");
                for (Room r : available) {
                    System.out.println(r);
                }
            }
        } catch (DateTimeParseException e) {
            System.out.println("Error: date must be in YYYY-MM-DD format.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void bookRoom() {
        try {
            System.out.print("Room number: ");
            String roomNumber = scanner.nextLine().trim().toUpperCase();
            LocalDate checkIn = readDate("Check-in date");
            LocalDate checkOut = readDate("Check-out date");
            System.out.print("Guest name: ");
            String guestName = scanner.nextLine().trim();
            System.out.print("Guest phone: ");
            String guestPhone = scanner.nextLine().trim();
            System.out.print("Card number (16 digits, simulated - not real payment processing): ");
            String cardNumber = scanner.nextLine().trim();

            Reservation reservation = service.bookRoom(guestName, guestPhone, roomNumber, checkIn, checkOut, cardNumber);
            System.out.println();
            System.out.println("Booking confirmed! Reservation ID: " + reservation.getId());
            System.out.printf("Total: $%.2f for %d night(s). Payment: %s%n",
                    reservation.getTotalPrice(), reservation.getNights(), reservation.getPaymentStatus());
        } catch (DateTimeParseException e) {
            System.out.println("Error: date must be in YYYY-MM-DD format.");
        } catch (HotelService.PaymentFailedException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("You can try paying again later using this reservation ID: " + e.getReservationId());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (StorageException e) {
            System.out.println("Warning: booking succeeded, but NOT saved to disk. " + e.getMessage());
        }
    }

    private void viewReservation() {
        System.out.print("Reservation ID: ");
        String id = scanner.nextLine().trim().toUpperCase();
        try {
            Reservation r = service.getReservation(id);
            Guest g = service.getGuest(r.getGuestId());
            System.out.println();
            System.out.println("=== Reservation " + r.getId() + " ===");
            System.out.println("Guest:      " + g.getName() + " (" + g.getPhone() + ")");
            System.out.println("Room:       " + r.getRoomNumber());
            System.out.println("Dates:      " + r.getCheckIn() + " to " + r.getCheckOut() + " (" + r.getNights() + " night(s))");
            System.out.printf("Total:      $%.2f%n", r.getTotalPrice());
            System.out.println("Payment:    " + r.getPaymentStatus());
            System.out.println("Status:     " + r.getStatus());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void cancelReservation() {
        System.out.print("Reservation ID to cancel: ");
        String id = scanner.nextLine().trim().toUpperCase();
        try {
            service.cancelReservation(id);
            System.out.println("Reservation " + id + " cancelled. The room is now free for those dates.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (StorageException e) {
            System.out.println("Warning: cancelled, but NOT saved to disk. " + e.getMessage());
        }
    }

    private void viewAllReservations() {
        List<Reservation> all = service.getAllReservations();
        System.out.println();
        if (all.isEmpty()) {
            System.out.println("No reservations yet.");
            return;
        }
        System.out.println("=== All Reservations ===");
        for (Reservation r : all) {
            System.out.printf("%-6s room=%-6s %s to %s  $%-8.2f %-8s %-9s%n",
                    r.getId(), r.getRoomNumber(), r.getCheckIn(), r.getCheckOut(),
                    r.getTotalPrice(), r.getPaymentStatus(), r.getStatus());
        }
    }
}
