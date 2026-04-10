package org.miracloud.frontend.views;

import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.miracloud.frontend.AppState;

public class appView {

    // nur style noch, ohne richtige Logik ist nur um System zu testen hat neue css appView.css
    public Scene buildScene() {
        try {
            Stage stage = AppState.getStage();

            // die 3 Sachen oben von links nach rechts
            TextField search = new TextField();
            search.setPromptText("Search files...");
            search.getStyleClass().add("search-field");
            HBox.setHgrow(search, Priority.ALWAYS);

            Button uploadBtn = new Button("Upload");
            uploadBtn.getStyleClass().add("btn-primary");

            Button newFolderBtn = new Button("New Folder");
            newFolderBtn.getStyleClass().add("btn-secondary");

            HBox header = new HBox(search, uploadBtn, newFolderBtn);
            header.getStyleClass().add("app-header");

            // ahh title
            Label appTitle = new Label("miracloud");
            appTitle.getStyleClass().add("app-title");

            // nav links
            Button navFiles = navButton("Files");
            Button navShared = navButton("Shared");
            Button navRecent = navButton("Recent");
            Button navTrash = navButton("Trash");

            navFiles.getStyleClass().add("nav-active");

            Button[] navButtons = {navFiles, navShared, navRecent, navTrash};
            for (Button btn : navButtons) {
                btn.setOnAction(e -> {
                    for (Button b : navButtons) b.getStyleClass().remove("nav-active");
                    btn.getStyleClass().add("nav-active");
                });
            }

            VBox nav = new VBox(appTitle, navFiles, navShared, navRecent, navTrash);
            nav.getStyleClass().add("app-sidebar");

            // die main View in mitte no files on default
            TableView<String> fileTable = new TableView<>();
            fileTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            fileTable.setPlaceholder(new Label("No files yet..."));
            fileTable.getStyleClass().add("file-table");

            TableColumn<String, String> nameCol = new TableColumn<>("Name");
            TableColumn<String, String> sizeCol = new TableColumn<>("Size");
            TableColumn<String, String> modCol = new TableColumn<>("Modified");
            TableColumn<String, String> typeCol = new TableColumn<>("Type");
            fileTable.getColumns().addAll(nameCol, sizeCol, modCol, typeCol);

            VBox content = new VBox(fileTable);
            VBox.setVgrow(fileTable, Priority.ALWAYS);
            content.getStyleClass().add("app-content");

            BorderPane borderPane = new BorderPane();
            borderPane.setTop(header);
            borderPane.setLeft(nav);
            borderPane.setCenter(content);

            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(borderPane, bounds.getWidth(), bounds.getHeight());

            // hab eigene css die Farben auch genommen man kannst sie auch aus main dann nehmen
            AppState.applyStylesheets(scene, "appView.css");
            return scene;

        } catch (Exception e) {
            Label errorLabel = new Label(e.getMessage());
            Scene scene = new Scene(new VBox(errorLabel), 400, 300);
            AppState.getStage().setScene(scene);
            AppState.getStage().show();
            return null;
        }
    }

    private Button navButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-btn");
        return btn;
    }
}