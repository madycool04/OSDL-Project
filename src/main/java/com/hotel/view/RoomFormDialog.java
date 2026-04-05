package com.hotel.view;

import com.hotel.model.Room;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class RoomFormDialog extends Dialog<Room> {

    private final Room existing;

    private TextField roomNumberField;
    private ComboBox<Room.RoomType> typeCombo;
    private TextField priceField;
    private TextField floorField;
    private TextField maxOccField;
    private TextField descField;
    private CheckBox acCheck, wifiCheck, tvCheck;
    private ComboBox<Room.RoomStatus> statusCombo;

    public RoomFormDialog(Room existing) {
        this.existing = existing;
        setTitle(existing == null ? "Add New Room" : "Edit Room");
        setHeaderText(null);

        getDialogPane().setStyle("-fx-background-color: #1e293b;");
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.getStyleClass().add("btn-primary");
        okButton.setText(existing == null ? "Add Room" : "Save Changes");

        getDialogPane().setContent(buildForm());

        setResultConverter(bt -> {
            if (bt == ButtonType.OK) return buildRoom();
            return null;
        });
    }

    private GridPane buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #1e293b;");

        roomNumberField = new TextField();
        roomNumberField.setPromptText("e.g. 101");
        if (existing != null) { roomNumberField.setText(existing.getRoomNumber()); roomNumberField.setDisable(true); }

        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(Room.RoomType.values());
        typeCombo.setValue(existing != null ? existing.getRoomType() : Room.RoomType.SINGLE);

        priceField = new TextField(existing != null ? String.valueOf(existing.getPricePerNight()) : "1500");
        priceField.setPromptText("Price per night");

        floorField = new TextField(existing != null ? String.valueOf(existing.getFloor()) : "1");

        maxOccField = new TextField(existing != null ? String.valueOf(existing.getMaxOccupancy()) : "2");

        descField = new TextField(existing != null ? existing.getDescription() : "");
        descField.setPromptText("Room description");
        descField.setPrefWidth(280);

        acCheck   = new CheckBox("Air Conditioning");
        wifiCheck = new CheckBox("WiFi");
        tvCheck   = new CheckBox("Television");
        acCheck.setSelected(existing == null || existing.isHasAC());
        wifiCheck.setSelected(existing == null || existing.isHasWifi());
        tvCheck.setSelected(existing == null || existing.isHasTV());

        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(Room.RoomStatus.values());
        statusCombo.setValue(existing != null ? existing.getStatus() : Room.RoomStatus.AVAILABLE);

        // Layout
        addRow(grid, 0, "Room Number *", roomNumberField);
        addRow(grid, 1, "Room Type *",   typeCombo);
        addRow(grid, 2, "Price/Night (₹) *", priceField);
        addRow(grid, 3, "Floor",         floorField);
        addRow(grid, 4, "Max Occupancy", maxOccField);
        addRow(grid, 5, "Status",        statusCombo);
        addRow(grid, 6, "Description",   descField);

        Label amenLabel = new Label("Amenities");
        amenLabel.getStyleClass().add("field-label");
        HBox amenRow = new HBox(15, acCheck, wifiCheck, tvCheck);
        grid.add(amenLabel, 0, 7);
        grid.add(amenRow, 1, 7);

        return grid;
    }

    private void addRow(GridPane grid, int row, String labelText, javafx.scene.Node field) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("field-label");
        lbl.setMinWidth(140);
        grid.add(lbl, 0, row);
        grid.add(field, 1, row);
    }

    private Room buildRoom() {
        try {
            String num   = roomNumberField.getText().trim().toUpperCase();
            Room.RoomType type = typeCombo.getValue();
            double price = Double.parseDouble(priceField.getText().trim());
            int floor    = Integer.parseInt(floorField.getText().trim());

            Room room = existing != null ? existing : new Room(num, type, price, floor);
            if (existing != null) {
                room.setRoomType(type);
                room.setPricePerNight(price);
                room.setFloor(floor);
            }
            try { room.setMaxOccupancy(Integer.parseInt(maxOccField.getText().trim())); } catch (Exception ignored) {}
            room.setDescription(descField.getText().trim());
            room.setHasAC(acCheck.isSelected());
            room.setHasWifi(wifiCheck.isSelected());
            room.setHasTV(tvCheck.isSelected());
            room.setStatus(statusCombo.getValue());
            return room;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
