package org.miracloud.frontend;

import javafx.stage.Stage;

public class AppState {
    /* this file changes views */
    private static Stage stage;

    public static Stage getStage() { return stage; }
    public static void setStage(Stage stage) { AppState.stage = stage; }

}
