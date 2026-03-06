package org.miracloud.frontend.views;

//javafx import
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.miracloud.frontend.AppState;
import org.miracloud.frontend.controller.loginController;

import java.net.URL;


public class loginView {

    public void show() {
        Stage stage = AppState.getStage();
        loginController controller = new loginController();

        /*this goes in the Vertical Box*/
        // email input
        TextField emailInputField = new TextField("");
        Label emailLabel = new Label("Enter Email:");
        HBox emailInput = new HBox(emailLabel, emailInputField);
        emailInput.setAlignment(Pos.CENTER);
        //password input
        PasswordField passwordInputField = new PasswordField();
        Label passwordLabel = new Label("Enter Password:");
        HBox passwordInput = new HBox(passwordLabel, passwordInputField);
        passwordInput.setAlignment(Pos.CENTER);
        // Button
        Button loginButton = new Button("Login");
        // If you already have an acc, button to signup
        Label alreadyHaveAnAccLabel = new Label("Dont have an account?");
        Button toSignup = new Button("Signup");
        HBox alreadyHaveAnAcc = new HBox(alreadyHaveAnAccLabel, toSignup);
        alreadyHaveAnAcc.setAlignment(Pos.CENTER);
        // errors, like didn't fill smth in, acc already exists
        Label errors = new Label("");

        /*Vertical Box, goes in the center of the screen (the borderpane)*/
        VBox vBox = new VBox(emailInput, passwordInput, loginButton, alreadyHaveAnAcc, errors);
        vBox.setAlignment(Pos.CENTER);
        vBox.getStyleClass().add("vbox");

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(vBox);
        borderPane.getStyleClass().add("border-pane");

        Scene scene = new Scene(borderPane, 960, 540);

        AppState.applyStylesheets(scene, "login.css");

        toSignup.setOnAction(_ -> controller.toSignup());

        stage.setScene(scene);
        stage.show();
    }
}
