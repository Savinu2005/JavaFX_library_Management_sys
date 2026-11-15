package com.library;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        stage.setTitle("BookNest — Library Management System");
        stage.setMinWidth(1050);
        stage.setMinHeight(700);
        showLogin();
        stage.show();
    }

    public static void showLogin() throws Exception {
        stage.setScene(new Scene(
            FXMLLoader.load(Main.class.getResource("/com/library/login.fxml")), 1100, 720));
    }

    public static void showDashboard() throws Exception {
        stage.setScene(new Scene(
            FXMLLoader.load(Main.class.getResource("/com/library/main.fxml")), 1250, 800));
    }

    public static void main(String[] args) { launch(args); }
}
