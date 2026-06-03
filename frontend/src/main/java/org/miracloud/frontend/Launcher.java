package org.miracloud.frontend;

import javafx.application.Application;
import javafx.stage.Stage;


public class  Launcher extends Application {

    @Override
    public void start(Stage stage) {
        stage.setResizable(true);
        stage.setTitle("MiraCloud");
        AppState.setStage(stage);
        stage.setMaximized(true);

        // check to see if user is already logged in
        if (AppState.hasSession() && org.miracloud.frontend.controller.loginController.tryAutoLogin()) {
            AppState.navigateTo("app");
        } else {
            AppState.navigateTo("signup");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}