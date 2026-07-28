package com.example.javacw1601.controllers;

import com.example.javacw1601.backend.AuditLogger;
import com.example.javacw1601.backend.Inventory;
import com.example.javacw1601.backend.InventoryManager;
import com.example.javacw1601.backend.LegacyDataHandler;
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

    private InventoryManager inventoryManager;
    private AuditLogger auditLogger;
    private String dataFile;
    private Runnable onInventoryChanged;

    @FXML private TableView<Inventory> inventoryTable;
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

    @FXML private ComboBox<String> cmbSearchCategory;
    @FXML private TextField txtMinPrice;
    @FXML private TextField txtMaxPrice;
    @FXML private TextField txtKeyword;
    @FXML private Label lblSearchStatus;

    public void init(InventoryManager inventoryManager, AuditLogger auditLogger, String dataFile, Runnable onInventoryChanged) {
        this.inventoryManager = inventoryManager;
        this.auditLogger = auditLogger;
        this.dataFile = dataFile;
        this.onInventoryChanged = onInventoryChanged;
        refreshCategoryFilter();
        refreshTable();
    }

    private void refreshCategoryFilter() {
        String previousSelection = cmbSearchCategory.getValue();
        ObservableList<String> cats = FXCollections.observableArrayList();
        cats.add("All");
        ArrayList<String> categories = inventoryManager.getCategories();

        for (int i = 0; i < categories.size() - 1; i++) {
            for (int j = 0; j < categories.size() - i - 1; j++) {
                if (categories.get(j).compareToIgnoreCase(categories.get(j + 1)) > 0) {
                    String tmp = categories.get(j);
                    categories.set(j, categories.get(j + 1));
                    categories.set(j + 1, tmp);
                }
            }
        }
        cats.addAll(categories);
        cmbSearchCategory.setItems(cats);
        if (previousSelection != null && cats.contains(previousSelection)) {
            cmbSearchCategory.setValue(previousSelection);
        } else {
            cmbSearchCategory.setValue("All");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
    }

    public void refresh() {
        refreshCategoryFilter();
        refreshTable();
    }

    private void refreshTable() {
        ArrayList<Inventory> items = inventoryManager.getSortedItems();
        ObservableList<Inventory> data = FXCollections.observableArrayList(items);
        inventoryTable.setItems(data);
        inventoryTable.refresh();
        updateSummaryLabels();
        lblSearchStatus.setText("");
    }

    private void updateSummaryLabels() {
        lblTotalCount.setText("Total Items: " + inventoryManager.getTotalCount());
        lblTotalValue.setText("Total Value: Rs. " + String.format("%.2f", inventoryManager.getTotalValue()));
        lblLowStockCount.setText("Low Stock: " + inventoryManager.getLowStockItems().size());
    }

    @FXML
    private void handleSearch() {
        lblSearchStatus.setText("");

        String category = cmbSearchCategory.getValue();
        if (category == null) category = "All";

        double minPrice;
        String minText = txtMinPrice.getText() == null ? "" : txtMinPrice.getText().trim();
        if (minText.isEmpty()) {
            minPrice = 0.0;
        } else {
            try {
                minPrice = Double.parseDouble(minText);
                if (minPrice < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                lblSearchStatus.setText("Min Price must be a non-negative number.");
                return;
            }
        }

        double maxPrice;
        String maxText = txtMaxPrice.getText() == null ? "" : txtMaxPrice.getText().trim();
        if (maxText.isEmpty()) {
            maxPrice = Double.MAX_VALUE;
        } else {
            try {
                maxPrice = Double.parseDouble(maxText);
                if (maxPrice < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                lblSearchStatus.setText("Max Price must be a non-negative number.");
                return;
            }
        }

        if (minPrice > maxPrice) {
            lblSearchStatus.setText("Min Price cannot be greater than Max Price.");
            return;
        }

        String keyword = txtKeyword.getText() == null ? "" : txtKeyword.getText().trim();

        ArrayList<Inventory> results = inventoryManager.search(category, minPrice, maxPrice, keyword);
        ArrayList<Inventory> grouped = inventoryManager.groupByCategoryThenCode(results);
        inventoryTable.setItems(FXCollections.observableArrayList(grouped));
        updateSummaryLabels();
        lblSearchStatus.setText("Showing " + grouped.size() + " of " + inventoryManager.getTotalCount() + " item(s) matching your filters.");
    }

    @FXML
    private void handleClearFilters() {
        cmbSearchCategory.setValue("All");
        txtMinPrice.clear();
        txtMaxPrice.clear();
        txtKeyword.clear();
        refreshTable();
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
            controller.init(inventoryManager, auditLogger, dataFile);
            stage.showAndWait();
            refreshCategoryFilter();
            refreshTable();
            if (onInventoryChanged != null) onInventoryChanged.run();
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
            controller.init(inventoryManager, auditLogger, dataFile, selected);
            stage.showAndWait();
            refreshCategoryFilter();
            refreshTable();
            if (onInventoryChanged != null) onInventoryChanged.run();
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
            LegacyDataHandler.saveInventory(dataFile, inventoryManager.getAllItems());
            refreshCategoryFilter();
            refreshTable();
            if (onInventoryChanged != null) onInventoryChanged.run();
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
