package com.hotel.view;

import com.hotel.MainApp;
import com.hotel.model.Room;
import com.hotel.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class RoomsView {

    private BorderPane root;
    private TableView<Room> table;
    private TextField searchField;

    public RoomsView() {
        build();
    }

    private void build() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");

        // Top bar
        VBox top = new VBox(15);
        top.setPadding(new Insets(0, 0, 15, 0));

        HBox titleRow = new HBox(15);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Room Management");
        title.getStyleClass().add("page-title");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button addBtn = new Button("➕  Add Room");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> showAddRoomDialog());
        titleRow.getChildren().addAll(title, spacer, addBtn);

        // Search + filter
        HBox filterRow = new HBox(10);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        searchField = new TextField();
        searchField.setPromptText("🔍  Search by room number...");
        searchField.setPrefWidth(250);
        searchField.textProperty().addListener((o, ov, nv) -> filterTable(nv));

        ComboBox<String> typeFilter = new ComboBox<>(
            FXCollections.observableArrayList("All Types", "SINGLE", "DOUBLE", "SUITE", "DELUXE"));
        typeFilter.setValue("All Types");
        typeFilter.setOnAction(e -> applyFilters(searchField.getText(), typeFilter.getValue()));

        ComboBox<String> statusFilter = new ComboBox<>(
            FXCollections.observableArrayList("All Status", "AVAILABLE", "OCCUPIED", "MAINTENANCE"));
        statusFilter.setValue("All Status");
        statusFilter.setOnAction(e -> applyFilters(searchField.getText(), typeFilter.getValue()));

        filterRow.getChildren().addAll(searchField, new Label("Type:"), typeFilter,
                                       new Label("Status:"), statusFilter);
        top.getChildren().addAll(titleRow, filterRow);
        root.setTop(top);

        // Table
        table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setTableMenuButtonVisible(true);

        TableColumn<Room, String> numCol    = col("Room No",   140, r -> r.getRoomNumber());
        TableColumn<Room, String> typeCol   = col("Type",      100, r -> r.getRoomType().toString());
        TableColumn<Room, String> floorCol  = col("Floor",      70, r -> "Floor " + r.getFloor());
        TableColumn<Room, String> priceCol  = col("Price/Night",120, r -> String.format("₹ %.2f", r.getPricePerNight()));
        TableColumn<Room, String> occCol    = col("Max Occ.",    90, r -> r.getMaxOccupancy() + " persons");
        TableColumn<Room, String> amenCol   = col("Amenities",  180, r -> buildAmenities(r));
        TableColumn<Room, String> descCol   = col("Description",250, r -> r.getDescription());

        TableColumn<Room, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(120);
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getStatus().toString()));
        statusCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); return; }
                Label badge = new Label(s);
                badge.getStyleClass().add(switch (s) {
                    case "AVAILABLE"   -> "badge-available";
                    case "OCCUPIED"    -> "badge-occupied";
                    case "MAINTENANCE" -> "badge-maintenance";
                    default            -> "badge-available";
                });
                setGraphic(badge);
                setText(null);
            }
        });

        // Actions column
        TableColumn<Room, Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(160);
        actCol.setCellFactory(c -> new TableCell<>() {
            final Button editBtn   = new Button("Edit");
            final Button deleteBtn = new Button("Delete");
            { editBtn.getStyleClass().add("btn-secondary");
              deleteBtn.getStyleClass().add("btn-danger");
              editBtn.setStyle("-fx-padding: 4 10; -fx-font-size: 11px;");
              deleteBtn.setStyle("-fx-padding: 4 10; -fx-font-size: 11px;"); }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                Room room = getTableView().getItems().get(getIndex());
                editBtn.setOnAction(e -> showEditRoomDialog(room));
                deleteBtn.setOnAction(e -> deleteRoom(room));
                HBox box = new HBox(6, editBtn, deleteBtn);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });

        table.getColumns().addAll(numCol, typeCol, floorCol, priceCol,
                                  occCol, amenCol, statusCol, actCol, descCol);

        Label placeholder = new Label("No rooms found.");
        placeholder.setStyle("-fx-text-fill: #64748b;");
        table.setPlaceholder(placeholder);

        VBox tableCard = new VBox(table);
        tableCard.getStyleClass().add("card");
        tableCard.setPadding(new Insets(15));
        root.setCenter(tableCard);
    }

    private String buildAmenities(Room r) {
        StringBuilder sb = new StringBuilder();
        if (r.isHasAC())   sb.append("❄️AC ");
        if (r.isHasWifi()) sb.append("📶WiFi ");
        if (r.isHasTV())   sb.append("📺TV");
        return sb.toString().trim();
    }

    @SuppressWarnings("unchecked")
    private <T> TableColumn<Room, T> col(String header, double width,
                                          java.util.function.Function<Room, T> extractor) {
        TableColumn<Room, T> col = new TableColumn<>(header);
        col.setPrefWidth(width);
        col.setCellValueFactory(d -> {
            T val = extractor.apply(d.getValue());
            if (val instanceof String s)
                return (javafx.beans.value.ObservableValue<T>) new javafx.beans.property.SimpleStringProperty(s);
            return new javafx.beans.property.SimpleObjectProperty<>(val);
        });
        return col;
    }

    public void refresh() {
        table.setItems(FXCollections.observableArrayList(MainApp.roomService.getAllRooms()));
    }

    private void filterTable(String query) {
        if (query == null || query.isBlank()) {
            refresh(); return;
        }
        table.setItems(FXCollections.observableArrayList(
            MainApp.roomService.getAllRooms().stream()
                .filter(r -> r.getRoomNumber().toLowerCase().contains(query.toLowerCase()))
                .toList()
        ));
    }

    private void applyFilters(String search, String type) {
        table.setItems(FXCollections.observableArrayList(
            MainApp.roomService.getAllRooms().stream()
                .filter(r -> search == null || search.isBlank() ||
                             r.getRoomNumber().toLowerCase().contains(search.toLowerCase()))
                .filter(r -> type == null || type.equals("All Types") ||
                             r.getRoomType().toString().equals(type))
                .toList()
        ));
    }

    private void showAddRoomDialog() {
        RoomFormDialog dlg = new RoomFormDialog(null);
        dlg.showAndWait().ifPresent(room -> {
            if (MainApp.roomService.roomNumberExists(room.getRoomNumber())) {
                AlertUtil.showError("Duplicate Room", "Room " + room.getRoomNumber() + " already exists.");
                return;
            }
            MainApp.roomService.addRoom(room);
            refresh();
            AlertUtil.showInfo("Success", "Room " + room.getRoomNumber() + " added successfully.");
        });
    }

    private void showEditRoomDialog(Room room) {
        RoomFormDialog dlg = new RoomFormDialog(room);
        dlg.showAndWait().ifPresent(updated -> {
            MainApp.roomService.updateRoom(updated);
            refresh();
            AlertUtil.showInfo("Success", "Room updated successfully.");
        });
    }

    private void deleteRoom(Room room) {
        if (room.getStatus() == Room.RoomStatus.OCCUPIED) {
            AlertUtil.showError("Cannot Delete", "Room " + room.getRoomNumber() + " is currently occupied.");
            return;
        }
        if (AlertUtil.showConfirm("Delete Room", "Delete room " + room.getRoomNumber() + "? This cannot be undone.")) {
            MainApp.roomService.deleteRoom(room.getRoomNumber());
            refresh();
        }
    }

    public javafx.scene.Node getView() { return root; }
}
