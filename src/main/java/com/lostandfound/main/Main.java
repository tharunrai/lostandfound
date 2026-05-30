package com.lostandfound.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        setRoot("Login", "Lost & Found - Login", 800, 600);
    }

    public static void setRoot(String fxml, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/views/" + fxml + ".fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);
            scene.getStylesheets().add(Main.class.getResource("/css/styles.css").toExternalForm());
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load view: " + fxml);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
