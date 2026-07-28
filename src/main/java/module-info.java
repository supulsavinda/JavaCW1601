module com.example.javacw1601 {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.javacw1601 to javafx.fxml;
    opens com.example.javacw1601.controllers to javafx.fxml;
    opens com.example.javacw1601.backend to javafx.base, org.junit.platform.commons;

    exports com.example.javacw1601;
    exports com.example.javacw1601.controllers;
    exports com.example.javacw1601.backend;
}