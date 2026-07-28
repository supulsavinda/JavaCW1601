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
import java.time.format.DateTimeParseException;

public class UpdateItemController {

    private static final String[] DEFAULT_CATEGORIES = {
            "Engine", "Electrical", "Brakes", "Suspension", "Body & Chassis",
            "Tyres & Wheels", "Transmission", "Exhaust System", "Lighting", "Filters"
    };

    private InventoryManager inventoryManager;
    private AuditLogger auditLogger;
    private String dataFile;
    private String originalCode;

    @FXML private TextField txtCode;
    @FXML private TextField txtName;
    @FXML private TextField txtBrand;
    @FXML private TextField txtPrice;
    @FXML private TextField txtQuantity;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private DatePicker dpDate;
    @FXML private TextField txtImage;
    @FXML private Label lblError;

    public void init(InventoryManager inventoryManager, AuditLogger auditLogger, String dataFile, Inventory selected) {
        this.inventoryManager = inventoryManager;
        this.auditLogger = auditLogger;
        this.dataFile = dataFile;
        this.originalCode = selected.getCode();

        cmbCategory.setItems(FXCollections.observableArrayList(DEFAULT_CATEGORIES));
        for (String cat : inventoryManager.getCategories()) {
            if (!cmbCategory.getItems().contains(cat)) {
                cmbCategory.getItems().add(cat);
            }
        }

        txtCode.setText(selected.getCode());
        txtName.setText(selected.getName());
        txtBrand.setText(selected.getBrand());
        txtPrice.setText(String.valueOf(selected.getPrice()));
        txtQuantity.setText(String.valueOf(selected.getQuantity()));
        cmbCategory.setValue(selected.getCategory());
        cmbCategory.getEditor().setText(selected.getCategory());
        txtImage.setText(selected.getImage());

        try {
            dpDate.setValue(LocalDate.parse(selected.getDate()));
        } catch (DateTimeParseException | NullPointerException e) {
            dpDate.setValue(LocalDate.now());
        }
    }

    @FXML
    private void handleSave() {
        lblError.setText("");

        String name = txtName.getText() == null ? "" : txtName.getText().trim();
        String brand = txtBrand.getText() == null ? "" : txtBrand.getText().trim();
        String category = cmbCategory.getEditor().getText() == null ? "" : cmbCategory.getEditor().getText().trim();

        if (name.isEmpty()) {
            lblError.setText("Item Name is required.");
            return;
        }
        if (category.isEmpty()) {
            lblError.setText("Category is required.");
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

        Inventory updated = new Inventory(originalCode, name, brand, price, quantity, category, date.toString(), image);

        if (inventoryManager.updateItem(originalCode, updated)) {
            auditLogger.logUpdateItem(originalCode, updated);
            LegacyDataHandler.saveInventory(dataFile, inventoryManager.getAllItems());
            closeWindow();
        } else {
            lblError.setText("Could not update item. It may have been removed.");
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