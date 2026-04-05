package com.hotel.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.Map;

public class MainView {

    private final Stage stage;
    private BorderPane root;
    private StackPane contentHolder;
    private Map<String, Button> navButtons = new LinkedHashMap<>();

    // Pages (lazy-loaded)
    private DashboardView dashboardView;
    private RoomsView roomsView;
    private BookingView bookingView;
    private CheckoutView checkoutView;
    private GuestsView guestsView;
    private BillingView billingView;

    public MainView(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        root = new BorderPane();
        root.setLeft(buildSidebar());
        root.setTop(buildHeader());

        contentHolder = new StackPane();
        contentHolder.getStyleClass().add("content-area");
        root.setCenter(contentHolder);

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(
            getClass().getResource("/com/hotel/view/styles.css").toExternalForm()
        );

        stage.setTitle("🏨  Lime Tree Resort Management System");
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.show();

        // Show dashboard by default
        navigateTo("Dashboard");
    }

    private HBox buildHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("header-bar");
        header.setAlignment(Pos.CENTER_RIGHT);
        header.setSpacing(15);

        Label time = new Label();
        time.getStyleClass().add("text-muted");
        updateClock(time);

        // Auto-update clock
        javafx.animation.Timeline clock = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> updateClock(time))
        );
        clock.setCycleCount(javafx.animation.Animation.INDEFINITE);
        clock.play();

        Label version = new Label("v1.0  |  LIME TREE Resort");
        version.getStyleClass().add("text-muted");

        header.getChildren().addAll(time, new Label(" | ").getClass().cast(makeLabel("  |  ", "text-muted")), version);
        return header;
    }

    private Label makeLabel(String text, String styleClass) {
        Label l = new Label(text);
        l.getStyleClass().add(styleClass);
        return l;
    }

    private void updateClock(Label l) {
        l.setText(java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("EEE, dd MMM yyyy  HH:mm:ss")));
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.getStyleClass().add("sidebar");

        // Logo/Header
        VBox header = new VBox(4);
        header.getStyleClass().add("sidebar-header");
        header.setAlignment(Pos.CENTER);
        Label icon = new Label("🏨");
        icon.setStyle("-fx-font-size: 28px;");
        Label title = new Label("LIME TREE RESORT");
        title.getStyleClass().add("sidebar-title");
        Label sub = new Label("Management System");
        sub.getStyleClass().add("sidebar-subtitle");
        header.getChildren().addAll(icon, title, sub);
        header.setPadding(new Insets(20, 15, 20, 15));

        sidebar.getChildren().add(header);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #334155;");
        sidebar.getChildren().add(sep);

        // Nav items: label -> emoji + name
        String[][] navItems = {
            {"Dashboard",  "📊  Dashboard"},
            {"Rooms",      "🛏️  Rooms"},
            {"Booking",    "📋  New Booking"},
            {"Checkout",   "🔑  Check-Out"},
            {"Guests",     "👤  Guests"},
            {"Billing",    "💰  Billing"}
        };

        VBox navArea = new VBox(2);
        navArea.setPadding(new Insets(10, 0, 10, 0));

        for (String[] item : navItems) {
            Button btn = new Button(item[1]);
            btn.getStyleClass().add("nav-button");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> navigateTo(item[0]));
            navButtons.put(item[0], btn);
            navArea.getChildren().add(btn);
        }

        sidebar.getChildren().add(navArea);

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        // Bottom info
        VBox bottom = new VBox(4);
        bottom.setPadding(new Insets(15));
        bottom.setAlignment(Pos.CENTER);
        Label info = new Label("Data stored in ~/.hotelms");
        info.getStyleClass().add("text-muted");
        info.setStyle("-fx-font-size: 10px;");
        bottom.getChildren().add(info);
        sidebar.getChildren().add(bottom);

        return sidebar;
    }

    private void navigateTo(String page) {
        // Update button states
        navButtons.forEach((key, btn) -> {
            if (key.equals(page)) {
                btn.getStyleClass().add("active");
            } else {
                btn.getStyleClass().remove("active");
            }
        });

        // Load page
        javafx.scene.Node view = switch (page) {
            case "Dashboard" -> {
                if (dashboardView == null) dashboardView = new DashboardView(this);
                dashboardView.refresh();
                yield dashboardView.getView();
            }
            case "Rooms" -> {
                if (roomsView == null) roomsView = new RoomsView();
                roomsView.refresh();
                yield roomsView.getView();
            }
            case "Booking" -> {
                if (bookingView == null) bookingView = new BookingView(this);
                yield bookingView.getView();
            }
            case "Checkout" -> {
                if (checkoutView == null) checkoutView = new CheckoutView(this);
                checkoutView.refresh();
                yield checkoutView.getView();
            }
            case "Guests" -> {
                if (guestsView == null) guestsView = new GuestsView();
                guestsView.refresh();
                yield guestsView.getView();
            }
            case "Billing" -> {
                if (billingView == null) billingView = new BillingView();
                billingView.refresh();
                yield billingView.getView();
            }
            default -> new StackPane(new Label("Coming Soon"));
        };

        contentHolder.getChildren().setAll(view);
    }

    public void navigateToBooking() { navigateTo("Booking"); }
    public void navigateToCheckout() { navigateTo("Checkout"); }
    public void navigateToDashboard() { navigateTo("Dashboard"); }
}
