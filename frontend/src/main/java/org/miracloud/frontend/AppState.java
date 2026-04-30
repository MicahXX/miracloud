package org.miracloud.frontend;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.miracloud.frontend.views.appView;
import org.miracloud.frontend.views.loginView;
import org.miracloud.frontend.views.signupView;

import java.util.Objects;

public class AppState {
    private static Stage stage;
    private static Scene loginScene;
    private static Scene signupScene;
    private static Scene appScene;

    public static void setStage(Stage stage) { AppState.stage = stage; }
    public static Stage getStage() { return stage; }

    public static void navigateTo(String view) {
        boolean wasMaximized = stage.isMaximized();
        double width = stage.getWidth();
        double height = stage.getHeight();

        switch (view) {
            case "login" -> {
                if (loginScene == null) loginScene = new loginView().buildScene();
                stage.setScene(loginScene);
            }
            case "signup" -> {
                if (signupScene == null) signupScene = new signupView().buildScene();
                stage.setScene(signupScene);
            }
            case "app" -> {
                if (appScene == null) appScene = new appView().buildScene();
                stage.setScene(appScene);
            }
        }

        stage.show();

        Platform.runLater(() -> {
            if (wasMaximized) {
                stage.setMaximized(true);
            } else {
                stage.setWidth(width);
                stage.setHeight(height);
            }
        });
    }

    // AppState.java
    private static final boolean isMobile = com.gluonhq.attach.util.Platform.isAndroid()
            || com.gluonhq.attach.util.Platform.isIOS();

    public static boolean isMobile() { return isMobile; }

    public static void applyStylesheets(Scene scene, String desktopCss) {
        // don't add stylesheets if already applied
        if (!scene.getStylesheets().isEmpty()) return;

        String viewCss = isMobile ? desktopCss.replace(".css", "-mobile.css") : desktopCss;
        System.out.println("=== isMobile: " + isMobile + " | css: " + viewCss + " ===");

        scene.getStylesheets().add(
                Objects.requireNonNull(
                        AppState.class.getResource("/org/miracloud/frontend/main.css")
                ).toExternalForm()
        );
        scene.getStylesheets().add(
                Objects.requireNonNull(
                        AppState.class.getResource("/org/miracloud/frontend/" + viewCss)
                ).toExternalForm()
        );
    }
}
