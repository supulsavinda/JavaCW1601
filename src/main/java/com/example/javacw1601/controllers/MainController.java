package com.example.javacw1601.controllers;

import com.example.javacw1601.backend.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private static final String INVENTORY_LEGACY_FILE = "inventory_legacy.txt";
    private static final String DEALER_LEGACY_FILE = "dealers_legacy.txt";
    private static final String INVENTORY_DATA_FILE = "inventory_data.txt";

    private InventoryManager inventoryManager;
    private DealerManager dealerManager;
    private CartManager cartManager;
    private AuditLogger auditLogger;

    @FXML private StackPane contentArea;
    @FXML private TableView<Inventory> lowStockTable;
    @FXML private TableColumn<Inventory, String> colCode;
    @FXML private TableColumn<Inventory, String> colName;
    @FXML private TableColumn<Inventory, String> colBrand;
    @FXML private TableColumn<Inventory, Double> colPrice;
    @FXML private TableColumn<Inventory, String> colCategory;
    @FXML private TableColumn<Inventory, Integer> colQuantity;
    @FXML private TableColumn<Inventory, String> colImage;

    @FXML private Label lblTotalCount;
    @FXML private Label lblTotalValue;
    @FXML private Label lblLowStockCount;
    @FXML private TextField txtThreshold;

    private Parent dashboardContent;
    private Parent inventoryView;
    private Parent dealerView;
    private Parent cartView;
    private Parent auditLogView;
    private AuditLogController auditLogController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ArrayList<Inventory> items;
        java.io.File savedDataFile = new java.io.File(INVENTORY_DATA_FILE);
        if (savedDataFile.exists()) {
            items = LegacyDataHandler.loadInventoryData(INVENTORY_DATA_FILE);
        } else {
            items = LegacyDataHandler.loadItemsList(INVENTORY_LEGACY_FILE);
            LegacyDataHandler.saveInventory(INVENTORY_DATA_FILE, items);
        }

        ArrayList<Dealers> dealers = LegacyDataHandler.loadDealersList(DEALER_LEGACY_FILE);

        inventoryManager = new InventoryManager(items);
        dealerManager = new DealerManager(dealers);
        cartManager = new CartManager(inventoryManager);
        auditLogger = new AuditLogger();

        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colBrand.setCellValueFactory(new PropertyValueFactory<>("brand"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        colPrice.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
        colPrice.setCellFactory(col -> new TableCell<Inventory, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("Rs. %.2f", price));
            }
        });

        colImage.setCellValueFactory(new PropertyValueFactory<>("image"));
        colImage.setCellFactory(col -> new TableCell<Inventory, String>() {
            private final ImageView imageView = new ImageView();
            @Override
            protected void updateItem(String imageFilename, boolean empty) {
                super.updateItem(imageFilename, empty);
                if (empty || imageFilename == null || imageFilename.isEmpty()) {
                    setGraphic(null);
                    setText(null);
                } else {
                    try {
                        Image image = new Image("file:images/" + imageFilename);
                        imageView.setImage(image);
                        imageView.setFitWidth(60);
                        imageView.setFitHeight(60);
                        imageView.setPreserveRatio(true);
                        setGraphic(imageView);
                        setText(null);
                    } catch (Exception e) {
                        setGraphic(null);
                        setText("No image");
                    }
                }
            }
        });

        dashboardContent = (Parent) contentArea.getChildren().get(0);
        txtThreshold.setText(String.valueOf(inventoryManager.getLowStockThreshold()));
        refreshDashboard();
        loadOtherViews();
    }

    private void loadOtherViews() {
        try {
            FXMLLoader inventoryLoader = new FXMLLoader(getClass().getResource("/com/example/javacw1601/inventory-view.fxml"));
            inventoryView = inventoryLoader.load();
            InventoryController invCtrl = inventoryLoader.getController();
            invCtrl.init(inventoryManager, auditLogger, INVENTORY_DATA_FILE, this::refreshDashboard);

            FXMLLoader dealerLoader = new FXMLLoader(getClass().getResource("/com/example/javacw1601/dealer-view.fxml"));
            dealerView = dealerLoader.load();
            DealerController delCtrl = dealerLoader.getController();
            delCtrl.init(dealerManager);

            FXMLLoader cartLoader = new FXMLLoader(getClass().getResource("/com/example/javacw1601/cart-view.fxml"));
            cartView = cartLoader.load();
            CartController cartCtrl = cartLoader.getController();
            cartCtrl.init(cartManager, inventoryManager, INVENTORY_DATA_FILE, this::refreshDashboard);

            FXMLLoader auditLoader = new FXMLLoader(getClass().getResource("/com/example/javacw1601/audit-log-view.fxml"));
            auditLogView = auditLoader.load();
            auditLogController = auditLoader.getController();
            auditLogController.init(AuditLogger.LOG_FILE);

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load views: " + e.getMessage());
        }
    }

    private void refreshDashboard() {
        lblTotalCount.setText(String.valueOf(inventoryManager.getTotalCount()));
        lblTotalValue.setText(String.format("Rs. %.2f", inventoryManager.getTotalValue()));
        ArrayList<Inventory> lowItems = inventoryManager.getLowStockItems();
        ObservableList<Inventory> data = FXCollections.observableArrayList(lowItems);
        lowStockTable.setItems(data);
        lblLowStockCount.setText(String.valueOf(lowItems.size()));
    }

    private void showView(Parent view) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
    }

    private void showDashboardView() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(dashboardContent);
        refreshDashboard();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    @FXML
    private void handleApplyThreshold() {
        try {
            int newThreshold = Integer.parseInt(txtThreshold.getText().trim());
            if (newThreshold < 0) throw new NumberFormatException();
            inventoryManager.setLowStockThreshold(newThreshold);
            refreshDashboard();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Threshold", "Please enter a non-negative integer.");
        }
    }

    @FXML
    private void handleDashboard() {
        showDashboardView();
    }

    @FXML
    private void handleOpenFullInventory() {
        if (inventoryView != null) {
            showView(inventoryView);
        }
    }

    @FXML
    private void handleOpenDealer() {
        if (dealerView != null) {
            showView(dealerView);
        }
    }

    @FXML
    private void handleOpenCart() {
        if (cartView != null) {
            showView(cartView);
        }
    }

    @FXML
    private void handleOpenAuditLog() {
        if (auditLogView != null) {
            auditLogController.refresh();
            showView(auditLogView);
        }
    }

    @FXML
    private void handleExit() {
        if (showConfirm("Exit", "Are you sure you want to exit?")) {
            Platform.exit();
        }
    }
}