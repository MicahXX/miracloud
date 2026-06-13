package org.miracloud.frontend;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Launcher extends Application {
    @Override
    public void start(Stage stage) {
        stage.setResizable(true);
        stage.setTitle("MiraCloud");
        AppState.setStage(stage);
        stage.setMaximized(true);

        // show login immediately, then check session in background
        AppState.navigateTo("signup");

        new Thread(() -> {
            if (AppState.hasSession() && org.miracloud.frontend.controller.loginController.tryAutoLogin()) {
                Platform.runLater(() -> AppState.navigateTo("app"));
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}