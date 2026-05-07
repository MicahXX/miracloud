package org.miracloud.frontend.views;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.miracloud.frontend.AppState;
import org.miracloud.frontend.controller.appController;

import java.io.File;
import java.util.List;

public class appView {

    private final appController fc = new appController();

    public Scene buildScene() {
        try {
            Stage stage = AppState.getStage();

            // ── Header ───────────────────────────────────────────
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

            // ── Sidebar ──────────────────────────────────────────
            Label appTitle = new Label("miracloud");
            appTitle.getStyleClass().add("app-title");

            Button navFiles = navButton("Files");
            Button navShared = navButton("Shared");
            Button navRecent = navButton("Recent");
            Button navTrash = navButton("Trash");
            navFiles.getStyleClass().add("nav-active");

            Button logoutBtn = new Button("Logout");
            logoutBtn.getStyleClass().add("btn-secondary");

            Button[] navButtons = {navFiles, navShared, navRecent, navTrash};
            for (Button btn : navButtons) {
                btn.setOnAction(e -> {
                    for (Button b : navButtons) b.getStyleClass().remove("nav-active");
                    btn.getStyleClass().add("nav-active");
                });
            }

            VBox nav = new VBox(appTitle, navFiles, navShared, navRecent, navTrash, logoutBtn);
            nav.getStyleClass().add("app-sidebar");

            // ── File list ────────────────────────────────────────
            ListView<String> fileList = new ListView<>();
            fileList.setPlaceholder(new Label("No files yet..."));
            fileList.getStyleClass().add("file-table");
            VBox.setVgrow(fileList, Priority.ALWAYS);

            // right-click context menu to delete
            ContextMenu contextMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Delete");
            contextMenu.getItems().add(deleteItem);
            fileList.setContextMenu(contextMenu);

            deleteItem.setOnAction(e -> {
                String selected = fileList.getSelectionModel().getSelectedItem();
                if (selected == null) return;
                new Thread(() -> {
                    try {
                        fc.deleteFile(selected);
                        Platform.runLater(() -> fileList.getItems().remove(selected));
                    } catch (Exception ex) {
                        Platform.runLater(() -> showAlert("Delete failed", ex.getMessage()));
                    }
                }).start();
            });

            // ── Upload ───────────────────────────────────────────
            uploadBtn.setOnAction(e -> {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Choose file to upload");
                File file = chooser.showOpenDialog(stage);
                if (file == null) return;

                new Thread(() -> {
                    try {
                        fc.uploadFile(file.toPath());
                        List<String> files = fc.listFiles();
                        Platform.runLater(() ->
                                fileList.setItems(FXCollections.observableArrayList(files)));
                    } catch (Exception ex) {
                        Platform.runLater(() -> showAlert("Upload failed", ex.getMessage()));
                    }
                }).start();
            });

            // ── Logout ───────────────────────────────────────────
            logoutBtn.setOnAction(e -> new Thread(() -> {
                try {
                    fc.logout();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                Platform.runLater(() -> AppState.navigateTo("login"));
            }).start());

            // ── Load files on open ───────────────────────────────
            new Thread(() -> {
                try {
                    List<String> files = fc.listFiles();
                    Platform.runLater(() ->
                            fileList.setItems(FXCollections.observableArrayList(files)));
                } catch (Exception ex) {
                    Platform.runLater(() -> showAlert("Error loading files", ex.getMessage()));
                }
            }).start();

            // ── Layout ───────────────────────────────────────────
            VBox content = new VBox(fileList);
            VBox.setVgrow(fileList, Priority.ALWAYS);
            content.getStyleClass().add("app-content");

            BorderPane borderPane = new BorderPane();
            borderPane.setTop(header);
            borderPane.setLeft(nav);
            borderPane.setCenter(content);

            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(borderPane, bounds.getWidth(), bounds.getHeight());
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }
}