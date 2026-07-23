package com.example.javacw1601;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        java.net.URL url = MainApplication.class.getResource("/com/example/javacw1601/main-view.fxml");
        System.out.println("FXML URL: " + url);
        FXMLLoader fxmlLoader = new FXMLLoader(url);
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Malabe TUK-TUK");
        stage.setScene(scene);
        stage.show();
    }
}
