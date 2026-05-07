package org.miracloud.frontend;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.miracloud.frontend.views.appView;
import org.miracloud.frontend.views.loginView;
import org.miracloud.frontend.views.signupView;

import java.util.Objects;
import java.util.prefs.Preferences;

public class AppState {
    private static Stage stage;
    private static Scene loginScene;
    private static Scene signupScene;
    private static Scene appScene;

    // Token
    private static String accessToken = null;
    private static String refreshToken = null;
    private static String username = null;
    private static final Preferences prefs = Preferences.userNodeForPackage(AppState.class);

    public static void saveTokens(String access, String refresh, String user) {
        accessToken = access;
        refreshToken = refresh;
        username = user;
        prefs.put("accessToken", access);
        prefs.put("refreshToken", refresh);
        prefs.put("username", user);
    }

    public static String getAccessToken() {
        if (accessToken != null) return accessToken;
        accessToken = prefs.get("accessToken", null);
        return accessToken;
    }

    public static String getRefreshToken() {
        if (refreshToken != null) return refreshToken;
        refreshToken = prefs.get("refreshToken", null);
        return refreshToken;
    }

    public static String getUsername() {
        if (username != null) return username;
        username = prefs.get("username", null);
        return username;
    }

    public static void clearTokens() {
        accessToken = null;
        refreshToken = null;
        username = null;
        prefs.remove("accessToken");
        prefs.remove("refreshToken");
        prefs.remove("username");
    }

    public static boolean hasSession() {
        return getAccessToken() != null && getRefreshToken() != null;
    }

    // what view
    public static void setStage(Stage stage) { AppState.stage = stage; }
    public static Stage getStage() { return stage; }

    public static void navigateTo(String view) {
        boolean wasMaximized = stage.isMaximized();
        double width = stage.getWidth();
        double height = stage.getHeight();

        switch (view) {
            case "login" -> {
                loginScene = new loginView().buildScene(); // always rebuild login
                stage.setScene(loginScene);
            }
            case "signup" -> {
                if (signupScene == null) signupScene = new signupView().buildScene();
                stage.setScene(signupScene);
            }
            case "app" -> {
                appScene = new appView().buildScene(); // always rebuild app to refresh file list
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

    // mobile or desktop
    private static final boolean isMobile = com.gluonhq.attach.util.Platform.isAndroid()
            || com.gluonhq.attach.util.Platform.isIOS();

    public static boolean isMobile() { return isMobile; }

    public static void applyStylesheets(Scene scene, String desktopCss) {
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