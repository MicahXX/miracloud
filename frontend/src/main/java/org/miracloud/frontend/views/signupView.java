package org.miracloud.frontend.views;

//javafx import
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.net.URL;

import org.miracloud.frontend.controller.signupController;

public class signupView extends Application {

    @Override
    public void start(Stage stage) {

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
        Label label = new Label("");

        /*Vertical Box, goes in the center of the screen (the borderpane)*/
        VBox vBox = new VBox(usernameInput, emailInput, passwordInput, signupButton, label);
        vBox.setAlignment(Pos.CENTER);
        vBox.getStyleClass().add("vbox");

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(vBox);
        borderPane.getStyleClass().add("border-pane");

        Scene scene = new Scene(borderPane, 960, 540);
        URL cssResource = getClass().getResource("/org/miracloud/frontend/signup.css");
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        } else {
            System.err.println("CSS file not found!");
        }
        stage.setTitle("miracloud");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.initStyle(StageStyle.UTILITY);
        stage.show();

        signupButton.setOnAction(_ -> controller.handleSignup(
                emailInputField.getText(),
                usernameInputField.getText(),
                passwordInputField.getText()
        ));
    }
}
