package org.miracloud.frontend;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.miracloud.frontend.views.signupView;
import org.miracloud.frontend.views.loginView;


public class  Launcher extends Application {

    @Override
    public void start(Stage stage) {
        stage.setResizable(true);
        stage.setTitle("MiraCloud");
        AppState.setStage(stage);
        stage.setMaximized(true);
        AppState.navigateTo("signup");
    }
    public static void main(String[] args) {
        launch(args);
    }
}