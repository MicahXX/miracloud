package org.miracloud.frontend.views;

//javafx import
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.net.URL;

import org.miracloud.frontend.AppState;
import org.miracloud.frontend.controller.signupController;

public class signupView {

    public void show() {
        Stage stage = AppState.getStage();
        signupController controller = new signupController();

        /*this goes in the Vertical Box*/
        // user input
        TextField usernameInputField = new TextField("");
        Label usernameLabel = new Label("Enter Username:");
        HBox usernameInput = new HBox(usernameLabel, usernameInputField);
        usernameInput.setAlignment(Pos.CENTER);

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
        Button signupButton = new Button("Sign Up");
        // you have to accept privacy policy and cookies with easy checkbox
        Label privacyPolicyAndCookiesLabel = new Label("By clicking this you agree to our privacy policy and our cookies");
        CheckBox privacyPolicyAndCookiesCheckbox = new CheckBox();
        HBox privacyPolicyAndCookiesInput = new HBox(privacyPolicyAndCookiesLabel, privacyPolicyAndCookiesCheckbox);
        privacyPolicyAndCookiesInput.setAlignment(Pos.CENTER);

        // If you already have an acc, button to login
        Label alreadyHaveAnAccLabel = new Label("Already have an account?");
        Button toLogin = new Button("Login");
        HBox alreadyHaveAnAcc = new HBox(alreadyHaveAnAccLabel, toLogin);
        alreadyHaveAnAcc.setAlignment(Pos.CENTER);
        // errors, like didn't fill smth in, acc already exists
        Label errors = new Label("");

        /*Vertical Box, goes in the center of the screen (the borderpane)*/
        VBox vBox = new VBox(usernameInput, emailInput, passwordInput, privacyPolicyAndCookiesInput, signupButton, alreadyHaveAnAcc, errors);
        vBox.setAlignment(Pos.CENTER);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(vBox);

        /* CSS */
        privacyPolicyAndCookiesCheckbox.getStyleClass().add("privacyCheckbox");
        alreadyHaveAnAccLabel.getStyleClass().add("alreadyHaveAnAcLabel");
        toLogin.getStyleClass().add("toLogin");
        borderPane.getStyleClass().add("border-pane");
        vBox.getStyleClass().add("vbox");


        Scene scene = new Scene(borderPane, 960, 540);

        AppState.applyStylesheets(scene, "signup.css");

        signupButton.setOnAction(_ -> {
            String returnString = controller.handleSignup(
                    emailInputField.getText(),
                    usernameInputField.getText(),
                    passwordInputField.getText(),
                    privacyPolicyAndCookiesCheckbox.isSelected()
            );
            errors.setText(returnString);
        });

        toLogin.setOnAction(_ -> controller.toLogin());

        stage.setScene(scene);
        stage.show();
    }
}
