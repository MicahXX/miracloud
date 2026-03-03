package org.miracloud.frontend.views;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class signupView extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        /*this goes in the Vertical Box*/
        // user input
        TextField usernameInputField = new TextField("");
        Label usernameLabel = new Label("Enter Username");
        HBox usernameInput = new HBox(usernameLabel, usernameInputField);
        usernameInput.setAlignment(Pos.CENTER);
        // email input
        TextField emailInputField = new TextField("");
        Label emailLabel = new Label("Enter Email");
        HBox emailInput = new HBox(emailLabel, emailInputField);
        emailInput.setAlignment(Pos.CENTER);
        //password input
        TextField passwordInputField = new TextField("");
        Label passwordLabel = new Label("Enter Password");
        HBox passwordInput = new HBox(passwordLabel, passwordInputField);
        passwordInput.setAlignment(Pos.CENTER);


        Button signupButton = new Button("Click me");
        Label label = new Label("");

        //Vertical Box, goes in the center of the screen (the borderpane)
        VBox vBox = new VBox((Node) usernameInput, (Node) emailInput, (Node) passwordInput, signupButton, label);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(vBox);

        Scene scene = new Scene(borderPane, 960, 540);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.initStyle(StageStyle.UTILITY);
        stage.show();

        signupButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                label.setText("Signed up");
            }
        });
    }
}
