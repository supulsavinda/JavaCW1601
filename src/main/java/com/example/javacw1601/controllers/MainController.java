package com.example.javacw1601.controllers;

import com.example.javacw1601.backend.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
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

    private InventoryManager inventoryManager;
    private DealerManager dealerManager;
    private CartManager cartManager;
    private AuditLogger auditLogger;

    @FXML private TextField txtThreshold;
    @FXML private ListView<String> lowStockList;
    @FXML private Button btnRefresh;

    @FXML private TableView<Inventory> inventoryTable;
    @FXML private TableColumn<Inventory, String> colCode;
    @FXML private TableColumn<Inventory, String> colName;
    @FXML private TableColumn<Inventory, String> colBrand;
    @FXML private TableColumn<Inventory, Double> colPrice;
    @FXML private TableColumn<Inventory, Integer> colQuantity;
    @FXML private TableColumn<Inventory, String> colCategory;
    @FXML private TableColumn<Inventory, String> colDate;
    @FXML private TableColumn<Inventory, String> colImage;

    @FXML private Label lblTotalCount;
    @FXML private Label lblTotalValue;

    @FXML private ComboBox<String> cmbCategory;
    @FXML private TextField txtMinPrice;
    @FXML private TextField txtMaxPrice;
    @FXML private TextField txtSearchKeyword;
    @FXML private Button btnSearch;

    @FXML private TextField txtCode;
    @FXML private TextField txtName;
    @FXML private TextField txtBrand;
    @FXML private TextField txtPrice;
    @FXML private TextField txtQuantity;
    @FXML private TextField txtCategory;
    @FXML private TextField txtDate;
    @FXML private TextField txtImage;
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;

    @FXML private Button btnRandomDealers;
    @FXML private ListView<String> dealerList;

    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> colCartCode;
    @FXML private TableColumn<CartItem, String> colCartName;
    @FXML private TableColumn<CartItem, Integer> colCartQty;
    @FXML private TableColumn<CartItem, Double> colCartPrice;
    @FXML private TableColumn<CartItem, Double> colCartTotal;
    @FXML private Label lblCartTotal;
    @FXML private Button btnAddToCart;
    @FXML private Button btnRemoveFromCart;
    @FXML private Button btnCheckout;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String inventoryFile = "inventory_legacy.txt";
        String dealerFile = "dealers_legacy.txt";

        ArrayList<Inventory> items = LegacyDataHandler.loadItemsList(inventoryFile);
        inventoryManager = new InventoryManager(items);
        dealerManager = new DealerManager(LegacyDataHandler.loadDealersList(dealerFile));
        cartManager = new CartManager(inventoryManager);
        auditLogger = new AuditLogger();

        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colBrand.setCellValueFactory(new PropertyValueFactory<>("brand"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colImage.setCellValueFactory(new PropertyValueFactory<>("image"));

        colCartCode.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItem().getCode()));
        colCartName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItem().getName()));
        colCartQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCartPrice.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getItem().getPrice()).asObject());
        colCartTotal.setCellValueFactory(cellData -> {
            CartItem ci = cellData.getValue();
            return new SimpleDoubleProperty(ci.getItem().getPrice() * ci.getQuantity()).asObject();
        });

        cmbCategory.getItems().addAll("All", "Engine", "Electrical", "Bodywork", "Brakes");
        cmbCategory.setValue("All");

        txtThreshold.setText("5");
        inventoryManager.setLowStockThreshold(5);

        refreshInventoryTable();
        updateLowStockWarning();
        updateCartUI();
    }

    private void refreshInventoryTable() {
        ArrayList<Inventory> sorted = inventoryManager.getSortedItems();
        ObservableList<Inventory> data = FXCollections.observableArrayList(sorted);
        inventoryTable.setItems(data);
        lblTotalCount.setText("Total Items: " + inventoryManager.getTotalCount());
        lblTotalValue.setText("Total Value: Rs." + String.format("%.2f", inventoryManager.getTotalValue()));
    }

    private void updateLowStockWarning() {
        int threshold;
        try {
            threshold = Integer.parseInt(txtThreshold.getText());
            inventoryManager.setLowStockThreshold(threshold);
        } catch (NumberFormatException e) {
            threshold = inventoryManager.getLowStockThreshold();
            txtThreshold.setText(String.valueOf(threshold));
        }
        ArrayList<Inventory> low = inventoryManager.getLowStockItems();
        lowStockList.getItems().clear();
        for (Inventory inv : low) {
            lowStockList.getItems().add(inv.getCode() + " | " + inv.getName() + " | Qty: " + inv.getQuantity());
        }
    }

    private void updateCartUI() {
        ObservableList<CartItem> data = FXCollections.observableArrayList(cartManager.getCartItems());
        cartTable.setItems(data);
        lblCartTotal.setText("Total: Rs." + String.format("%.2f", cartManager.calculateTotal()));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleRefresh() {
        refreshInventoryTable();
        updateLowStockWarning();
    }

    @FXML
    private void handleAdd() {
        try {
            String code = txtCode.getText().trim();
            String name = txtName.getText().trim();
            String brand = txtBrand.getText().trim();
            double price = Double.parseDouble(txtPrice.getText().trim());
            int quantity = Integer.parseInt(txtQuantity.getText().trim());
            String category = txtCategory.getText().trim();
            String date = txtDate.getText().trim();
            String image = txtImage.getText().trim().isEmpty() ? "no_image.png" : txtImage.getText().trim();

            Inventory newItem = new Inventory(code, name, brand, price, quantity, category, date, image);
            if (inventoryManager.addItem(newItem)) {
                auditLogger.logAddItem(newItem);
                LegacyDataHandler.saveInventory("inventory_legacy.txt", inventoryManager.getAllItems());
                refreshInventoryTable();
                updateLowStockWarning();
                showAlert("Success", "Item added successfully.");
            } else {
                showAlert("Error", "Duplicate or invalid code: " + code);
            }
        } catch (Exception e) {
            showAlert("Error", "Invalid input: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        String code = txtCode.getText().trim();
        if (code.isEmpty()) {
            showAlert("Error", "Enter code to update.");
            return;
        }
        try {
            String name = txtName.getText().trim();
            String brand = txtBrand.getText().trim();
            double price = Double.parseDouble(txtPrice.getText().trim());
            int quantity = Integer.parseInt(txtQuantity.getText().trim());
            String category = txtCategory.getText().trim();
            String date = txtDate.getText().trim();
            String image = txtImage.getText().trim().isEmpty() ? "no_image.png" : txtImage.getText().trim();

            Inventory updated = new Inventory(code, name, brand, price, quantity, category, date, image);
            if (inventoryManager.updateItem(code, updated)) {
                auditLogger.logUpdateItem(code, updated);
                LegacyDataHandler.saveInventory("inventory_legacy.txt", inventoryManager.getAllItems());
                refreshInventoryTable();
                updateLowStockWarning();
                showAlert("Success", "Item updated.");
            } else {
                showAlert("Error", "Code not found: " + code);
            }
        } catch (Exception e) {
            showAlert("Error", "Invalid input: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Inventory selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Select an item to delete.");
            return;
        }
        String code = selected.getCode();
        if (inventoryManager.deleteItem(code)) {
            auditLogger.logDeleteItem(code);
            LegacyDataHandler.saveInventory("inventory_legacy.txt", inventoryManager.getAllItems());
            refreshInventoryTable();
            updateLowStockWarning();
            showAlert("Success", "Item deleted.");
        } else {
            showAlert("Error", "Failed to delete.");
        }
    }

    @FXML
    private void handleSearch() {
        String category = cmbCategory.getValue();
        double minPrice, maxPrice;
        try {
            minPrice = Double.parseDouble(txtMinPrice.getText().trim());
            maxPrice = Double.parseDouble(txtMaxPrice.getText().trim());
        } catch (NumberFormatException e) {
            minPrice = 0;
            maxPrice = Double.MAX_VALUE;
        }
        String keyword = txtSearchKeyword.getText().trim();

        ArrayList<Inventory> results = inventoryManager.search(category, minPrice, maxPrice, keyword);
        ObservableList<Inventory> data = FXCollections.observableArrayList(results);
        inventoryTable.setItems(data);
    }

    @FXML
    private void handleRandomDealers() {
        try {
            ArrayList<Dealers> selected = dealerManager.select4Random();
            dealerList.getItems().clear();
            for (Dealers d : selected) {
                dealerList.getItems().add(d.getCode() + " | " + d.getName() + " | " + d.getLocation());
            }
        } catch (Exception e) {
            showAlert("Error", "Not enough dealers.");
        }
    }

    @FXML
    private void handleAddToCart() {
        Inventory selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Select an item from the table.");
            return;
        }
        try {
            int qty = Integer.parseInt(txtQuantity.getText().trim());
            if (cartManager.addToCart(selected.getCode(), qty)) {
                updateCartUI();
                showAlert("Success", "Added to cart.");
            } else {
                showAlert("Error", "Cannot add. Check stock or quantity.");
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Enter valid quantity.");
        }
    }

    @FXML
    private void handleRemoveFromCart() {
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Select a cart item to remove.");
            return;
        }
        cartManager.removeFromCart(selected.getItem().getCode());
        updateCartUI();
    }

    @FXML
    private void handleCheckout() {
        double total = cartManager.checkout();
        if (total > 0) {
            updateCartUI();
            refreshInventoryTable();
            updateLowStockWarning();
            showAlert("Checkout", "Total paid: Rs." + String.format("%.2f", total));
        } else {
            showAlert("Error", "Cart is empty.");
        }
    }
}
