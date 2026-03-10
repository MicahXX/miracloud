package org.miracloud.frontend;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.miracloud.frontend.views.appView;
import org.miracloud.frontend.views.loginView;
import org.miracloud.frontend.views.signupView;

import java.net.URL;
import java.util.Objects;

public class AppState {
    private static Stage stage;
    private static loginView loginView = new loginView();
    private static signupView signupView = new signupView();

    private static  appView appView = new appView();


    public static void setStage(Stage stage) { AppState.stage = stage; }
    public static Stage getStage() { return stage; }

    public static void navigateTo(String view) {
        switch (view) {
            case "login" -> loginView.show();
            case "signup" -> signupView.show();
            case "app" -> appView.show();
        }
    }

    public static void applyStylesheets(Scene scene, String... viewCss) {
        scene.getStylesheets().add(
                Objects.requireNonNull(AppState.class.getResource("/org/miracloud/frontend/main.css")).toExternalForm()
        );
        for (String css : viewCss) {
            if(!css.isEmpty()) {
                scene.getStylesheets().add(
                        Objects.requireNonNull(AppState.class.getResource("/org/miracloud/frontend/" + css)).toExternalForm()
                );
            }
        }
    }
}
