package com.hotel.view;

import com.hotel.MainApp;
import com.hotel.model.Booking;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class BillingView {

    private BorderPane root;
    private TableView<Booking> billTable;
    private TextArea billDetailArea;
    private Label totalRevenueLabel, paidCountLabel, pendingCountLabel, avgBillLabel;

    public BillingView() {
        build();
    }

    private void build() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");

        VBox top = new VBox(15);
        top.setPadding(new Insets(0, 0, 15, 0));

        Label title = new Label("Billing & Revenue");
        title.getStyleClass().add("page-title");
        Label sub = new Label("Financial overview and billing history");
        sub.getStyleClass().add("page-subtitle");

        top.getChildren().addAll(title, sub, buildSummaryCards());
        root.setTop(top);

        SplitPane split = new SplitPane();
        split.setDividerPositions(0.48);
        split.setStyle("-fx-background-color: transparent;");
        split.getItems().addAll(buildBillListPanel(), buildDetailPanel());
        root.setCenter(split);
    }

    private HBox buildSummaryCards() {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);

        totalRevenueLabel = bigLabel("₹ 0");
        paidCountLabel    = bigLabel("0");
        pendingCountLabel = bigLabel("0");
        avgBillLabel      = bigLabel("₹ 0");

        row.getChildren().addAll(
            miniCard("Total Revenue", totalRevenueLabel, "#06b6d4"),
            miniCard("Paid Bills", paidCountLabel, "#22c55e"),
            miniCard("Pending/Active", pendingCountLabel, "#f59e0b"),
            miniCard("Avg Bill Value", avgBillLabel, "#a855f7")
        );
        return row;
    }

    private VBox miniCard(String label, Label valueLabel, String color) {
        VBox card = new VBox(5);
        card.getStyleClass().add("stat-card");
        card.setAlignment(Pos.CENTER_LEFT);

        Rectangle bar = new Rectangle(35, 3, Color.web(color));
        bar.setArcWidth(3);
        bar.setArcHeight(3);

        valueLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");

        card.getChildren().addAll(bar, valueLabel, lbl);
        return card;
    }

    private Label bigLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e2e8f0;");
        return l;
    }

    private VBox buildBillListPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(8));
        panel.setStyle("-fx-background-color: transparent;");

        HBox filterRow = new HBox(10);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        Label filterLbl = new Label("Filter:");
        filterLbl.getStyleClass().add("field-label");

        ComboBox<String> statusFilter = new ComboBox<>(FXCollections.observableArrayList(
            "All", "Paid", "Unpaid", "Cancelled"));
        statusFilter.setValue("All");
        statusFilter.setOnAction(e -> applyFilter(statusFilter.getValue()));

        TextField search = new TextField();
        search.setPromptText("Search guest/booking...");
        search.setPrefWidth(180);
        search.textProperty().addListener((o, ov, nv) -> searchBills(nv));

        filterRow.getChildren().addAll(filterLbl, statusFilter, search);

        billTable = new TableView<>();
        billTable.getStyleClass().add("table-view");
        billTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(billTable, Priority.ALWAYS);

        TableColumn<Booking, String> idCol = new TableColumn<>("Booking ID");
        idCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getBookingId()));

        TableColumn<Booking, String> guestCol = new TableColumn<>("Guest");
        guestCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getGuest().getName()));

        TableColumn<Booking, String> dateCol = new TableColumn<>("Check-In");
        dateCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getCheckInDate().format(DateTimeFormatter.ofPattern("dd-MM-yy"))));

        TableColumn<Booking, String> nightsCol = new TableColumn<>("Nights");
        nightsCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            String.valueOf(d.getValue().getNights())));
        nightsCol.setPrefWidth(60);

        TableColumn<Booking, String> amtCol = new TableColumn<>("Total (₹)");
        amtCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            String.format("%.2f", d.getValue().getTotalAmount())));

        TableColumn<Booking, String> payCol = new TableColumn<>("Payment");
        payCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getPaymentMode() != null ? d.getValue().getPaymentMode() : "–"));

        TableColumn<Booking, String> paidCol = new TableColumn<>("Status");
        paidCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().isPaid() ? "PAID" :
            d.getValue().getStatus() == Booking.BookingStatus.CANCELLED ? "CANCELLED" : "PENDING"));

        paidCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setStyle(""); return; }
                setText(s);
                setStyle(switch (s) {
                    case "PAID" -> "-fx-text-fill: #22c55e; -fx-font-weight: bold;";
                    case "CANCELLED" -> "-fx-text-fill: #ef4444; -fx-font-weight: bold;";
                    default -> "-fx-text-fill: #f59e0b; -fx-font-weight: bold;";
                });
            }
        });

        billTable.getColumns().addAll(idCol, guestCol, dateCol, nightsCol, amtCol, payCol, paidCol);
        billTable.setPlaceholder(new Label("No billing records.") {{ setStyle("-fx-text-fill:#64748b;"); }});
        billTable.getSelectionModel().selectedItemProperty().addListener(
            (o, ov, nv) -> showBillDetail(nv));

        panel.getChildren().addAll(new Label("Billing Records") {{ getStyleClass().add("section-header"); }},
                filterRow, billTable);

        return panel;
    }

    private VBox buildDetailPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(8));
        panel.setStyle("-fx-background-color: transparent;");

        Label lbl = new Label("Bill Detail");
        lbl.getStyleClass().add("section-header");

        billDetailArea = new TextArea();
        billDetailArea.setEditable(false);
        billDetailArea.setWrapText(false);
        billDetailArea.setPromptText("Select a billing record to view the bill...");
        billDetailArea.setPrefHeight(600);
        VBox.setVgrow(billDetailArea, Priority.ALWAYS);

        billDetailArea.setStyle(
            "-fx-control-inner-background: #ffffff;" +
            "-fx-text-fill: #000000;" +
            "-fx-font-family: 'Consolas';" +
            "-fx-font-size: 13px;" +
            "-fx-border-color: #475569;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );

        VBox billCard = new VBox();
        VBox.setVgrow(billCard, Priority.ALWAYS);

        billCard.setStyle(
            "-fx-background-color: #1e293b;" +
            "-fx-padding: 10;" +
            "-fx-background-radius: 10;"
        );

        billCard.getChildren().add(billDetailArea);

        Button saveBtn = new Button("💾  Save Bill to File");
        saveBtn.getStyleClass().add("btn-secondary");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> {
            Booking sel = billTable.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            String path = MainApp.billService.generateBill(sel);
            com.hotel.util.AlertUtil.showInfo("Saved", "Bill saved to:\n" + path);
        });

        panel.getChildren().addAll(lbl, billCard, saveBtn);
        return panel;
    }

    private void showBillDetail(Booking booking) {
        if (booking == null) {
            billDetailArea.clear();
            return;
        }
        billDetailArea.setText(MainApp.billService.getBillAsString(booking));
    }

    private void applyFilter(String filter) {
        List<Booking> all = MainApp.bookingService.getAllBookings();
        List<Booking> filtered = switch (filter) {
            case "Paid" -> all.stream().filter(Booking::isPaid).toList();
            case "Unpaid" -> all.stream().filter(b -> !b.isPaid() &&
                    b.getStatus() != Booking.BookingStatus.CANCELLED).toList();
            case "Cancelled" -> all.stream().filter(b ->
                    b.getStatus() == Booking.BookingStatus.CANCELLED).toList();
            default -> all;
        };
        billTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void searchBills(String query) {
        if (query == null || query.isBlank()) {
            billTable.setItems(FXCollections.observableArrayList(MainApp.bookingService.getAllBookings()));
            return;
        }
        String lower = query.toLowerCase();
        billTable.setItems(FXCollections.observableArrayList(
            MainApp.bookingService.getAllBookings().stream()
                .filter(b -> b.getBookingId().toLowerCase().contains(lower) ||
                             b.getGuest().getName().toLowerCase().contains(lower))
                .toList()
        ));
    }

    public void refresh() {
        List<Booking> all = MainApp.bookingService.getAllBookings();
        billTable.setItems(FXCollections.observableArrayList(all));

        double totalRevenue = MainApp.bookingService.getTotalRevenue();
        long paidCount = all.stream().filter(Booking::isPaid).count();
        long pendingCount = all.stream().filter(b -> !b.isPaid() &&
                b.getStatus() != Booking.BookingStatus.CANCELLED).count();
        double avg = paidCount > 0 ?
                all.stream().filter(Booking::isPaid)
                        .mapToDouble(Booking::getTotalAmount).average().orElse(0) : 0;

        totalRevenueLabel.setText(String.format("₹ %.0f", totalRevenue));
        paidCountLabel.setText(String.valueOf(paidCount));
        pendingCountLabel.setText(String.valueOf(pendingCount));
        avgBillLabel.setText(String.format("₹ %.0f", avg));
    }

    public javafx.scene.Node getView() {
        return root;
    }
}