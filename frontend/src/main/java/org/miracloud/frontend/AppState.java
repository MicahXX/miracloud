package org.miracloud.frontend;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.miracloud.frontend.views.loginView;
import org.miracloud.frontend.views.signupView;

import java.net.URL;

public class AppState {
    private static Stage stage;
    private static loginView loginView = new loginView();
    private static signupView signupView = new signupView();


    public static void setStage(Stage stage) { AppState.stage = stage; }
    public static Stage getStage() { return stage; }

    public static void navigateTo(String view) {
        switch (view) {
            case "login" -> loginView.show();
            case "signup" -> signupView.show();
        }
    }

    public static void applyStylesheets(Scene scene, String... viewCss) {
        scene.getStylesheets().add(
                AppState.class.getResource("/org/miracloud/frontend/main.css").toExternalForm()
        );
        for (String css : viewCss) {
            scene.getStylesheets().add(
                    AppState.class.getResource("/org/miracloud/frontend/"+css).toExternalForm()
            );
        }
    }
}
