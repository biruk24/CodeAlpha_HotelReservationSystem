package codeAlphaHotel.repository;

import codeAlphaHotel.model.Guest;
import codeAlphaHotel.model.Reservation;
import codeAlphaHotel.model.Room;

import java.util.Collection;

public interface DataStorage {

    boolean exists();


    void save(Collection<Room> rooms, Collection<Guest> guests, Collection<Reservation> reservations);

    void load(RoomRepository roomRepository, GuestRepository guestRepository, ReservationRepository reservationRepository);

    String describeLocation();
}


