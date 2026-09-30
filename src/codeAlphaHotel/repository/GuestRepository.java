package codeAlphaHotel.repository;

import codeAlphaHotel.model.Guest;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class GuestRepository {

    private final Map<String, Guest> guests = new LinkedHashMap<>();
    public void save(Guest guest) {
        guests.put(guest.getId(), guest);
    }
    public Optional<Guest> findById(String id){
        return Optional.ofNullable(guests.get(id));
    }
    public Collection<Guest> findAll(){
        return guests.values();
    }
    public Optional<Guest> findByNameAndPhone(String name, String phone) {
        return guests.values().stream()
                .filter(g -> g.getName().equalsIgnoreCase(name) && g.getPhone().equals(phone))
                .findFirst();


    }
    public int count(){
        return guests.size();
    }
}
