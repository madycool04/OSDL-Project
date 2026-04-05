package com.hotel.service;

import com.hotel.model.Room;
import com.hotel.util.FileStorageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RoomService {

    private static final String ROOMS_FILE = "rooms.dat";
    private List<Room> rooms;

    public RoomService() {
        rooms = FileStorageUtil.loadList(ROOMS_FILE);
        if (rooms.isEmpty()) {
            initializeSampleRooms();
        }
    }

    private void initializeSampleRooms() {
        // Floor 1 - Singles
        rooms.add(new Room("101", Room.RoomType.SINGLE, 1500.0, 1));
        rooms.add(new Room("102", Room.RoomType.SINGLE, 1500.0, 1));
        rooms.add(new Room("103", Room.RoomType.DOUBLE, 2500.0, 1));
        rooms.add(new Room("104", Room.RoomType.DOUBLE, 2500.0, 1));

        // Floor 2 - Doubles
        rooms.add(new Room("201", Room.RoomType.DOUBLE, 2800.0, 2));
        rooms.add(new Room("202", Room.RoomType.DOUBLE, 2800.0, 2));
        rooms.add(new Room("203", Room.RoomType.DELUXE, 4000.0, 2));
        rooms.add(new Room("204", Room.RoomType.DELUXE, 4000.0, 2));

        // Floor 3 - Suites
        rooms.add(new Room("301", Room.RoomType.SUITE, 7000.0, 3));
        rooms.add(new Room("302", Room.RoomType.SUITE, 7500.0, 3));
        rooms.add(new Room("303", Room.RoomType.DELUXE, 4500.0, 3));

        // One under maintenance
        Room maint = new Room("105", Room.RoomType.SINGLE, 1500.0, 1);
        maint.setStatus(Room.RoomStatus.MAINTENANCE);
        rooms.add(maint);

        save();
    }

    public void addRoom(Room room) {
        rooms.add(room);
        save();
    }

    public void updateRoom(Room room) {
        rooms.replaceAll(r -> r.getRoomNumber().equals(room.getRoomNumber()) ? room : r);
        save();
    }

    public void deleteRoom(String roomNumber) {
        rooms.removeIf(r -> r.getRoomNumber().equals(roomNumber));
        save();
    }

    public Optional<Room> findByNumber(String roomNumber) {
        return rooms.stream()
            .filter(r -> r.getRoomNumber().equalsIgnoreCase(roomNumber))
            .findFirst();
    }

    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms);
    }

    public List<Room> getAvailableRooms() {
        return rooms.stream()
            .filter(Room::isAvailable)
            .collect(Collectors.toList());
    }

    public List<Room> getRoomsByType(Room.RoomType type) {
        return rooms.stream()
            .filter(r -> r.getRoomType() == type)
            .collect(Collectors.toList());
    }

    public List<Room> getAvailableRoomsByType(Room.RoomType type) {
        return rooms.stream()
            .filter(r -> r.isAvailable() && r.getRoomType() == type)
            .collect(Collectors.toList());
    }

    public boolean roomNumberExists(String roomNumber) {
        return rooms.stream().anyMatch(r -> r.getRoomNumber().equalsIgnoreCase(roomNumber));
    }

    public long countByStatus(Room.RoomStatus status) {
        return rooms.stream().filter(r -> r.getStatus() == status).count();
    }

    public long getTotalRooms() {
        return rooms.size();
    }

    private void save() {
        FileStorageUtil.saveList(rooms, ROOMS_FILE);
    }
}
