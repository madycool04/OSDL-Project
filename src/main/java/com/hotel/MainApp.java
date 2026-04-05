package com.hotel;

import com.hotel.service.BookingService;
import com.hotel.service.BillService;
import com.hotel.service.GuestService;
import com.hotel.service.RoomService;
import com.hotel.view.MainView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main entry point for the Hotel Management System.
 * Bootstraps services and launches the JavaFX UI.
 */
public class MainApp extends Application {

    // Shared service singletons
    public static RoomService roomService;
    public static GuestService guestService;
    public static BookingService bookingService;
    public static BillService billService;

    @Override
    public void init() {
        // Initialize services
        roomService    = new RoomService();
        guestService   = new GuestService();
        bookingService = new BookingService(roomService, guestService);
        billService    = new BillService();
    }

    @Override
    public void start(Stage primaryStage) {
        MainView mainView = new MainView(primaryStage);
        mainView.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
