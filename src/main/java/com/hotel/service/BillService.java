package com.hotel.service;

import com.hotel.model.Booking;
import com.hotel.util.FileStorageUtil;
import com.hotel.util.IdGeneratorUtil;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Generates hotel bills as formatted text files.
 * (PDF generation requires iTextPDF library - text fallback provided here.)
 */
public class BillService {

    private static final DateTimeFormatter DT_FMT =
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final DateTimeFormatter D_FMT =
        DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /**
     * Generate bill and save to the data directory. Returns the file path.
     */
    public String generateBill(Booking booking) {
        String receiptNo = IdGeneratorUtil.generateReceiptNumber();
        String filename = "Bill_" + booking.getBookingId() + ".txt";
        String path = FileStorageUtil.getDataDir() + File.separator + filename;

        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            printBill(pw, booking, receiptNo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return path;
    }

    private void printBill(PrintWriter pw, Booking booking, String receiptNo) {
        String line   = "=".repeat(54);
        String dashes = "-".repeat(54);

        pw.println(line);
        pw.println("           LIME TREE RESORT MANAGEMENT SYSTEM          ");
        pw.println("         123 Dabra Chowk, Hisar - 125001           ");
        pw.println("         Tel: +91-80-1234-5678  GST: 29ABCDE1234F1Z5");
        pw.println(line);
        pw.println();
        pw.printf("Receipt No : %-35s%n", receiptNo);
        pw.printf("Date       : %-35s%n", LocalDateTime.now().format(DT_FMT));
        pw.println(dashes);
        pw.println("GUEST DETAILS");
        pw.println(dashes);
        pw.printf("Name       : %-35s%n", booking.getGuest().getName());
        pw.printf("Guest ID   : %-35s%n", booking.getGuest().getGuestId());
        pw.printf("Phone      : %-35s%n", booking.getGuest().getPhone());
        pw.printf("Email      : %-35s%n", booking.getGuest().getEmail());
        pw.printf("ID Proof   : %s - %s%n",
                  booking.getGuest().getIdProofType(),
                  booking.getGuest().getIdProofNumber());
        pw.println(dashes);
        pw.println("BOOKING DETAILS");
        pw.println(dashes);
        pw.printf("Booking ID : %-35s%n", booking.getBookingId());
        pw.printf("Room No    : %-35s%n", booking.getRoom().getRoomNumber());
        pw.printf("Room Type  : %-35s%n", booking.getRoom().getRoomType());
        pw.printf("Check-In   : %-35s%n", booking.getCheckInDate().format(D_FMT));
        pw.printf("Check-Out  : %-35s%n", booking.getCheckOutDate().format(D_FMT));
        pw.printf("Nights     : %-35d%n", booking.getNights());
        pw.printf("Guests     : %-35d%n", booking.getNumberOfGuests());
        if (booking.getSpecialRequests() != null && !booking.getSpecialRequests().isEmpty()) {
            pw.printf("Requests   : %-35s%n", booking.getSpecialRequests());
        }
        pw.println(dashes);
        pw.println("BILLING SUMMARY");
        pw.println(dashes);
        pw.printf("%-30s %8s %12.2f%n",
                  "Room Charges (" + booking.getNights() + " night(s))",
                  "INR", booking.getRoomCharges());
        pw.printf("  @INR %.2f/night%n", booking.getRoom().getPricePerNight());
        pw.printf("%-30s %8s %12.2f%n",
                  "Service Charges (10%)", "INR", booking.getServiceCharges());
        pw.printf("%-30s %8s %12.2f%n",
                  "GST (18%)", "INR", booking.getTaxAmount());
        pw.println(dashes);
        pw.printf("%-30s %8s %12.2f%n",
                  "TOTAL AMOUNT", "INR", booking.getTotalAmount());
        pw.println(line);
        pw.printf("Payment Mode : %-37s%n",
                  booking.getPaymentMode() != null ? booking.getPaymentMode() : "PENDING");
        pw.printf("Payment Status: %-36s%n", booking.isPaid() ? "PAID" : "UNPAID");
        pw.println(line);
        pw.println();
        pw.println("  Thank you for choosing Lime Tree Resort!");
        pw.println("  We look forward to your next visit.");
        pw.println();
        pw.println("     **** THIS IS A COMPUTER GENERATED BILL ****");
        pw.println(line);
    }

    /**
     * Return the bill as a formatted String (for in-app display).
     */
    public String getBillAsString(Booking booking) {
        StringWriter sw = new StringWriter();
        printBill(new PrintWriter(sw), booking, IdGeneratorUtil.generateReceiptNumber());
        return sw.toString();
    }
}
