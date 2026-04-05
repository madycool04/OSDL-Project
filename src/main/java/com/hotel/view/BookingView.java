package com.hotel.view;

import com.hotel.MainApp;
import com.hotel.model.Booking;
import com.hotel.model.Guest;
import com.hotel.model.Room;
import com.hotel.util.AlertUtil;
import com.hotel.util.IdGeneratorUtil;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BookingView {

    private final MainView mainView;
    private ScrollPane root;

    // Guest fields
    private TextField guestNameField, guestPhoneField, guestEmailField;
    private TextField guestAddressField, idProofNumberField;
    private ComboBox<String> idProofTypeCombo;

    // Booking fields
    private ComboBox<Room.RoomType> roomTypeCombo;
    private ComboBox<Room> roomCombo;
    private DatePicker checkInPicker, checkOutPicker;
    private Spinner<Integer> guestCountSpinner;
    private TextArea specialRequestsArea;
    private ComboBox<String> paymentModeCombo;

    // Bill preview
    private Label roomChargesLabel, serviceChargesLabel, taxLabel, totalLabel, nightsLabel;

    public BookingView(MainView mainView) {
        this.mainView = mainView;
        build();
    }

    private void build() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(0));

        // Title
        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("New Booking");
        title.getStyleClass().add("page-title");
        container.getChildren().add(titleRow);
        titleRow.getChildren().add(title);

        // Two-column layout
        HBox columns = new HBox(20);
        columns.setAlignment(Pos.TOP_LEFT);

        VBox leftCol  = new VBox(15);
        VBox rightCol = new VBox(15);
        HBox.setHgrow(leftCol, Priority.ALWAYS);
        HBox.setHgrow(rightCol, Priority.ALWAYS);

        leftCol.getChildren().addAll(buildGuestCard(), buildBookingCard());
        rightCol.getChildren().addAll(buildRoomSelectionCard(), buildBillPreviewCard(), buildActionCard());

        columns.getChildren().addAll(leftCol, rightCol);
        container.getChildren().add(columns);

        root = new ScrollPane(container);
        root.setFitToWidth(true);
        root.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        root.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    private VBox buildGuestCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");

        Label cardTitle = new Label("👤  Guest Information");
        cardTitle.getStyleClass().add("card-title");

        guestNameField    = field("Full Name *");
        guestPhoneField   = field("Phone Number *");
        guestEmailField   = field("Email Address");
        guestAddressField = field("Address");
        idProofNumberField = field("ID Proof Number *");

        idProofTypeCombo = new ComboBox<>(FXCollections.observableArrayList(
            "Aadhaar Card", "PAN Card", "Passport", "Voter ID", "Driving License"));
        idProofTypeCombo.setValue("Aadhaar Card");

        // Search existing guest by phone
        Button searchBtn = new Button("🔍 Find Existing Guest");
        searchBtn.getStyleClass().add("btn-secondary");
        searchBtn.setStyle("-fx-padding: 7 14; -fx-font-size: 12px;");
        searchBtn.setOnAction(e -> searchExistingGuest());

        card.getChildren().addAll(
            cardTitle,
            searchBtn,
            new Separator(),
            row("Full Name *",      guestNameField),
            row("Phone *",         guestPhoneField),
            row("Email",           guestEmailField),
            row("Address",         guestAddressField),
            row("ID Proof Type",   idProofTypeCombo),
            row("ID Proof No. *",  idProofNumberField)
        );
        return card;
    }

    private VBox buildBookingCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");

        Label cardTitle = new Label("📋  Booking Details");
        cardTitle.getStyleClass().add("card-title");

        checkInPicker  = new DatePicker(LocalDate.now());
        checkOutPicker = new DatePicker(LocalDate.now().plusDays(1));
        checkInPicker.setStyle("-fx-pref-width: 200px;");
        checkOutPicker.setStyle("-fx-pref-width: 200px;");

        checkInPicker.setOnAction(e -> updateBillPreview());
        checkOutPicker.setOnAction(e -> updateBillPreview());

        guestCountSpinner = new Spinner<>(1, 10, 1);
        guestCountSpinner.setEditable(true);
        guestCountSpinner.setPrefWidth(120);

        specialRequestsArea = new TextArea();
