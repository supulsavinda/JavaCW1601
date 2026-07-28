package com.example.javacw1601.controllers;

import com.example.javacw1601.backend.DealerManager;
import com.example.javacw1601.backend.Dealers;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DealerController implements Initializable {

    private DealerManager dealerManager;

    @FXML private TableView<Dealers> dealerTable;
    @FXML private TableColumn<Dealers, String> colDealerCode;
    @FXML private TableColumn<Dealers, String> colDealerName;
    @FXML private TableColumn<Dealers, String> colDealerPhone;
    @FXML private TableColumn<Dealers, String> colDealerLocation;

    @FXML private Label lblDealerCount;
    @FXML private Label lblDealerError;

    @FXML private ListView<String> lstRandomDealers;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colDealerCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colDealerName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDealerPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colDealerLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
    }

    public void init(DealerManager dealerManager) {
        this.dealerManager = dealerManager;
        refreshTable();
    }

    private void refreshTable() {
        ObservableList<Dealers> data = FXCollections.observableArrayList(dealerManager.getAllDealers());
        dealerTable.setItems(data);
        lblDealerCount.setText("Total Dealers: " + dealerManager.getTotalCount());
    }

    @FXML
    private void handleRandomDealers() {
        lblDealerError.setText("");
        try {
            ArrayList<Dealers> selected = dealerManager.select4Random();
            lstRandomDealers.getItems().clear();
            for (Dealers d : selected) {
                lstRandomDealers.getItems().add(
                        d.getCode() + "  |  " + d.getName() + "  |  " + d.getPhone() + "  |  " + d.getLocation());
            }
        } catch (IllegalStateException e) {
            lblDealerError.setText("Need at least 4 dealers to pick from (currently " + dealerManager.getTotalCount() + ").");
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) dealerTable.getScene().getWindow();
        stage.close();
    }
}
