module com.example.javacw1601 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.javacw1601 to javafx.fxml;
    exports com.example.javacw1601;
}