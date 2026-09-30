package codeAlphaHotel.service;

import codeAlphaHotel.model.Guest;
import codeAlphaHotel.model.PaymentStatus;
import codeAlphaHotel.model.Reservation;
import codeAlphaHotel.model.ReservationStatus;
import codeAlphaHotel.model.Room;
import codeAlphaHotel.model.RoomCategory;
import codeAlphaHotel.repository.DataStorage;
import codeAlphaHotel.repository.GuestRepository;
import codeAlphaHotel.repository.ReservationRepository;
import codeAlphaHotel.repository.RoomRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public  class HotelService {


    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final ReservationRepository reservationRepository;
    private final DataStorage storage;

    public HotelService(RoomRepository roomRepository, GuestRepository guestRepository,
                        ReservationRepository reservationRepository, DataStorage storage) {
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
        this.reservationRepository = reservationRepository;
        this.storage = storage;
    }

    private void persist() {
        storage.save(roomRepository.findAll(), guestRepository.findAll(), reservationRepository.findAll());
    }

    public List<Room> getAllRooms() {

        return new ArrayList<>(roomRepository.findAll());
    }


    public List<Room> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut, RoomCategory category) {
        validateDateRange(checkIn, checkOut);
        List<Room> available = new ArrayList<>();
        for (Room room : roomRepository.findAll()) {
            if (category != null && room.getCategory() != category) {
                continue;
            }
            if (!reservationRepository.hasOverlap(room.getNumber(), checkIn, checkOut)) {
                available.add(room);
            }
        }
        return available;
    }

    private void validateDateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Both dates are required.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date.");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past.");
        }
    }


    public boolean simulatePayment(String cardNumber) {
        String digitsOnly = cardNumber.replaceAll("[ -]", "");
        return digitsOnly.matches("\\d{16}");
    }

    public  Reservation bookRoom(String guestName, String guestPhone, String roomNumber,
                                LocalDate checkIn, LocalDate checkOut, String cardNumber) {
        if (guestName == null || guestName.isBlank()) {
            throw new IllegalArgumentException("Guest name is required.");
        }
        if (guestPhone == null || guestPhone.isBlank()) {
            throw new IllegalArgumentException("Guest phone is required.");
        }
        validateDateRange(checkIn, checkOut);

        Room room = roomRepository.findByNumber(roomNumber)
                .orElseThrow(() -> new IllegalArgumentException("No room found with number '" + roomNumber + "'."));

        if (reservationRepository.hasOverlap(roomNumber, checkIn, checkOut)) {
            throw new IllegalArgumentException(
                    "Room " + roomNumber + " is already booked for part or all of that date range.");
        }

        Guest guest = guestRepository.findByNameAndPhone(guestName, guestPhone)
                .orElseGet(() -> {
                    Guest g = new Guest(UUID.randomUUID().toString().substring(0, 8), guestName, guestPhone);
                    guestRepository.save(g);
                    return g;
                });

        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        double totalPrice = nights * room.getPricePreNight();

        boolean paymentOk = simulatePayment(cardNumber);
        PaymentStatus paymentStatus = paymentOk ? PaymentStatus.PAID : PaymentStatus.FAILED;

        Reservation reservation = new Reservation(reservationRepository.nextId(), guest.getId(), roomNumber,
                checkIn, checkOut, totalPrice, paymentStatus, ReservationStatus.ACTIVE, LocalDateTime.now());
        reservationRepository.save(reservation);
        persist();

        if (!paymentOk) {
            throw new PaymentFailedException(reservation.getId());
        }
        return reservation;
    }


    public static class PaymentFailedException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final String reservationId;

        public PaymentFailedException(String reservationId) {
            super("Payment failed. Reservation " + reservationId
                    + " was created but is unpaid - it will not be honored until payment succeeds.");
            this.reservationId = reservationId;
        }

        public String getReservationId() {
            return reservationId;
        }
    }

    public void cancelReservation(String reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("No reservation found with ID '" + reservationId + "'."));
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalArgumentException("Reservation " + reservationId + " is already cancelled.");
        }
        reservation.cancel();
        persist();
    }

    public Reservation getReservation(String reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("No reservation found with ID '" + reservationId + "'."));
    }

    public Guest getGuest(String guestId) {
        return guestRepository.findById(guestId)
                .orElseThrow(() -> new IllegalArgumentException("No guest found with ID '" + guestId + "'."));
    }

    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservationRepository.findAll());
    }
}
