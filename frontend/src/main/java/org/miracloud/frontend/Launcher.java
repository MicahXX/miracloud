package org.miracloud.frontend;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.miracloud.frontend.views.signupView;
import org.miracloud.frontend.views.loginView;


public class Launcher extends Application {

    @Override
    public void start(Stage stage) {
        stage.setResizable(false);
        //stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("MiraCloud");
        AppState.setStage(stage);
        new signupView().show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}