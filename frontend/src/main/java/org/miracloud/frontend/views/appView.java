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

    public Scene buildScene() {
        try{
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

        AppState.applyStylesheets(scene, "");

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
