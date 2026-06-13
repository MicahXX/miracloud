package org.miracloud.frontend;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.miracloud.frontend.views.appView;
import org.miracloud.frontend.views.loginView;
import org.miracloud.frontend.views.signupView;

import java.util.Objects;
import java.util.prefs.Preferences;

public class AppState {

    private static Stage stage;
    private static final Preferences prefs = Preferences.userNodeForPackage(AppState.class);

    private static String accessToken = null;
    private static String refreshToken = null;
    private static String username = null;

    public static void saveTokens(String access, String refresh, String user) {
        accessToken = access; refreshToken = refresh; username = user;
        prefs.put("accessToken", access); prefs.put("refreshToken", refresh); prefs.put("username", user);
    }
    public static String getAccessToken() { return accessToken != null ? accessToken : (accessToken = prefs.get("accessToken", null)); }
    public static String getRefreshToken() { return refreshToken != null ? refreshToken : (refreshToken = prefs.get("refreshToken", null)); }
    public static String getUsername()     { return username != null ? username : (username = prefs.get("username", null)); }
    public static void clearTokens() {
        accessToken = null; refreshToken = null; username = null;
        prefs.remove("accessToken"); prefs.remove("refreshToken"); prefs.remove("username");
    }
    public static boolean hasSession() { return getAccessToken() != null && getRefreshToken() != null; }

    public static void setStage(Stage s) { stage = s; }
    public static Stage getStage() { return stage; }

    public static void navigateTo(String view) {
        double stageW = stage.getWidth();
        double stageH = stage.getHeight();
        boolean wasMax = stage.isMaximized();

        Scene current = stage.getScene();
        if (current != null
                && current.getRoot() instanceof HBox currentRoot
                && currentRoot.getChildren().size() >= 2
                && (view.equals("login") || view.equals("signup"))) {

            // Slide out: panels fly apart using the *current* root width (already laid out)
            double half = currentRoot.getWidth() / 2.0;
            slideOut(currentRoot, half, () -> buildAndSlideIn(view, stageW, stageH, wasMax));

        } else {
            buildAndSlideIn(view, stageW, stageH, wasMax);
        }
    }

    private static void slideOut(HBox root, double half, Runnable onDone) {
        Region L = (Region) root.getChildren().get(0);
        Region R = (Region) root.getChildren().get(1);
        Interpolator ease = Interpolator.SPLINE(0.4, 0.0, 0.6, 1.0);

        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(L.translateXProperty(),  0,     ease),
                        new KeyValue(R.translateXProperty(),  0,     ease),
                        new KeyValue(L.opacityProperty(),     1.0,   ease),
                        new KeyValue(R.opacityProperty(),     1.0,   ease)),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(L.translateXProperty(),  -half, ease),
                        new KeyValue(R.translateXProperty(),   half, ease),
                        new KeyValue(L.opacityProperty(),      0.0,  ease),
                        new KeyValue(R.opacityProperty(),       0.0, ease))
        );
        t.setOnFinished(e -> { L.setTranslateX(0); R.setTranslateX(0); onDone.run(); });
        t.play();
    }

    private static void slideIn(HBox root, double half) {
        Region L = (Region) root.getChildren().get(0);
        Region R = (Region) root.getChildren().get(1);
        Interpolator ease = Interpolator.SPLINE(0.4, 0.0, 0.6, 1.0);

        L.setTranslateX(-half);
        R.setTranslateX( half);
        L.setOpacity(0.0);
        R.setOpacity(0.0);

        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(L.translateXProperty(), -half, ease),
                        new KeyValue(R.translateXProperty(),  half, ease),
                        new KeyValue(L.opacityProperty(),     0.0,  ease),
                        new KeyValue(R.opacityProperty(),     0.0,  ease)),
                new KeyFrame(Duration.millis(260),
                        new KeyValue(L.translateXProperty(),  0,    ease),
                        new KeyValue(R.translateXProperty(),  0,    ease),
                        new KeyValue(L.opacityProperty(),     1.0,  ease),
                        new KeyValue(R.opacityProperty(),     1.0,  ease))
        );
        t.play();
    }

    private static void buildAndSlideIn(String view, double stageW, double stageH, boolean wasMax) {
        Scene next = switch (view) {
            case "login"  -> new loginView().buildScene();
            case "signup" -> new signupView().buildScene();
            case "app"    -> new appView().buildScene();
            default       -> null;
        };
        if (next == null) return;

        stage.setScene(next);
        stage.show();

        Platform.runLater(() -> {
            if (wasMax) stage.setMaximized(true);
            else { stage.setWidth(stageW); stage.setHeight(stageH); }

            Platform.runLater(() -> {
                Scene s = stage.getScene();
                if (s == null) return;

                if (s.getRoot() instanceof HBox newRoot && newRoot.getChildren().size() >= 2) {
                    double half = newRoot.getWidth() / 2.0;
                    slideIn(newRoot, half);
                } else if (s.getRoot() != null) {
                    s.getRoot().setOpacity(0.0);
                    new Timeline(
                            new KeyFrame(Duration.ZERO,        new KeyValue(s.getRoot().opacityProperty(), 0.0)),
                            new KeyFrame(Duration.millis(260), new KeyValue(s.getRoot().opacityProperty(), 1.0))
                    ).play();
                }
            });
        });
    }

    private static final boolean isMobile =
            System.getProperty("javafx.platform", "").equalsIgnoreCase("android") ||
            System.getProperty("javafx.platform", "").equalsIgnoreCase("ios");

    public static boolean isMobile() { return isMobile; }

    public static void applyStylesheets(Scene scene, String desktopCss) {
        if (!scene.getStylesheets().isEmpty()) return;
        String css = isMobile ? desktopCss.replace(".css", "-mobile.css") : desktopCss;
        System.out.println("=== isMobile: " + isMobile + " | css: " + css + " ===");
        scene.getStylesheets().add(Objects.requireNonNull(
                AppState.class.getResource("/org/miracloud/frontend/main.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(
                AppState.class.getResource("/org/miracloud/frontend/" + css)).toExternalForm());
    }
}