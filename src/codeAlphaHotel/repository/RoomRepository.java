package codeAlphaHotel.repository;

import codeAlphaHotel.model.Room;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class RoomRepository {

    private final Map<String, Room> rooms = new LinkedHashMap<>();

    public void addRoom(Room room){
        rooms.put(room.getNumber(), room);
    }
    public Optional<Room> getRoom(String number){
        return Optional.ofNullable(rooms.get(number));
    }
    public Optional<Room> findByNumber(String number) {

        return Optional.ofNullable(rooms.get(number));
    }
    public Collection<Room> findAll(){
        return rooms.values();
    }
}
