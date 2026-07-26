package com.example.javacw1601.controllers;

import com.example.javacw1601.backend.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private static final String INVENTORY_FILE = "inventory_legacy.txt";
    private static final String DEALER_FILE = "dealers_legacy.txt";

    private InventoryManager inventoryManager;
    private DealerManager dealerManager;
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

    @FXML
    private void handleRefresh() {
        refreshInventoryTable();
    }

    @FXML
    private void handleOpenAddItem() {
    }

    @FXML
    private void handleOpenUpdateItem() {
    }

    @FXML
    private void handleDeleteItem() {
    }

    @FXML
    private void handleOpenDealer() {
    }

    @FXML
    private void handleOpenCart() {
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }
}