specialRequestsArea.setPromptText("Any special requests? (optional)");
specialRequestsArea.setPrefRowCount(3);
specialRequestsArea.setWrapText(true);
specialRequestsArea.setStyle(
    "-fx-control-inner-background: #0f172a;" +
    "-fx-text-fill: #e2e8f0;" +
    "-fx-prompt-text-fill: #64748b;" +
    "-fx-border-color: #334155;" +
    "-fx-background-radius: 8;" +
    "-fx-border-radius: 8;" +
    "-fx-font-size: 13px;"
);

        paymentModeCombo = new ComboBox<>(FXCollections.observableArrayList(
            "Cash", "Credit Card", "Debit Card", "UPI", "Net Banking", "Cheque"));
        paymentModeCombo.setValue("Cash");

        card.getChildren().addAll(
            cardTitle,
            row("Check-In Date *",  checkInPicker),
            row("Check-Out Date *", checkOutPicker),
            row("No. of Guests",    guestCountSpinner),
            row("Payment Mode",     paymentModeCombo),
            new Label("Special Requests:") {{ getStyleClass().add("field-label"); }},
            specialRequestsArea
        );
        return card;
    }

    private VBox buildRoomSelectionCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");

        Label cardTitle = new Label("🛏️  Room Selection");
        cardTitle.getStyleClass().add("card-title");

        roomTypeCombo = new ComboBox<>(FXCollections.observableArrayList(Room.RoomType.values()));
        roomTypeCombo.setPromptText("Select Room Type");
        roomTypeCombo.setOnAction(e -> loadAvailableRooms());

        roomCombo = new ComboBox<>();
        roomCombo.setPromptText("Select Room");
        roomCombo.setMaxWidth(Double.MAX_VALUE);
        roomCombo.setOnAction(e -> updateBillPreview());
        roomCombo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Room r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) setText(null);
                else setText(r.getRoomNumber() + " – " + r.getRoomType() + " – ₹" + r.getPricePerNight() + "/night");
            }
        });
        roomCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Room r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) setText(null);
                else setText(r.getRoomNumber() + " – ₹" + r.getPricePerNight() + "/night");
            }
        });

        card.getChildren().addAll(
            cardTitle,
            row("Room Type", roomTypeCombo),
            row("Room",      roomCombo)
        );
        return card;
    }

    private VBox buildBillPreviewCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        Label cardTitle = new Label("💰  Bill Preview");
        cardTitle.getStyleClass().add("card-title");

        nightsLabel        = previewLabel("–");
        roomChargesLabel   = previewLabel("₹ 0.00");
        serviceChargesLabel= previewLabel("₹ 0.00");
        taxLabel           = previewLabel("₹ 0.00");
        totalLabel         = new Label("₹ 0.00");
        totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f59e0b;");

        Separator sep = new Separator();

        card.getChildren().addAll(
            cardTitle,
            billRow("Nights",                    nightsLabel),
            billRow("Room Charges",              roomChargesLabel),
            billRow("Service Charges (10%)",     serviceChargesLabel),
            billRow("GST (18%)",                 taxLabel),
            sep,
            billRow("TOTAL AMOUNT",              totalLabel)
        );
        return card;
    }

    private VBox buildActionCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        Button confirmBtn = new Button("✅  Confirm Booking");
        confirmBtn.getStyleClass().add("btn-success");
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setStyle(confirmBtn.getStyle() + "-fx-font-size: 14px; -fx-padding: 13;");
        confirmBtn.setOnAction(e -> confirmBooking());

        Button clearBtn = new Button("🔄  Clear Form");
        clearBtn.getStyleClass().add("btn-secondary");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> clearForm());

        card.getChildren().addAll(confirmBtn, clearBtn);
        return card;
    }

    // ---- Helpers ----

    private void loadAvailableRooms() {
        Room.RoomType type = roomTypeCombo.getValue();
        if (type == null) return;
        List<Room> available = MainApp.roomService.getAvailableRoomsByType(type);
        roomCombo.setItems(FXCollections.observableArrayList(available));
        if (!available.isEmpty()) roomCombo.setValue(available.get(0));
        updateBillPreview();
    }

    private void updateBillPreview() {
        Room room = roomCombo.getValue();
        LocalDate in = checkInPicker.getValue();
        LocalDate out = checkOutPicker.getValue();
        if (room == null || in == null || out == null || !out.isAfter(in)) {
            return;
        }
        long nights = ChronoUnit.DAYS.between(in, out);
        double roomCharges   = room.getPricePerNight() * nights;
        double serviceCharge = roomCharges * 0.10;
        double tax           = (roomCharges + serviceCharge) * 0.18;
        double total         = roomCharges + serviceCharge + tax;

        nightsLabel.setText(nights + " night(s)");
        roomChargesLabel.setText(String.format("₹ %.2f", roomCharges));
        serviceChargesLabel.setText(String.format("₹ %.2f", serviceCharge));
        taxLabel.setText(String.format("₹ %.2f", tax));
        totalLabel.setText(String.format("₹ %.2f", total));
    }

    private void confirmBooking() {
        // Validation
        if (guestNameField.getText().isBlank()) {
            AlertUtil.showError("Validation Error", "Guest name is required."); return;
        }
        if (guestPhoneField.getText().isBlank()) {
            AlertUtil.showError("Validation Error", "Phone number is required."); return;
        }
        if (idProofNumberField.getText().isBlank()) {
            AlertUtil.showError("Validation Error", "ID proof number is required."); return;
        }
        if (roomCombo.getValue() == null) {
            AlertUtil.showError("Validation Error", "Please select a room."); return;
        }
        LocalDate in = checkInPicker.getValue();
        LocalDate out = checkOutPicker.getValue();
        if (in == null || out == null || !out.isAfter(in)) {
            AlertUtil.showError("Validation Error", "Check-out date must be after check-in date."); return;
        }

        // Build guest
        Guest guest = new Guest(
            IdGeneratorUtil.generateGuestId(),
            guestNameField.getText().trim(),
            guestPhoneField.getText().trim(),
            guestEmailField.getText().trim(),
            guestAddressField.getText().trim(),
            idProofTypeCombo.getValue(),
            idProofNumberField.getText().trim()
        );

        // Create booking
        Booking booking = MainApp.bookingService.createBooking(
            guest, roomCombo.getValue(), in, out,
            guestCountSpinner.getValue(),
            specialRequestsArea.getText().trim()
        );
        booking.setPaymentMode(paymentModeCombo.getValue());

        String msg = "Booking confirmed!\n\n" +
            "Booking ID: " + booking.getBookingId() + "\n" +
            "Guest: " + guest.getName() + "\n" +
            "Room: " + booking.getRoom().getRoomNumber() + "\n" +
            "Total: ₹" + String.format("%.2f", booking.getTotalAmount());

        AlertUtil.showInfo("Booking Confirmed", msg);
        clearForm();
        mainView.navigateToDashboard();
    }

    private void searchExistingGuest() {
        String phone = guestPhoneField.getText().trim();
        if (phone.isBlank()) {
            AlertUtil.showWarning("Search", "Enter a phone number first."); return;
        }
        MainApp.guestService.findByPhone(phone).ifPresentOrElse(g -> {
            guestNameField.setText(g.getName());
            guestEmailField.setText(g.getEmail() != null ? g.getEmail() : "");
            guestAddressField.setText(g.getAddress() != null ? g.getAddress() : "");
            idProofTypeCombo.setValue(g.getIdProofType());
            idProofNumberField.setText(g.getIdProofNumber());
            AlertUtil.showInfo("Guest Found", "Guest details loaded: " + g.getName());
        }, () -> AlertUtil.showInfo("Not Found", "No guest found with that phone number."));
    }

    private void clearForm() {
        guestNameField.clear(); guestPhoneField.clear(); guestEmailField.clear();
        guestAddressField.clear(); idProofNumberField.clear();
        roomTypeCombo.setValue(null); roomCombo.setValue(null);
        checkInPicker.setValue(LocalDate.now());
        checkOutPicker.setValue(LocalDate.now().plusDays(1));
        guestCountSpinner.getValueFactory().setValue(1);
        specialRequestsArea.clear();
        paymentModeCombo.setValue("Cash");
        nightsLabel.setText("–");
        roomChargesLabel.setText("₹ 0.00");
        serviceChargesLabel.setText("₹ 0.00");
        taxLabel.setText("₹ 0.00");
        totalLabel.setText("₹ 0.00");
    }

    private TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        return tf;
    }

    private HBox row(String label, javafx.scene.Node field) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("field-label");
        lbl.setMinWidth(130);
        if (field instanceof TextField tf) tf.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(field, Priority.ALWAYS);
        row.getChildren().addAll(lbl, field);
        return row;
    }

    private HBox billRow(String label, javafx.scene.Node valueNode) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        row.getChildren().addAll(lbl, sp, valueNode);
        return row;
    }

    private Label previewLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 13px;");
        return l;
    }

    public javafx.scene.Node getView() { return root; }
}
