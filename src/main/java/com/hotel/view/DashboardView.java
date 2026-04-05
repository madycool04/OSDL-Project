package com.hotel.view;

import com.hotel.MainApp;
import com.hotel.model.Booking;
import com.hotel.model.Room;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardView {

    private final MainView mainView;
    private ScrollPane root;
    private VBox container;

    public DashboardView(MainView mainView) {
        this.mainView = mainView;
        build();
    }

    private void build() {
        container = new VBox(20);
        container.setPadding(new Insets(0));

        root = new ScrollPane(container);
        root.setFitToWidth(true);
        root.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        root.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    public void refresh() {
        container.getChildren().clear();

        // Page title
        VBox titleBox = new VBox(3);
        Label title = new Label("Dashboard");
        title.getStyleClass().add("page-title");
        Label sub = new Label("Welcome back — here's today's overview");
        sub.getStyleClass().add("page-subtitle");
        titleBox.getChildren().addAll(title, sub);

        // Stats row
        HBox statsRow = buildStatsRow();

        // Middle row: recent bookings + room status
        HBox middleRow = new HBox(20);
        VBox recentBookings = buildRecentBookings();
        VBox roomStatusCard = buildRoomStatusSummary();
        HBox.setHgrow(recentBookings, Priority.ALWAYS);
        middleRow.getChildren().addAll(recentBookings, roomStatusCard);

        // Quick actions
        HBox quickActions = buildQuickActions();

        container.getChildren().addAll(titleBox, statsRow, middleRow, quickActions);
    }

    private HBox buildStatsRow() {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);

        long totalRooms    = MainApp.roomService.getTotalRooms();
        long available     = MainApp.roomService.countByStatus(Room.RoomStatus.AVAILABLE);
        long occupied      = MainApp.roomService.countByStatus(Room.RoomStatus.OCCUPIED);
        long maintenance   = MainApp.roomService.countByStatus(Room.RoomStatus.MAINTENANCE);
        long totalGuests   = MainApp.guestService.getTotalGuests();
        double revenue     = MainApp.bookingService.getTotalRevenue();
        long todayCheckIn  = MainApp.bookingService.getTodayCheckIns();
        long todayCheckOut = MainApp.bookingService.getTodayCheckOuts();

        row.getChildren().addAll(
            makeStatCard(String.valueOf(totalRooms),   "Total Rooms",     "#3b82f6"),
            makeStatCard(String.valueOf(available),    "Available",       "#22c55e"),
            makeStatCard(String.valueOf(occupied),     "Occupied",        "#ef4444"),
            makeStatCard(String.valueOf(maintenance),  "Maintenance",     "#f59e0b"),
            makeStatCard(String.valueOf(totalGuests),  "Total Guests",    "#a855f7"),
            makeStatCard(String.format("₹%.0f", revenue), "Revenue",     "#06b6d4"),
            makeStatCard(String.valueOf(todayCheckIn), "Today Check-In",  "#84cc16"),
            makeStatCard(String.valueOf(todayCheckOut),"Today Check-Out", "#f97316")
        );

        // Wrap in scroll if too wide
        ScrollPane sp = new ScrollPane(row);
        sp.setFitToHeight(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 5 0;");
        sp.setPrefHeight(130);

        HBox wrapper = new HBox(sp);
        HBox.setHgrow(sp, Priority.ALWAYS);
        return wrapper;
    }

    private VBox makeStatCard(String value, String label, String accentColor) {
        VBox card = new VBox(5);
        card.getStyleClass().add("stat-card");
        card.setAlignment(Pos.CENTER_LEFT);

        // Colored top bar
        Rectangle bar = new Rectangle(40, 3);
        bar.setFill(Color.web(accentColor));
        bar.setArcWidth(3);
        bar.setArcHeight(3);

        Label valLabel = new Label(value);
        valLabel.getStyleClass().add("stat-value");
        valLabel.setStyle("-fx-text-fill: " + accentColor + ";");

        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");

        card.getChildren().addAll(bar, valLabel, lbl);
        return card;
    }

    private VBox buildRecentBookings() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        VBox.setVgrow(card, Priority.ALWAYS);

        Label title = new Label("Recent Bookings");
        title.getStyleClass().add("card-title");

        TableView<Booking> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setPrefHeight(260);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Booking, String> idCol = new TableColumn<>("Booking ID");
        idCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getBookingId()));

        TableColumn<Booking, String> guestCol = new TableColumn<>("Guest");
        guestCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getGuest().getName()));

        TableColumn<Booking, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getRoom().getRoomNumber()));

        TableColumn<Booking, String> checkInCol = new TableColumn<>("Check-In");
        checkInCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getCheckInDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));

        TableColumn<Booking, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getStatus().toString().replace("_", " ")));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setStyle(""); return; }
                setText(s);
                String color = switch (s) {
                    case "CONFIRMED"   -> "#3b82f6";
                    case "CHECKED IN"  -> "#22c55e";
                    case "CHECKED OUT" -> "#94a3b8";
                    case "CANCELLED"   -> "#ef4444";
                    default            -> "#e2e8f0";
                };
                setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
            }
        });

        table.getColumns().addAll(idCol, guestCol, roomCol, checkInCol, statusCol);

        List<Booking> recent = MainApp.bookingService.getAllBookings();
        // Show last 10
        int from = Math.max(0, recent.size() - 10);
        table.getItems().addAll(recent.subList(from, recent.size()));

        Label empty = new Label("No bookings yet.");
        empty.setStyle("-fx-text-fill: #64748b;");
        table.setPlaceholder(empty);

        card.getChildren().addAll(title, table);
        return card;
    }

    private VBox buildRoomStatusSummary() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPrefWidth(240);
        card.setMinWidth(220);

        Label title = new Label("Room Status");
        title.getStyleClass().add("card-title");

        long total = MainApp.roomService.getTotalRooms();
        long available = MainApp.roomService.countByStatus(Room.RoomStatus.AVAILABLE);
        long occupied  = MainApp.roomService.countByStatus(Room.RoomStatus.OCCUPIED);
        long maint     = MainApp.roomService.countByStatus(Room.RoomStatus.MAINTENANCE);

        card.getChildren().addAll(
            title,
            makeStatusRow("Available",   available, total, "#22c55e"),
            makeStatusRow("Occupied",    occupied,  total, "#ef4444"),
            makeStatusRow("Maintenance", maint,     total, "#f59e0b")
        );

        // Occupancy rate
        double rate = total > 0 ? (double) occupied / total * 100 : 0;
        Label rateLabel = new Label(String.format("Occupancy Rate: %.1f%%", rate));
        rateLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-padding: 8 0 0 0;");

        // Progress bar
        ProgressBar pb = new ProgressBar(rate / 100.0);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setStyle("-fx-accent: #f59e0b;");

        card.getChildren().addAll(rateLabel, pb);
        return card;
    }

    private VBox makeStatusRow(String label, long count, long total, String color) {
        VBox row = new VBox(4);
        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label cnt = new Label(count + " / " + total);
        cnt.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 12px;");
        top.getChildren().addAll(lbl, spacer, cnt);

        ProgressBar pb = new ProgressBar(total > 0 ? (double) count / total : 0);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setStyle("-fx-accent: " + color + "; -fx-pref-height: 6px;");

        row.getChildren().addAll(top, pb);
        return row;
    }

    private HBox buildQuickActions() {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label("Quick Actions:");
        lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");

        Button newBooking = new Button("➕  New Booking");
        newBooking.getStyleClass().add("btn-primary");
        newBooking.setOnAction(e -> mainView.navigateToBooking());

        Button checkout = new Button("🔑  Check-Out Guest");
        checkout.getStyleClass().add("btn-warning");
        checkout.setOnAction(e -> mainView.navigateToCheckout());

        Label today = makeInfoLabel();
today.setText("📅  Today: " + LocalDate.now().format(
    DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));

        row.getChildren().addAll(lbl, newBooking, checkout, today);
        return row;
    }

    private Label makeInfoLabel() {
        Label l = new Label("📅  Today: " + LocalDate.now().format(
            DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
        l.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px; -fx-padding: 0 0 0 15;");
        return l;
    }

    public javafx.scene.Node getView() { return root; }
}
