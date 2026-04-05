package com.hotel.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum BookingStatus { CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED }

    private String bookingId;
    private Guest guest;
    private Room room;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDate actualCheckOut;
    private BookingStatus status;
    private int numberOfGuests;
    private String specialRequests;

    // Billing
    private double roomCharges;
    private double serviceCharges;    // 10%
    private double taxAmount;         // 18% GST
    private double totalAmount;
    private boolean isPaid;
    private String paymentMode;       // Cash, Card, UPI

    public Booking() {}

    public Booking(String bookingId, Guest guest, Room room,
                   LocalDate checkIn, LocalDate checkOut, int numGuests) {
        this.bookingId = bookingId;
        this.guest = guest;
        this.room = room;
        this.checkInDate = checkIn;
        this.checkOutDate = checkOut;
        this.numberOfGuests = numGuests;
        this.status = BookingStatus.CONFIRMED;
        this.isPaid = false;
        calculateBill();
    }

    public void calculateBill() {
        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (nights <= 0) nights = 1;
        this.roomCharges = room.getPricePerNight() * nights;
        this.serviceCharges = roomCharges * 0.10;
        this.taxAmount = (roomCharges + serviceCharges) * 0.18;
        this.totalAmount = roomCharges + serviceCharges + taxAmount;
    }

    public long getNights() {
        return Math.max(1, ChronoUnit.DAYS.between(checkInDate, checkOutDate));
    }

    // Getters & Setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public Guest getGuest() { return guest; }
    public void setGuest(Guest guest) { this.guest = guest; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public LocalDate getActualCheckOut() { return actualCheckOut; }
    public void setActualCheckOut(LocalDate actualCheckOut) { this.actualCheckOut = actualCheckOut; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public int getNumberOfGuests() { return numberOfGuests; }
    public void setNumberOfGuests(int numberOfGuests) { this.numberOfGuests = numberOfGuests; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public double getRoomCharges() { return roomCharges; }
    public void setRoomCharges(double roomCharges) { this.roomCharges = roomCharges; }

    public double getServiceCharges() { return serviceCharges; }
    public void setServiceCharges(double serviceCharges) { this.serviceCharges = serviceCharges; }

    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    @Override
    public String toString() {
        return "Booking{" + bookingId + ", " + guest.getName() + ", Room " + room.getRoomNumber() + "}";
    }
}
