package codeAlphaHotel.repository;

import codeAlphaHotel.model.Reservation;
import codeAlphaHotel.model.ReservationStatus;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class ReservationRepository {


    private final Map<String, Reservation> reservations = new LinkedHashMap<>();

    public void save(Reservation reservation) {
        reservations.put(reservation.getId(), reservation);
    }

    public Optional<Reservation> findById(String id) {

        return Optional.ofNullable(reservations.get(id));
    }

    public Collection<Reservation> findAll() {
        return reservations.values();



    }



    public boolean hasOverlap(String roomNumber, LocalDate checkIn, LocalDate checkOut) {
        return reservations.values().stream()
                .anyMatch(r -> r.getStatus() == ReservationStatus.ACTIVE
                        && r.getRoomNumber().equals(roomNumber)
                        && r.overlaps(checkIn, checkOut));
    }


    public String nextId() {
        int max = 0;
        for (String id : reservations.keySet()) {
            if (id.startsWith("R")) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(1)));
                } catch (NumberFormatException ignored) {

                }
            }
        }
        return "R" + String.format("%03d", max + 1);
    }
}


