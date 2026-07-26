package com.example.javacw1601.controllers;

import com.example.javacw1601.backend.*;
import com.example.javacw1601.controllers.AddItemController;
import com.example.javacw1601.controllers.UpdateItemController;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private static final String INVENTORY_FILE = "inventory_legacy.txt";
    private static final String DEALER_FILE = "dealers_legacy.txt";

    private InventoryManager inventoryManager;
    private DealerManager dealerManager;
    private CartManager cartManager;
    private AuditLogger auditLogger;

    @FXML private TableView<Inventory> inventoryTable;
    @FXML private TableColumn<Inventory, String> colCode;
    @FXML private TableColumn<Inventory, String> colName;
    @FXML private TableColumn<Inventory, String> colBrand;
    @FXML private TableColumn<Inventory, Double> colPrice;
    @FXML private TableColumn<Inventory, String> colCategory;
    @FXML private TableColumn<Inventory, String> colDate;
    @FXML private TableColumn<Inventory, Integer> colQuantity;
    @FXML private TableColumn<Inventory, Integer> colThreshold;

    @FXML private Label lblTotalCount;
    @FXML private Label lblTotalValue;
    @FXML private Label lblLowStockCount;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ArrayList<Inventory> items = LegacyDataHandler.loadItemsList(INVENTORY_FILE);
        ArrayList<Dealers> dealers = LegacyDataHandler.loadDealersList(DEALER_FILE);

        inventoryManager = new InventoryManager(items);
        dealerManager = new DealerManager(dealers);
        cartManager = new CartManager(inventoryManager);
        auditLogger = new AuditLogger();

        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colBrand.setCellValueFactory(new PropertyValueFactory<>("brand"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colThreshold.setCellValueFactory(new PropertyValueFactory<>("threshold"));

        colPrice.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
        colPrice.setCellFactory(col -> new TableCell<Inventory, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("Rs. %.2f", price));
            }
        });

        inventoryTable.setRowFactory(tv -> new TableRow<Inventory>() {
            @Override
            protected void updateItem(Inventory item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("low-stock-row");
                if (!empty && item != null && item.isLowStock()) {
                    getStyleClass().add("low-stock-row");
                }
            }
        });

        refreshInventoryTable();
    }

    private void refreshInventoryTable() {
        ArrayList<Inventory> sorted = inventoryManager.getSortedItems();
        ObservableList<Inventory> data = FXCollections.observableArrayList(sorted);
        inventoryTable.setItems(data);
        lblTotalCount.setText("Total Items: " + inventoryManager.getTotalCount());
        lblTotalValue.setText("Total Value: Rs. " + String.format("%.2f", inventoryManager.getTotalValue()));
        lblLowStockCount.setText("Low Stock: " + inventoryManager.getLowStockItems().size());
    }

    private Window getWindow() {
        return inventoryTable.getScene().getWindow();
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

    private Stage openModal(String fxml, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javacw1601/" + fxml));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(getWindow());
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.setUserData(loader);
        return stage;
    }

    @FXML
    private void handleRefresh() {
        refreshInventoryTable();
    }

    @FXML
    private void handleOpenAddItem() {
        try {
            Stage stage = openModal("add-item-view.fxml", "Add New Spare Part");
            FXMLLoader loader = (FXMLLoader) stage.getUserData();
            AddItemController controller = loader.getController();
            controller.init(inventoryManager, auditLogger, INVENTORY_FILE);
            stage.showAndWait();
            refreshInventoryTable();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open the Add Item window: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenUpdateItem() {
        Inventory selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item in the table to update.");
            return;
        }
        try {
            Stage stage = openModal("update-item-view.fxml", "Update Spare Part");
            FXMLLoader loader = (FXMLLoader) stage.getUserData();
            UpdateItemController controller = loader.getController();
            controller.init(inventoryManager, auditLogger, INVENTORY_FILE, selected);
            stage.showAndWait();
            refreshInventoryTable();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open the Update Item window: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteItem() {
        Inventory selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item in the table to delete.");
            return;
        }
        boolean confirmed = showConfirm("Delete Item",
                "Delete \"" + selected.getName() + "\" (" + selected.getCode() + ") from the inventory?");
        if (!confirmed) return;

        String code = selected.getCode();
        if (inventoryManager.deleteItem(code)) {
            auditLogger.logDeleteItem(code);
            LegacyDataHandler.saveInventory(INVENTORY_FILE, inventoryManager.getAllItems());
            refreshInventoryTable();
            showAlert(Alert.AlertType.INFORMATION, "Deleted", "Item removed from the inventory.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete item.");
        }
    }

    @FXML
    private void handleOpenDealer() {
        try {
            Stage stage = openModal("dealer-view.fxml", "Dealer Management");
            FXMLLoader loader = (FXMLLoader) stage.getUserData();
            DealerController controller = loader.getController();
            controller.init(dealerManager, auditLogger, DEALER_FILE);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open the Dealer window: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenCart() {
        try {
            Stage stage = openModal("cart-view.fxml", "Shopping Cart & Checkout");
            FXMLLoader loader = (FXMLLoader) stage.getUserData();
            CartController controller = loader.getController();
            controller.init(cartManager, inventoryManager, INVENTORY_FILE);
            stage.showAndWait();
            refreshInventoryTable();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open the Cart window: " + e.getMessage());
        }
    }

    @FXML
    private void handleExit() {
        if (showConfirm("Exit", "Are you sure you want to exit Malabe Tuk-Tuk Spares?")) {
            Platform.exit();
        }
    }
}