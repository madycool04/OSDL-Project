package com.hotel.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class IdGeneratorUtil {

    private static final AtomicInteger bookingCounter = new AtomicInteger(
        FileStorageUtil.loadObject("booking_counter.dat", 1000)
    );
    private static final AtomicInteger guestCounter = new AtomicInteger(
        FileStorageUtil.loadObject("guest_counter.dat", 1000)
    );

    public static String generateBookingId() {
        int next = bookingCounter.incrementAndGet();
        FileStorageUtil.saveObject(next, "booking_counter.dat");
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "BK" + date + next;
    }

    public static String generateGuestId() {
        int next = guestCounter.incrementAndGet();
        FileStorageUtil.saveObject(next, "guest_counter.dat");
        return "GST" + String.format("%04d", next);
    }

    public static String generateReceiptNumber() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "RCP" + ts;
    }
}
