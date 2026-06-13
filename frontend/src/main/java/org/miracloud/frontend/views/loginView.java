package org.miracloud.frontend.views;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import org.miracloud.frontend.AppState;
import org.miracloud.frontend.controller.loginController;

public class loginView {

    public Scene buildScene() {
        try {
            loginController controller = new loginController();

            Label emailLabel = new Label("EMAIL");
            TextField emailInputField = new TextField();
            emailInputField.setPromptText("you@example.com");
            VBox emailInput = new VBox(4, emailLabel, emailInputField);

            Label passwordLabel = new Label("PASSWORD");
            PasswordField passwordInputField = new PasswordField();
            passwordInputField.setPromptText("••••••••");
            VBox passwordInput = new VBox(4, passwordLabel, passwordInputField);

            Button loginButton = new Button("Sign in");
            Label errors = new Label("");
            errors.setWrapText(true);

            VBox formPanel = new VBox(20, emailInput, passwordInput, loginButton, errors);
            formPanel.getStyleClass().add("form-panel");
            formPanel.setAlignment(Pos.CENTER);
            HBox.setHgrow(formPanel, Priority.ALWAYS);

            Label logo = new Label("\u2601 MiraCloud");
            Label tagline = new Label("Your files, everywhere you need them.");
            tagline.setWrapText(true);
            tagline.setAlignment(Pos.CENTER);
            Button accentCta = new Button("Sign up");
            VBox centerContent = new VBox(16, tagline, accentCta);
            centerContent.setAlignment(Pos.CENTER);

            Circle dot1 = new Circle(3);
            Circle dot2 = new Circle(3);
            HBox dots = new HBox(6, dot1, dot2);
            dots.setAlignment(Pos.CENTER);

            Region topSpacer = new Region();
            Region bottomSpacer = new Region();
            VBox.setVgrow(topSpacer, Priority.ALWAYS);
            VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

            VBox accentPanel = new VBox(logo, topSpacer, centerContent, bottomSpacer, dots);
            accentPanel.setAlignment(Pos.CENTER);
            HBox.setHgrow(accentPanel, Priority.ALWAYS);

            HBox root = new HBox(accentPanel, formPanel);
            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(root.widthProperty());
            clip.heightProperty().bind(root.heightProperty());
            root.setClip(clip);

            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());

            errors.getStyleClass().add("error-label");
            emailLabel.getStyleClass().add("field-label");
            passwordLabel.getStyleClass().add("field-label");
            loginButton.getStyleClass().add("submit-button");
            logo.getStyleClass().add("accent-logo");
            tagline.getStyleClass().add("accent-tagline");
            accentCta.getStyleClass().add("accent-cta");
            dot1.getStyleClass().add("dot-active");
            dot2.getStyleClass().add("dot");
            accentPanel.getStyleClass().add("accent-panel");
            root.getStyleClass().add("auth-root");

            AppState.applyStylesheets(scene, "login.css");

            loginButton.setOnAction(e -> {
                loginButton.setDisable(true);
                errors.setText("Signing in...");
                new Thread(() -> {
                    String result = controller.handleLogin(emailInputField.getText(), passwordInputField.getText());
                    Platform.runLater(() -> {
                        loginButton.setDisable(false);
                        errors.setText(result);
                    });
                }).start();
            });
            accentCta.setOnAction(e -> controller.toSignup());

            return scene;

        } catch (Exception e) {
            Label err = new Label(e.getMessage());
            Scene scene = new Scene(new VBox(err), 400, 300);
            AppState.getStage().setScene(scene);
            AppState.getStage().show();
            return null;
        }
    }
}