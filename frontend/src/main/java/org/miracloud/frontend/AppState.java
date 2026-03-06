package org.miracloud.frontend;

import javafx.stage.Stage;
import org.miracloud.frontend.views.loginView;
import org.miracloud.frontend.views.signupView;

public class AppState {
    private static Stage stage;

    public static void setStage(Stage stage) { AppState.stage = stage; }
    public static Stage getStage() { return stage; }

    public static void navigateTo(String view) {
        switch (view) {
            case "login" -> new loginView().show();
            case "signup" -> new signupView().show();
        }
    }
}
