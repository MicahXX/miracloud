package org.miracloud.frontend.views;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.miracloud.frontend.AppState;
import org.miracloud.frontend.controller.loginController;

public class appView {

    public void show() {
        Stage stage = AppState.getStage();

        //navbar
        VBox nav = new VBox();

        // header
        TextField search = new TextField("Search");
        HBox header = new HBox(search);


        //Label label  = new Label("this is cloud storage app");

        //borderPane.setCenter(label);

        BorderPane borderPane = new BorderPane();

        borderPane.setTop(header);
        borderPane.setLeft(nav);


        /* CSS */


        Scene scene = new Scene(borderPane, 960, 540);

        AppState.applyStylesheets(scene);

        stage.setScene(scene);
        stage.show();
    }
}
