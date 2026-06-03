package org.miracloud.frontend.views;

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

            // email input
            Label emailLabel = new Label("EMAIL");
            TextField emailInputField = new TextField();
            emailInputField.setPromptText("you@example.com");
            VBox emailInput = new VBox(4, emailLabel, emailInputField);

            // password input
            Label passwordLabel = new Label("PASSWORD");
            PasswordField passwordInputField = new PasswordField();
            passwordInputField.setPromptText("••••••••");
            VBox passwordInput = new VBox(4, passwordLabel, passwordInputField);

            // forgot password
            Button forgotButton = new Button("Forgot password?");
            HBox forgotRow = new HBox(forgotButton);
            forgotRow.setAlignment(Pos.CENTER_RIGHT);

            // sign in button
            Button loginButton = new Button("Sign in");

            // errors
            Label errors = new Label("");
            errors.setWrapText(true);

            // form panel — right side
            VBox formPanel = new VBox(20, emailInput, passwordInput, forgotRow, loginButton, errors);
            formPanel.getStyleClass().add("form-panel");
            formPanel.setAlignment(Pos.CENTER);
            HBox.setHgrow(formPanel, Priority.ALWAYS);

            // accent panel — left side
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

            // root
            HBox root = new HBox(accentPanel, formPanel);
            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(root.widthProperty());
            clip.heightProperty().bind(root.heightProperty());
            root.setClip(clip);

            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());

            // CSS
            errors.getStyleClass().add("error-label");
            emailLabel.getStyleClass().add("field-label");
            passwordLabel.getStyleClass().add("field-label");
            forgotButton.getStyleClass().add("forgot-link");
            loginButton.getStyleClass().add("submit-button");
            logo.getStyleClass().add("accent-logo");
            tagline.getStyleClass().add("accent-tagline");
            accentCta.getStyleClass().add("accent-cta");
            dot1.getStyleClass().add("dot-active");
            dot2.getStyleClass().add("dot");
            accentPanel.getStyleClass().add("accent-panel");
            root.getStyleClass().add("auth-root");

            AppState.applyStylesheets(scene, "login.css");

            // handlers
            loginButton.setOnAction(e -> errors.setText(
                    controller.handleLogin(emailInputField.getText(), passwordInputField.getText())));
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