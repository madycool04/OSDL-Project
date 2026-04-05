package com.hotel.service;

import com.hotel.model.Guest;
import com.hotel.util.FileStorageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GuestService {

    private static final String GUESTS_FILE = "guests.dat";
    private List<Guest> guests;

    public GuestService() {
        guests = FileStorageUtil.loadList(GUESTS_FILE);
    }

    public void addGuest(Guest guest) {
        guests.add(guest);
        save();
    }

    public void updateGuest(Guest guest) {
        guests.replaceAll(g -> g.getGuestId().equals(guest.getGuestId()) ? guest : g);
        save();
    }

    public Optional<Guest> findById(String guestId) {
        return guests.stream().filter(g -> g.getGuestId().equals(guestId)).findFirst();
    }

    public Optional<Guest> findByPhone(String phone) {
        return guests.stream().filter(g -> g.getPhone().equals(phone)).findFirst();
    }

    public List<Guest> searchGuests(String query) {
        String lower = query.toLowerCase();
        return guests.stream()
            .filter(g -> g.getName().toLowerCase().contains(lower)
                      || g.getPhone().contains(query)
                      || g.getGuestId().toLowerCase().contains(lower))
            .collect(Collectors.toList());
    }

    public List<Guest> getAllGuests() {
        return new ArrayList<>(guests);
    }

    public long getTotalGuests() {
        return guests.size();
    }

    private void save() {
        FileStorageUtil.saveList(guests, GUESTS_FILE);
    }
}
