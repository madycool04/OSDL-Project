package com.hotel.view;

import com.hotel.MainApp;
import com.hotel.model.Booking;
import com.hotel.model.Guest;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class GuestsView {

    private BorderPane root;
    private TableView<Guest> guestTable;
    private TableView<Booking> historyTable;
    private TextField searchField;

    public GuestsView() {
        build();
    }

    private void build() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");

        // Top
        VBox top = new VBox(12);
        top.setPadding(new Insets(0, 0, 15, 0));
        Label title = new Label("Guest Registry");
        title.getStyleClass().add("page-title");
        Label sub = new Label("All registered guests and their booking history");
        sub.getStyleClass().add("page-subtitle");

        searchField = new TextField();
        searchField.setPromptText("🔍  Search by name, phone or guest ID...");
        searchField.setPrefWidth(320);
        searchField.textProperty().addListener((o, ov, nv) -> filterGuests(nv));

        HBox searchRow = new HBox(15, searchField);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(title, sub, searchRow);
        root.setTop(top);

        // Split: guest table | history
        SplitPane split = new SplitPane();
        split.setDividerPositions(0.5);
        split.setStyle("-fx-background-color: transparent;");
        split.getItems().addAll(buildGuestPanel(), buildHistoryPanel());
        root.setCenter(split);
    }

    private VBox buildGuestPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(8));
        panel.setStyle("-fx-background-color: transparent;");

        Label lbl = new Label("All Guests");
        lbl.getStyleClass().add("section-header");

        guestTable = new TableView<>();
        guestTable.getStyleClass().add("table-view");
        guestTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(guestTable, Priority.ALWAYS);

        TableColumn<Guest, String> idCol = new TableColumn<>("Guest ID");
        idCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getGuestId()));

        TableColumn<Guest, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getName()));

        TableColumn<Guest, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getPhone()));

        TableColumn<Guest, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getEmail() != null ? d.getValue().getEmail() : "–"));

        TableColumn<Guest, String> idProofCol = new TableColumn<>("ID Proof");
        idProofCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getIdProofType() + ": " + d.getValue().getIdProofNumber()));

        TableColumn<Guest, String> bookingsCol = new TableColumn<>("Bookings");
        bookingsCol.setCellValueFactory(d -> {
            long count = MainApp.bookingService.getBookingsByGuest(d.getValue().getGuestId()).size();
            return new javafx.beans.property.SimpleStringProperty(String.valueOf(count));
        });

        guestTable.getColumns().addAll(idCol, nameCol, phoneCol, emailCol, idProofCol, bookingsCol);
        guestTable.setPlaceholder(new Label("No guests registered yet.") {{ setStyle("-fx-text-fill:#64748b;"); }});

        guestTable.getSelectionModel().selectedItemProperty().addListener(
            (o, ov, nv) -> showGuestHistory(nv));

        panel.getChildren().addAll(lbl, guestTable);
        return panel;
    }

    private VBox buildHistoryPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(8));
        panel.setStyle("-fx-background-color: transparent;");

        Label lbl = new Label("Booking History");
        lbl.getStyleClass().add("section-header");

        historyTable = new TableView<>();
        historyTable.getStyleClass().add("table-view");
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(historyTable, Priority.ALWAYS);

        TableColumn<Booking, String> idCol = new TableColumn<>("Booking ID");
        idCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getBookingId()));

        TableColumn<Booking, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getRoom().getRoomNumber() + " – " + d.getValue().getRoom().getRoomType()));

        TableColumn<Booking, String> inCol = new TableColumn<>("Check-In");
        inCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getCheckInDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));

        TableColumn<Booking, String> outCol = new TableColumn<>("Check-Out");
        outCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getCheckOutDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));

        TableColumn<Booking, String> nightsCol = new TableColumn<>("Nights");
        nightsCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            String.valueOf(d.getValue().getNights())));

        TableColumn<Booking, String> amtCol = new TableColumn<>("Amount");
        amtCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            String.format("₹ %.2f", d.getValue().getTotalAmount())));

        TableColumn<Booking, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getStatus().toString().replace("_", " ")));
        statusCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setStyle(""); return; }
                setText(s);
                String col = switch (s) {
                    case "CONFIRMED"   -> "#3b82f6";
                    case "CHECKED IN"  -> "#22c55e";
                    case "CHECKED OUT" -> "#94a3b8";
                    case "CANCELLED"   -> "#ef4444";
                    default            -> "#e2e8f0";
                };
                setStyle("-fx-text-fill: " + col + "; -fx-font-weight: bold;");
            }
        });

        historyTable.getColumns().addAll(idCol, roomCol, inCol, outCol, nightsCol, amtCol, statusCol);
        historyTable.setPlaceholder(new Label("Select a guest to see their booking history.") {{
            setStyle("-fx-text-fill:#64748b;");
        }});

        panel.getChildren().addAll(lbl, historyTable);
        return panel;
    }

    private void showGuestHistory(Guest guest) {
        if (guest == null) { historyTable.getItems().clear(); return; }
        List<Booking> history = MainApp.bookingService.getBookingsByGuest(guest.getGuestId());
        historyTable.setItems(FXCollections.observableArrayList(history));
    }

    private void filterGuests(String query) {
        if (query == null || query.isBlank()) {
            guestTable.setItems(FXCollections.observableArrayList(MainApp.guestService.getAllGuests()));
            return;
        }
        guestTable.setItems(FXCollections.observableArrayList(
            MainApp.guestService.searchGuests(query)
        ));
    }

    public void refresh() {
        guestTable.setItems(FXCollections.observableArrayList(MainApp.guestService.getAllGuests()));
        historyTable.getItems().clear();
    }

    public javafx.scene.Node getView() { return root; }
}
