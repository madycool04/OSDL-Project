package com.hotel.service;

import com.hotel.model.Booking;
import com.hotel.model.Guest;
import com.hotel.model.Room;
import com.hotel.util.FileStorageUtil;
import com.hotel.util.IdGeneratorUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BookingService {

    private static final String BOOKINGS_FILE = "bookings.dat";
    private List<Booking> bookings;
    private final RoomService roomService;
    private final GuestService guestService;

    public BookingService(RoomService roomService, GuestService guestService) {
        this.roomService = roomService;
        this.guestService = guestService;
        this.bookings = FileStorageUtil.loadList(BOOKINGS_FILE);
    }

    /**
     * Create a new booking.
     */
    public Booking createBooking(Guest guest, Room room, LocalDate checkIn,
                                  LocalDate checkOut, int numGuests, String specialRequests) {
        // Save guest if new
        if (guestService.findById(guest.getGuestId()).isEmpty()) {
            guestService.addGuest(guest);
        }

        String bookingId = IdGeneratorUtil.generateBookingId();
        Booking booking = new Booking(bookingId, guest, room, checkIn, checkOut, numGuests);
        booking.setSpecialRequests(specialRequests);

        // Update room status
        room.setStatus(Room.RoomStatus.OCCUPIED);
        roomService.updateRoom(room);

        bookings.add(booking);
        save();
        return booking;
    }

    /**
     * Check in a guest (change status from CONFIRMED to CHECKED_IN).
     */
    public void checkIn(String bookingId) {
        findById(bookingId).ifPresent(b -> {
            b.setStatus(Booking.BookingStatus.CHECKED_IN);
            save();
        });
    }

    /**
     * Check out a guest - computes final bill and frees the room.
     */
    public Booking checkOut(String bookingId, String paymentMode) {
        Optional<Booking> opt = findById(bookingId);
        if (opt.isEmpty()) return null;

        Booking booking = opt.get();
        booking.setStatus(Booking.BookingStatus.CHECKED_OUT);
        booking.setActualCheckOut(LocalDate.now());
        booking.setPaymentMode(paymentMode);
        booking.setPaid(true);
        booking.calculateBill();   // recalculate in case dates changed

        // Free the room
        Room room = booking.getRoom();
        room.setStatus(Room.RoomStatus.AVAILABLE);
        roomService.updateRoom(room);

        save();
        return booking;
    }

    /**
     * Cancel a booking.
     */
    public void cancelBooking(String bookingId) {
        findById(bookingId).ifPresent(b -> {
            b.setStatus(Booking.BookingStatus.CANCELLED);
            b.getRoom().setStatus(Room.RoomStatus.AVAILABLE);
            roomService.updateRoom(b.getRoom());
            save();
        });
    }

    public Optional<Booking> findById(String bookingId) {
        return bookings.stream()
            .filter(b -> b.getBookingId().equals(bookingId))
            .findFirst();
    }

    public List<Booking> getAllBookings() {
        return new ArrayList<>(bookings);
    }

    public List<Booking> getActiveBookings() {
        return bookings.stream()
            .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED
                      || b.getStatus() == Booking.BookingStatus.CHECKED_IN)
            .collect(Collectors.toList());
    }

    public List<Booking> getCheckedOutBookings() {
        return bookings.stream()
            .filter(b -> b.getStatus() == Booking.BookingStatus.CHECKED_OUT)
            .collect(Collectors.toList());
    }

    public List<Booking> getBookingsByGuest(String guestId) {
        return bookings.stream()
            .filter(b -> b.getGuest().getGuestId().equals(guestId))
            .collect(Collectors.toList());
    }

    public double getTotalRevenue() {
        return bookings.stream()
            .filter(Booking::isPaid)
            .mapToDouble(Booking::getTotalAmount)
            .sum();
    }

    public long getTodayCheckIns() {
        LocalDate today = LocalDate.now();
        return bookings.stream()
            .filter(b -> b.getCheckInDate().equals(today))
            .count();
    }

    public long getTodayCheckOuts() {
        LocalDate today = LocalDate.now();
        return bookings.stream()
            .filter(b -> b.getCheckOutDate().equals(today)
                      && b.getStatus() != Booking.BookingStatus.CANCELLED)
            .count();
    }

    private void save() {
        FileStorageUtil.saveList(bookings, BOOKINGS_FILE);
    }
}
