package org.miracloud.frontend.views;

import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.miracloud.frontend.AppState;
import org.miracloud.frontend.controller.loginController;

public class loginView {

    public Scene buildScene() {
        try{
        Stage stage = AppState.getStage();
        loginController controller = new loginController();


        /*this goes in the Vertical Box*/
        // email input
        TextField emailInputField = new TextField("");
        Label emailLabel = new Label("Enter Email:");
        HBox emailInput = new HBox(12, emailLabel, emailInputField);
        emailInput.setAlignment(Pos.CENTER);
        //password input
        PasswordField passwordInputField = new PasswordField();
        Label passwordLabel = new Label("Enter Password:");
        HBox passwordInput = new HBox(12, passwordLabel, passwordInputField);
        passwordInput.setAlignment(Pos.CENTER);
        // Button
        Button loginButton = new Button("Login");
        // If you already have an acc, button to signup
        Label alreadyHaveAnAccLabel = new Label("Dont have an account?");
        Button toSignup = new Button("Signup");
        HBox alreadyHaveAnAcc = new HBox(6, alreadyHaveAnAccLabel, toSignup);
        alreadyHaveAnAcc.setAlignment(Pos.CENTER);
        // errors, like didn't fill smth in, acc already exists
        Label errors = new Label("");

        loginButton.setOnAction(e -> {
            String result = controller.handleLogin(
                    emailInputField.getText(),
                    passwordInputField.getText()
            );
            errors.setText(result);
        });

        /*Vertical Box, goes in the center of the screen (the borderpane)*/
        VBox vBox = new VBox(16, emailInput, passwordInput, loginButton, alreadyHaveAnAcc, errors);
        vBox.setAlignment(Pos.CENTER);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(vBox);

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(borderPane, bounds.getWidth(), bounds.getHeight());

        /* CSS */
        borderPane.getStyleClass().add("border-pane");
        vBox.getStyleClass().add("vbox");
        toSignup.getStyleClass().add("toSignup");
        emailLabel.getStyleClass().add("input-label");
        passwordLabel.getStyleClass().add("input-label");
        emailInput.getStyleClass().add("input-row");
        passwordInput.getStyleClass().add("input-row");
        loginButton.getStyleClass().add("login-button");

        AppState.applyStylesheets(scene, "login.css");

        toSignup.setOnAction(e -> controller.toSignup());

        return scene;
    } catch (Exception e) {
        // show error on screen instead of crashing
        Label errorLabel = new Label(e.getMessage());
        Scene scene = new Scene(new VBox(errorLabel), 400, 300);
        AppState.getStage().setScene(scene);
        AppState.getStage().show();
            return null;
        }
    }
}
