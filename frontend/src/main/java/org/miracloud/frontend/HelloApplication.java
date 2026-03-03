package org.miracloud.frontend;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Button helloButton = new Button("Click me");
        Label label = new Label("");
        StackPane mainPane = new StackPane(helloButton, label);
        Scene scene = new Scene(mainPane, 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();


        helloButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //System.out.println("Hello World!");
                label.setText("Hello");
            }
        });
    }
}
