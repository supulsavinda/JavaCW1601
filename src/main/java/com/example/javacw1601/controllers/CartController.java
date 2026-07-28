package com.example.javacw1601.controllers;

import com.example.javacw1601.backend.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.ArrayList;

public class CartController {

    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> colCartCode;
    @FXML private TableColumn<CartItem, String> colCartName;
    @FXML private TableColumn<CartItem, Integer> colCartQty;
    @FXML private TableColumn<CartItem, Double> colCartPrice;
    @FXML private TableColumn<CartItem, Double> colCartTotal;

    @FXML private TextField txtCartItemCode;
    @FXML private TextField txtCartQuantity;
    @FXML private Label lblCartTotal;
    @FXML private Label lblCartError;

    private CartManager cartManager;
    private InventoryManager inventoryManager;
    private String dataFile;
    private Runnable onInventoryChanged;

    private ObservableList<CartItem> cartItemsObservable = FXCollections.observableArrayList();

    public void init(CartManager cartManager, InventoryManager inventoryManager, String dataFile, Runnable onInventoryChanged) {
        this.cartManager = cartManager;
        this.inventoryManager = inventoryManager;
        this.dataFile = dataFile;
        this.onInventoryChanged = onInventoryChanged;

        colCartCode.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getItem().getCode()));
        colCartName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getItem().getName()));
        colCartQty.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        colCartPrice.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getItem().getPrice()).asObject());
        colCartTotal.setCellValueFactory(cellData -> {
            double total = cellData.getValue().getQuantity() * cellData.getValue().getItem().getPrice();
            return new SimpleDoubleProperty(total).asObject();
        });

        refreshCartTable();
    }

    private void refreshCartTable() {
        cartItemsObservable.setAll(cartManager.getCartItems());
        cartTable.setItems(cartItemsObservable);
        updateTotal();
    }

    private void updateTotal() {
        double total = cartManager.calculateTotal();
        lblCartTotal.setText(String.format("Total: Rs. %.2f", total));
    }

    private void clearError() {
        lblCartError.setText("");
    }

    private void showError(String msg) {
        lblCartError.setText(msg);
    }

    @FXML
    private void handleAddToCart() {
        clearError();
        String code = txtCartItemCode.getText().trim();
        String qtyText = txtCartQuantity.getText().trim();

        if (code.isEmpty()) {
            showError("Please enter an item code.");
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(qtyText);
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError("Quantity must be a positive integer.");
            return;
        }

        if (cartManager.addToCart(code, qty)) {
            txtCartItemCode.clear();
            txtCartQuantity.clear();
            refreshCartTable();
        } else {
            showError("Could not add item. Either the code doesn't exist, or not enough stock.");
        }
    }

    @FXML
    private void handleRemoveFromCart() {
        clearError();
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select an item to remove.");
            return;
        }
        String code = selected.getItem().getCode();
        if (cartManager.removeFromCart(code)) {
            refreshCartTable();
        } else {
            showError("Failed to remove item.");
        }
    }

    @FXML
    private void handleClearCart() {
        clearError();
        cartManager.clearCart();
        refreshCartTable();
    }

    @FXML
    private void handleUpdateQuantity() {
        clearError();
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select an item to update.");
            return;
        }
        String code = selected.getItem().getCode();
        String qtyText = txtCartQuantity.getText().trim();
        if (qtyText.isEmpty()) {
            showError("Please enter a new quantity in the Qty field.");
            return;
        }
        int newQty;
        try {
            newQty = Integer.parseInt(qtyText);
            if (newQty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError("Quantity must be a positive integer.");
            return;
        }

        if (cartManager.updateQuantity(code, newQty)) {
            refreshCartTable();
        } else {
            showError("Could not update quantity. Possibly insufficient stock.");
        }
    }

    @FXML
    private void handleCheckout() {
        clearError();
        if (cartManager.getCartItems().isEmpty()) {
            showError("Cart is empty. Nothing to checkout.");
            return;
        }

        double total = cartManager.checkout();
        LegacyDataHandler.saveInventory(dataFile, inventoryManager.getAllItems());
        refreshCartTable();
        if (onInventoryChanged != null) onInventoryChanged.run();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Checkout Complete");
        alert.setHeaderText(null);
        alert.setContentText("Total paid: Rs. " + String.format("%.2f", total));
        alert.showAndWait();
    }

    @FXML
    private void handleCloseCart() {
        Stage stage = (Stage) cartTable.getScene().getWindow();
        stage.close();
    }
}