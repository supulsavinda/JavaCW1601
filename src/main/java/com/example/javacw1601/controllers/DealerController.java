package com.example.javacw1601.controllers;

import com.example.javacw1601.backend.AuditLogger;
import com.example.javacw1601.backend.DealerManager;
import com.example.javacw1601.backend.Dealers;
import com.example.javacw1601.backend.LegacyDataHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DealerController implements Initializable {

    private DealerManager dealerManager;
    private AuditLogger auditLogger;
    private String dataFile;

    @FXML private TableView<Dealers> dealerTable;
    @FXML private TableColumn<Dealers, String> colDealerCode;
    @FXML private TableColumn<Dealers, String> colDealerName;
    @FXML private TableColumn<Dealers, String> colDealerPhone;
    @FXML private TableColumn<Dealers, String> colDealerLocation;

    @FXML private TextField txtDealerCode;
    @FXML private TextField txtDealerName;
    @FXML private TextField txtDealerPhone;
    @FXML private TextField txtDealerLocation;
    @FXML private Label lblDealerError;

    @FXML private ListView<String> lstRandomDealers;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colDealerCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colDealerName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDealerPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colDealerLocation.setCellValueFactory(new PropertyValueFactory<>("location"));

        dealerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) populateForm(newV);
        });
    }

    public void init(DealerManager dealerManager, AuditLogger auditLogger, String dataFile) {
        this.dealerManager = dealerManager;
        this.auditLogger = auditLogger;
        this.dataFile = dataFile;
        refreshTable();
    }

    private void refreshTable() {
        ObservableList<Dealers> data = FXCollections.observableArrayList(dealerManager.getAllDealers());
        dealerTable.setItems(data);
    }

    private void populateForm(Dealers d) {
        txtDealerCode.setText(d.getCode());
        txtDealerName.setText(d.getName());
        txtDealerPhone.setText(d.getPhone());
        txtDealerLocation.setText(d.getLocation());
    }

    private void clearForm() {
        txtDealerCode.clear();
        txtDealerName.clear();
        txtDealerPhone.clear();
        txtDealerLocation.clear();
        dealerTable.getSelectionModel().clearSelection();
        lblDealerError.setText("");
    }

    private void persist() {
        LegacyDataHandler.saveDealers(dataFile, dealerManager.getAllDealers());
    }

    @FXML
    private void handleAddDealer() {
        lblDealerError.setText("");
        String code = txtDealerCode.getText() == null ? "" : txtDealerCode.getText().trim();
        String name = txtDealerName.getText() == null ? "" : txtDealerName.getText().trim();
        String phone = txtDealerPhone.getText() == null || txtDealerPhone.getText().trim().isEmpty()
                ? "Unknown" : txtDealerPhone.getText().trim();
        String location = txtDealerLocation.getText() == null ? "" : txtDealerLocation.getText().trim();

        if (code.isEmpty() || name.isEmpty() || location.isEmpty()) {
            lblDealerError.setText("Code, Name and Location are required.");
            return;
        }
        if (dealerManager.findDealerByCode(code) != null) {
            lblDealerError.setText("A dealer with code \"" + code + "\" already exists.");
            return;
        }

        Dealers newDealer = new Dealers(code, name, phone, location);
        if (dealerManager.addDealer(newDealer)) {
            auditLogger.logAddDealer(newDealer);
            persist();
            refreshTable();
            clearForm();
        } else {
            lblDealerError.setText("Could not add dealer. The code may already be in use.");
        }
    }

    @FXML
    private void handleUpdateDealer() {
        lblDealerError.setText("");
        String code = txtDealerCode.getText() == null ? "" : txtDealerCode.getText().trim();
        String name = txtDealerName.getText() == null ? "" : txtDealerName.getText().trim();
        String phone = txtDealerPhone.getText() == null || txtDealerPhone.getText().trim().isEmpty()
                ? "Unknown" : txtDealerPhone.getText().trim();
        String location = txtDealerLocation.getText() == null ? "" : txtDealerLocation.getText().trim();

        if (code.isEmpty()) {
            lblDealerError.setText("Select a dealer or enter its Code to update.");
            return;
        }
        if (name.isEmpty() || location.isEmpty()) {
            lblDealerError.setText("Name and Location are required.");
            return;
        }
        if (dealerManager.findDealerByCode(code) == null) {
            lblDealerError.setText("No dealer found with code \"" + code + "\".");
            return;
        }

        Dealers updated = new Dealers(code, name, phone, location);
        if (dealerManager.updateDealer(code, updated)) {
            auditLogger.logUpdateDealer(code, updated);
            persist();
            refreshTable();
            clearForm();
        } else {
            lblDealerError.setText("Could not update dealer.");
        }
    }

    @FXML
    private void handleDeleteDealer() {
        lblDealerError.setText("");
        Dealers selected = dealerTable.getSelectionModel().getSelectedItem();
        String code = selected != null ? selected.getCode()
                : (txtDealerCode.getText() == null ? "" : txtDealerCode.getText().trim());

        if (code.isEmpty() || dealerManager.findDealerByCode(code) == null) {
            lblDealerError.setText("Select a dealer from the table, or enter a valid Code, to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Dealer");
        confirm.setHeaderText(null);
        confirm.setContentText("Delete dealer with code \"" + code + "\"?");
        if (confirm.showAndWait().filter(bt -> bt == ButtonType.OK).isEmpty()) {
            return;
        }

        if (dealerManager.deleteDealer(code)) {
            auditLogger.logDeleteDealer(code);
            persist();
            refreshTable();
            clearForm();
        } else {
            lblDealerError.setText("Failed to delete dealer.");
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
    }

    @FXML
    private void handleRandomDealers() {
        try {
            ArrayList<Dealers> selected = dealerManager.select4Random();
            lstRandomDealers.getItems().clear();
            for (Dealers d : selected) {
                lstRandomDealers.getItems().add(d.getCode() + "  |  " + d.getName() + "  |  " + d.getLocation());
            }
        } catch (IllegalStateException e) {
            lblDealerError.setText("Need at least 4 dealers to pick from (currently " + dealerManager.getTotalCount() + ").");
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) txtDealerCode.getScene().getWindow();
        stage.close();
    }
}