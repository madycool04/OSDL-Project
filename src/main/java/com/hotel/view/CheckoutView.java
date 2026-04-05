package com.hotel.view;

import com.hotel.MainApp;
import com.hotel.model.Booking;
import com.hotel.service.BillService;
import com.hotel.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class CheckoutView {

    private final MainView mainView;
    private BorderPane root;
    private TableView<Booking> bookingTable;
    private TextArea billPreviewArea;
    private ComboBox<String> paymentCombo;
    private Label selectedLabel;
    private Booking selectedBooking;

    public CheckoutView(MainView mainView) {
        this.mainView = mainView;
        build();
    }

    private void build() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");

        VBox top = new VBox(5);
        top.setPadding(new Insets(0, 0, 15, 0));
        Label title = new Label("Check-Out Management");
        title.getStyleClass().add("page-title");
        Label sub = new Label("Select an active booking to process check-out");
        sub.getStyleClass().add("page-subtitle");
        top.getChildren().addAll(title, sub);
        root.setTop(top);

        SplitPane split = new SplitPane();
        split.setDividerPositions(0.48);
        split.setStyle("-fx-background-color: transparent;");

        split.getItems().addAll(buildBookingListPanel(), buildCheckoutPanel());
        root.setCenter(split);
    }

    private VBox buildBookingListPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: transparent;");

        Label lbl = new Label("Active Bookings");
        lbl.getStyleClass().add("section-header");

        TextField search = new TextField();
        search.setPromptText("🔍  Search by Booking ID or Guest name...");
        search.textProperty().addListener((o, ov, nv) -> filterBookings(nv));

        bookingTable = new TableView<>();
        bookingTable.getStyleClass().add("table-view");
        bookingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(bookingTable, Priority.ALWAYS);

        TableColumn<Booking, String> idCol = new TableColumn<>("Booking ID");
        idCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getBookingId()));

        TableColumn<Booking, String> guestCol = new TableColumn<>("Guest");
        guestCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getGuest().getName()));

        TableColumn<Booking, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getRoom().getRoomNumber() + " (" + d.getValue().getRoom().getRoomType() + ")"));

        TableColumn<Booking, String> inCol = new TableColumn<>("Check-In");
        inCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getCheckInDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));

        TableColumn<Booking, String> outCol = new TableColumn<>("Check-Out");
        outCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getCheckOutDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));

        TableColumn<Booking, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getStatus().toString().replace("_", " ")));
        statusCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setStyle(""); return; }
                setText(s);
                setStyle(s.equals("CHECKED IN") ? "-fx-text-fill: #22c55e; -fx-font-weight: bold;"
                    : "-fx-text-fill: #3b82f6; -fx-font-weight: bold;");
            }
        });

        TableColumn<Booking, String> totalCol = new TableColumn<>("Amount");
        totalCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            String.format("₹%.2f", d.getValue().getTotalAmount())));

        bookingTable.getColumns().addAll(idCol, guestCol, roomCol, inCol, outCol, statusCol, totalCol);
        bookingTable.setPlaceholder(new Label("No active bookings.") {{ setStyle("-fx-text-fill:#64748b;"); }});

        bookingTable.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            selectedBooking = nv;
            updateBillPreview(nv);
        });

        panel.getChildren().addAll(lbl, search, bookingTable);
        return panel;
    }

    private VBox buildCheckoutPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: transparent;");

        Label lbl = new Label("Bill & Checkout");
        lbl.getStyleClass().add("section-header");

        selectedLabel = new Label("No booking selected");
        selectedLabel.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic;");

        billPreviewArea = new TextArea();
        billPreviewArea.setEditable(false);
        billPreviewArea.setWrapText(false);
        billPreviewArea.setPromptText("Select a booking from the left to preview the bill...");
        billPreviewArea.setStyle(
            "-fx-control-inner-background: #ffffff;" +
            "-fx-text-fill: #000000;" +
            "-fx-font-family: 'Consolas';" +
            "-fx-font-size: 13px;" +
            "-fx-border-color: #334155;" +
            "-fx-background-radius: 10;" +
            "-fx-border-radius: 10;"
        );
        VBox.setVgrow(billPreviewArea, Priority.ALWAYS);

        HBox payRow = new HBox(10);
        payRow.setAlignment(Pos.CENTER_LEFT);
        Label payLbl = new Label("Payment Mode:");
        payLbl.getStyleClass().add("field-label");
        paymentCombo = new ComboBox<>(FXCollections.observableArrayList(
            "Cash", "Credit Card", "Debit Card", "UPI", "Net Banking"));
        paymentCombo.setValue("Cash");

        payRow.getChildren().addAll(payLbl, paymentCombo);

        Button checkoutBtn = new Button("✅  Process Checkout & Generate Bill");
        checkoutBtn.getStyleClass().add("btn-success");
        checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.setStyle(checkoutBtn.getStyle() + "-fx-font-size: 13px; -fx-padding: 12;");
        checkoutBtn.setOnAction(e -> processCheckout());

        Button cancelBtn = new Button("❌  Cancel Booking");
        cancelBtn.getStyleClass().add("btn-danger");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setOnAction(e -> cancelBooking());

        Button saveBillBtn = new Button("💾  Save Bill to File");
        saveBillBtn.getStyleClass().add("btn-secondary");
        saveBillBtn.setMaxWidth(Double.MAX_VALUE);
        saveBillBtn.setOnAction(e -> saveBill());

        panel.getChildren().addAll(lbl, selectedLabel, billPreviewArea, payRow,
                                   checkoutBtn, saveBillBtn, cancelBtn);
        return panel;
    }

    private void updateBillPreview(Booking booking) {
        if (booking == null) {
            billPreviewArea.clear();
            selectedLabel.setText("No booking selected");
            return;
        }
        selectedLabel.setText("Selected: " + booking.getBookingId() + "  –  " + booking.getGuest().getName());
        BillService bs = MainApp.billService;
        billPreviewArea.setText(bs.getBillAsString(booking));
    }

    private void processCheckout() {
        if (selectedBooking == null) {
            AlertUtil.showWarning("No Selection", "Please select a booking to check out."); return;
        }
        if (selectedBooking.getStatus() == Booking.BookingStatus.CHECKED_OUT) {
            AlertUtil.showWarning("Already Checked Out", "This booking is already checked out."); return;
        }
        if (!AlertUtil.showConfirm("Confirm Checkout",
            "Process checkout for " + selectedBooking.getGuest().getName() + "?\nTotal: ₹" +
            String.format("%.2f", selectedBooking.getTotalAmount()))) return;

        Booking checked = MainApp.bookingService.checkOut(
            selectedBooking.getBookingId(), paymentCombo.getValue());

        if (checked != null) {
            billPreviewArea.setText(MainApp.billService.getBillAsString(checked));
            AlertUtil.showInfo("Checkout Complete",
                "Checkout processed successfully!\nBilling Amount: ₹" +
                String.format("%.2f", checked.getTotalAmount()));
            refresh();
        }
    }

    private void saveBill() {
        if (selectedBooking == null) {
            AlertUtil.showWarning("No Selection", "Please select a booking first."); return;
        }
        String path = MainApp.billService.generateBill(selectedBooking);
        AlertUtil.showInfo("Bill Saved", "Bill saved to:\n" + path);
    }

    private void cancelBooking() {
        if (selectedBooking == null) {
            AlertUtil.showWarning("No Selection", "Please select a booking."); return;
        }
        if (!AlertUtil.showConfirm("Cancel Booking",
            "Cancel booking " + selectedBooking.getBookingId() + "? This will free the room.")) return;
        MainApp.bookingService.cancelBooking(selectedBooking.getBookingId());
        AlertUtil.showInfo("Cancelled", "Booking cancelled successfully.");
        refresh();
    }

    private void filterBookings(String query) {
        List<Booking> active = MainApp.bookingService.getActiveBookings();
        if (query == null || query.isBlank()) {
            bookingTable.setItems(FXCollections.observableArrayList(active));
            return;
        }
        String lower = query.toLowerCase();
        bookingTable.setItems(FXCollections.observableArrayList(
            active.stream().filter(b ->
                b.getBookingId().toLowerCase().contains(lower) ||
                b.getGuest().getName().toLowerCase().contains(lower)
            ).toList()
        ));
    }

    public void refresh() {
        List<Booking> active = MainApp.bookingService.getActiveBookings();
        bookingTable.setItems(FXCollections.observableArrayList(active));
        selectedBooking = null;
        billPreviewArea.clear();
        selectedLabel.setText("No booking selected");
    }

    public javafx.scene.Node getView() { return root; }
}