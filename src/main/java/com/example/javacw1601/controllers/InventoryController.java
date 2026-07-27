package com.example.javacw1601.controllers;

import com.example.javacw1601.backend.*;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class InventoryController implements Initializable {

    private static final String INVENTORY_FILE = "inventory_legacy.txt";

    private InventoryManager inventoryManager;
    private AuditLogger auditLogger;

    @FXML private TableView<Inventory> inventoryTable;
    @FXML private TableColumn<Inventory, String> colCode;
    @FXML private TableColumn<Inventory, String> colName;
    @FXML private TableColumn<Inventory, String> colBrand;
    @FXML private TableColumn<Inventory, Double> colPrice;
    @FXML private TableColumn<Inventory, String> colCategory;
    @FXML private TableColumn<Inventory, Integer> colQuantity;
    @FXML private TableColumn<Inventory, Integer> colThreshold;
    @FXML private TableColumn<Inventory, String> colImage;

    @FXML private Label lblTotalCount;
    @FXML private Label lblTotalValue;
    @FXML private Label lblLowStockCount;

    public void init(InventoryManager inventoryManager, AuditLogger auditLogger) {
        this.inventoryManager = inventoryManager;
        this.auditLogger = auditLogger;
        refreshTable();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colBrand.setCellValueFactory(new PropertyValueFactory<>("brand"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
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
    }

    private void refreshTable() {
        ArrayList<Inventory> items = inventoryManager.getSortedItems();
        ObservableList<Inventory> data = FXCollections.observableArrayList(items);
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
        refreshTable();
    }

    @FXML
    private void handleOpenAddItem() {
        try {
            Stage stage = openModal("add-item-view.fxml", "Add New Spare Part");
            FXMLLoader loader = (FXMLLoader) stage.getUserData();
            AddItemController controller = loader.getController();
            controller.init(inventoryManager, auditLogger, INVENTORY_FILE);
            stage.showAndWait();
            refreshTable();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open Add Item window: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenUpdateItem() {
        Inventory selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item to update.");
            return;
        }
        try {
            Stage stage = openModal("update-item-view.fxml", "Update Spare Part");
            FXMLLoader loader = (FXMLLoader) stage.getUserData();
            UpdateItemController controller = loader.getController();
            controller.init(inventoryManager, auditLogger, INVENTORY_FILE, selected);
            stage.showAndWait();
            refreshTable();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open Update Item window: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteItem() {
        Inventory selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an item to delete.");
            return;
        }
        if (!showConfirm("Delete Item", "Delete \"" + selected.getName() + "\" (" + selected.getCode() + ") ?")) {
            return;
        }
        String code = selected.getCode();
        if (inventoryManager.deleteItem(code)) {
            auditLogger.logDeleteItem(code);
            LegacyDataHandler.saveInventory(INVENTORY_FILE, inventoryManager.getAllItems());
            refreshTable();
            showAlert(Alert.AlertType.INFORMATION, "Deleted", "Item removed.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete item.");
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) inventoryTable.getScene().getWindow();
        stage.close();
    }
}