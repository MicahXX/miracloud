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
import org.miracloud.frontend.controller.signupController;

import java.io.PrintWriter;
import java.io.StringWriter;

public class signupView {

    public Scene buildScene() {
        try {
            signupController controller = new signupController();

            Label usernameLabel = new Label("USERNAME");
            TextField usernameInputField = new TextField();
            usernameInputField.setPromptText("Your name");
            VBox usernameInput = new VBox(4, usernameLabel, usernameInputField);

            Label emailLabel = new Label("EMAIL");
            TextField emailInputField = new TextField();
            emailInputField.setPromptText("you@example.com");
            VBox emailInput = new VBox(4, emailLabel, emailInputField);

            Label passwordLabel = new Label("PASSWORD");
            PasswordField passwordInputField = new PasswordField();
            passwordInputField.setPromptText("••••••••");
            VBox passwordInput = new VBox(4, passwordLabel, passwordInputField);

            CheckBox privacyCheckbox = new CheckBox();
            Label privacyLabel = new Label("Agree to our privacy policy and our cookies");
            HBox privacyRow = new HBox(8, privacyCheckbox, privacyLabel);
            privacyRow.setAlignment(Pos.CENTER_LEFT);

            Button signupButton = new Button("Create account");
            Label errors = new Label("");
            errors.setWrapText(true);

            VBox formPanel = new VBox(18, usernameInput, emailInput, passwordInput, privacyRow, signupButton, errors);
            formPanel.getStyleClass().add("form-panel");
            formPanel.setAlignment(Pos.CENTER);
            HBox.setHgrow(formPanel, Priority.ALWAYS);

            Label logo = new Label("\u2601 MiraCloud");
            Label tagline = new Label("Already have an account? Jump back in.");
            tagline.setWrapText(true);
            tagline.setAlignment(Pos.CENTER);
            Button accentCta = new Button("Sign in");
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

            HBox root = new HBox(formPanel, accentPanel);
            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(root.widthProperty());
            clip.heightProperty().bind(root.heightProperty());
            root.setClip(clip);

            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());

            errors.getStyleClass().add("error-label");
            usernameLabel.getStyleClass().add("field-label");
            emailLabel.getStyleClass().add("field-label");
            passwordLabel.getStyleClass().add("field-label");
            privacyLabel.getStyleClass().add("footer-label");
            privacyCheckbox.getStyleClass().add("privacy-checkbox");
            signupButton.getStyleClass().add("submit-button");
            logo.getStyleClass().add("accent-logo");
            tagline.getStyleClass().add("accent-tagline");
            accentCta.getStyleClass().add("accent-cta");
            dot1.getStyleClass().add("dot");
            dot2.getStyleClass().add("dot-active");
            accentPanel.getStyleClass().add("accent-panel");
            root.getStyleClass().add("auth-root");

            AppState.applyStylesheets(scene, "signup.css");

            signupButton.setOnAction(e -> {
                signupButton.setDisable(true);
                errors.setText("Creating account...");
                new Thread(() -> {
                    String result = controller.handleSignup(
                            emailInputField.getText(),
                            usernameInputField.getText(),
                            passwordInputField.getText(),
                            privacyCheckbox.isSelected());
                    Platform.runLater(() -> {
                        signupButton.setDisable(false);
                        errors.setText(result);
                    });
                }).start();
            });
            accentCta.setOnAction(e -> controller.toLogin());

            return scene;

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Label err = new Label(sw.toString());
            err.setWrapText(true);
            Scene scene = new Scene(new ScrollPane(err), 400, 300);
            AppState.getStage().setScene(scene);
            AppState.getStage().show();
            return null;
        }
    }
}