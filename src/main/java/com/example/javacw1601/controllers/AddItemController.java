package com.example.javacw1601.controllers;

import com.example.javacw1601.backend.AuditLogger;
import com.example.javacw1601.backend.Inventory;
import com.example.javacw1601.backend.InventoryManager;
import com.example.javacw1601.backend.LegacyDataHandler;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class AddItemController {

    private static final String[] DEFAULT_CATEGORIES = {
            "Engine", "Electrical", "Brakes", "Suspension", "Body & Chassis",
            "Tyres & Wheels", "Transmission", "Exhaust System", "Lighting", "Filters"
    };

    private InventoryManager inventoryManager;
    private AuditLogger auditLogger;
    private String dataFile;

    @FXML private TextField txtCode;
    @FXML private TextField txtName;
    @FXML private TextField txtBrand;
    @FXML private TextField txtPrice;
    @FXML private TextField txtQuantity;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private DatePicker dpDate;
    @FXML private TextField txtImage;
    @FXML private Label lblError;

    public void init(InventoryManager inventoryManager, AuditLogger auditLogger, String dataFile) {
        this.inventoryManager = inventoryManager;
        this.auditLogger = auditLogger;
        this.dataFile = dataFile;

        cmbCategory.setItems(FXCollections.observableArrayList(DEFAULT_CATEGORIES));
        for (String cat : inventoryManager.getCategories()) {
            if (!cmbCategory.getItems().contains(cat)) {
                cmbCategory.getItems().add(cat);
            }
        }
        dpDate.setValue(LocalDate.now());
        txtImage.setText("no_image.png");
    }

    @FXML
    private void handleSave() {
        lblError.setText("");

        String code = txtCode.getText() == null ? "" : txtCode.getText().trim();
        String name = txtName.getText() == null ? "" : txtName.getText().trim();
        String brand = txtBrand.getText() == null ? "" : txtBrand.getText().trim();
        String category = cmbCategory.getEditor().getText() == null ? "" : cmbCategory.getEditor().getText().trim();

        if (code.isEmpty()) {
            lblError.setText("Item Code is required.");
            return;
        }
        if (name.isEmpty()) {
            lblError.setText("Item Name is required.");
            return;
        }
        if (category.isEmpty()) {
            lblError.setText("Category is required.");
            return;
        }
        if (inventoryManager.findItemByCode(code) != null) {
            lblError.setText("An item with code \"" + code + "\" already exists.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(txtPrice.getText().trim());
            if (price < 0) throw new NumberFormatException();
        } catch (Exception e) {
            lblError.setText("Enter a valid, non-negative Price.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(txtQuantity.getText().trim());
            if (quantity < 0) throw new NumberFormatException();
        } catch (Exception e) {
            lblError.setText("Enter a valid, non-negative Quantity.");
            return;
        }

        LocalDate date = dpDate.getValue() == null ? LocalDate.now() : dpDate.getValue();
        String image = txtImage.getText() == null || txtImage.getText().trim().isEmpty()
                ? "no_image.png" : txtImage.getText().trim();

        Inventory newItem = new Inventory(code, name, brand, price, quantity, category, date.toString(), image);

        if (inventoryManager.addItem(newItem)) {
            auditLogger.logAddItem(newItem);
            LegacyDataHandler.saveInventory(dataFile, inventoryManager.getAllItems());
            closeWindow();
        } else {
            lblError.setText("Could not add item. The code may already be in use.");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) txtCode.getScene().getWindow();
        stage.close();
    }
}