package com.example.javacw1601.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class AuditLogController {

    @FXML
    private ListView<String> logListView;

    private String logFile;

    public void init(String logFile) {
        this.logFile = logFile;
        loadLog();
    }

    @FXML
    private void handleRefresh() {
        loadLog();
    }

    public void refresh() {
        loadLog();
    }

    private void loadLog() {
        Path path = Path.of(logFile);
        ObservableList<String> entries = FXCollections.observableArrayList();

        if (Files.exists(path)) {
            try {
                List<String> lines = Files.readAllLines(path);

                for (int i = lines.size() - 1; i >= 0; i--) {
                    entries.add(lines.get(i));
                }
            } catch (IOException e) {
                entries.add("Could not read audit log: " + e.getMessage());
            }
        }

        if (entries.isEmpty()) {
            entries.add("No activity recorded yet.");
        }

        logListView.setItems(entries);
    }
}
