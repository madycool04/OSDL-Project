package com.hotel.model;

import java.io.Serializable;

public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum RoomType { SINGLE, DOUBLE, SUITE, DELUXE }
    public enum RoomStatus { AVAILABLE, OCCUPIED, MAINTENANCE }

    private String roomNumber;
    private RoomType roomType;
    private RoomStatus status;
    private double pricePerNight;
    private int floor;
    private String description;
    private boolean hasAC;
    private boolean hasWifi;
    private boolean hasTV;
    private int maxOccupancy;

    public Room() {}

    public Room(String roomNumber, RoomType roomType, double pricePerNight, int floor) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.floor = floor;
        this.status = RoomStatus.AVAILABLE;
        this.hasAC = true;
        this.hasWifi = true;
        this.hasTV = true;
        this.maxOccupancy = roomType == RoomType.SINGLE ? 1 : roomType == RoomType.DOUBLE ? 2 : 4;
        this.description = generateDescription();
    }

    private String generateDescription() {
        return switch (roomType) {
            case SINGLE -> "Cozy single room with city view";
            case DOUBLE -> "Spacious double room with king-size bed";
            case SUITE -> "Luxurious suite with living area and premium amenities";
            case DELUXE -> "Deluxe room with panoramic view and premium furnishings";
        };
    }

    // Getters & Setters
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }

    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isHasAC() { return hasAC; }
    public void setHasAC(boolean hasAC) { this.hasAC = hasAC; }

    public boolean isHasWifi() { return hasWifi; }
    public void setHasWifi(boolean hasWifi) { this.hasWifi = hasWifi; }

    public boolean isHasTV() { return hasTV; }
    public void setHasTV(boolean hasTV) { this.hasTV = hasTV; }

    public int getMaxOccupancy() { return maxOccupancy; }
    public void setMaxOccupancy(int maxOccupancy) { this.maxOccupancy = maxOccupancy; }

    public boolean isAvailable() { return status == RoomStatus.AVAILABLE; }

    @Override
    public String toString() {
        return "Room{" + roomNumber + ", " + roomType + ", " + status + ", ₹" + pricePerNight + "}";
    }
}
