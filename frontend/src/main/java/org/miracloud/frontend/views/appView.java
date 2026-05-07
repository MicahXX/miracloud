package org.miracloud.frontend.views;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.miracloud.frontend.AppState;
import org.miracloud.frontend.controller.appController;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class appView {

    private final appController fc = new appController();
    private List<FileEntry> allFiles = new ArrayList<>();

    public Scene buildScene() {
        try {
            Stage stage = AppState.getStage();

            // SearchField and Upload/New Folder Button
            TextField search = new TextField();
            search.setPromptText("Search files...");
            search.getStyleClass().add("search-field");
            HBox.setHgrow(search, Priority.ALWAYS);

            Button uploadBtn = new Button("Upload");
            uploadBtn.getStyleClass().add("btn-primary");

            // todo: make this button create a folder
            Button newFolderBtn = new Button("New Folder");
            newFolderBtn.getStyleClass().add("btn-secondary");

            HBox header = new HBox(search, uploadBtn, newFolderBtn);
            header.getStyleClass().add("app-header");

            // Files etc. Sidebar on left
            Label appTitle = new Label("miracloud");
            appTitle.getStyleClass().add("app-title");

            Button navFiles  = navButton("Files");
            // todo: make these do smth
            Button navShared = navButton("Shared");
            Button navRecent = navButton("Recent");
            Button navTrash  = navButton("Trash");
            navFiles.getStyleClass().add("nav-active");

            Button logoutBtn = new Button("Logout");
            logoutBtn.getStyleClass().addAll("btn-secondary", "logout-btn");

            Button[] navButtons = {navFiles, navShared, navRecent, navTrash};
            for (Button btn : navButtons) {
                btn.setOnAction(e -> {
                    for (Button b : navButtons) b.getStyleClass().remove("nav-active");
                    btn.getStyleClass().add("nav-active");
                });
            }

            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);

            VBox nav = new VBox(appTitle, navFiles, navShared, navRecent, navTrash, spacer, logoutBtn);
            nav.getStyleClass().add("app-sidebar");

            // On top thw files table to name the columns
            Label colName = new Label("Name");
            colName.getStyleClass().add("col-header");
            colName.setPrefWidth(350);

            Label colSize = new Label("Size");
            colSize.getStyleClass().add("col-header");
            colSize.setPrefWidth(120);

            Label colDate = new Label("Uploaded");
            colDate.getStyleClass().add("col-header");
            colDate.setPrefWidth(200);

            HBox columnHeaders = new HBox(colName, colSize, colDate);
            columnHeaders.getStyleClass().add("col-header-row");
            columnHeaders.setSpacing(0);

            // Files
            ListView<FileEntry> fileList = new ListView<>();
            fileList.setPlaceholder(new Label("No files yet..."));
            fileList.getStyleClass().add("file-list");
            VBox.setVgrow(fileList, Priority.ALWAYS);
            fileList.setMaxWidth(Double.MAX_VALUE);

            search.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null || newVal.isBlank()) {
                    fileList.setItems(FXCollections.observableArrayList(allFiles));
                } else {
                    String lower = newVal.toLowerCase();
                    fileList.setItems(FXCollections.observableArrayList(
                            allFiles.stream()
                                    .filter(f -> f.name().toLowerCase().contains(lower))
                                    .toList()
                    ));
                }
            });

            fileList.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(FileEntry item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                        return;
                    }
                    Label icon = new Label(fileIcon(item.name()));
                    icon.getStyleClass().add("file-icon");

                    Label name = new Label(item.name());
                    name.getStyleClass().add("file-name");
                    name.setPrefWidth(300);

                    Label size = new Label(item.formattedSize());
                    size.getStyleClass().add("file-meta");
                    size.setPrefWidth(120);

                    Label date = new Label(item.lastModified());
                    date.getStyleClass().add("file-meta");
                    date.setPrefWidth(200);

                    HBox row = new HBox(icon, name, size, date);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setSpacing(10);
                    row.setMaxWidth(Double.MAX_VALUE);
                    row.getStyleClass().add("file-row");

                    setGraphic(row);
                    setText(null);
                }
            });

            // Menu that opens when you click on files
            ContextMenu contextMenu = new ContextMenu();
            MenuItem openItem   = new MenuItem("Open");
            MenuItem renameItem = new MenuItem("Rename");
            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.getStyleClass().add("menu-item-danger");

            contextMenu.getItems().addAll(openItem, renameItem, new SeparatorMenuItem(), deleteItem);
            fileList.setContextMenu(contextMenu);

            // Open
            openItem.setOnAction(e -> openSelected(fileList));
            fileList.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) openSelected(fileList);
            });

            // Rename
            renameItem.setOnAction(e -> {
                FileEntry selected = fileList.getSelectionModel().getSelectedItem();
                if (selected == null) return;

                TextInputDialog dialog = new TextInputDialog(selected.name());
                dialog.setTitle("Rename");
                dialog.setHeaderText(null);
                dialog.setContentText("New name:");

                dialog.showAndWait().ifPresent(newName -> {
                    if (newName.isBlank() || newName.equals(selected.name())) return;
                    new Thread(() -> {
                        try {
                            fc.renameFile(selected.name(), newName);
                            List<FileEntry> files = fc.listFiles();
                            Platform.runLater(() -> updateList(files, fileList, search));
                        } catch (Exception ex) {
                            Platform.runLater(() -> showAlert("Rename failed", ex.getMessage()));
                        }
                    }).start();
                });
            });

            // Delete
            deleteItem.setOnAction(e -> {
                FileEntry selected = fileList.getSelectionModel().getSelectedItem();
                if (selected == null) return;
                new Thread(() -> {
                    try {
                        fc.deleteFile(selected.name());
                        Platform.runLater(() -> {
                            allFiles.remove(selected);
                            fileList.getItems().remove(selected);
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> showAlert("Delete failed", ex.getMessage()));
                    }
                }).start();
            });

            // Upload
            uploadBtn.setOnAction(e -> {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Choose file to upload");
                File file = chooser.showOpenDialog(stage);
                if (file == null) return;

                new Thread(() -> {
                    try {
                        fc.uploadFile(file.toPath());
                        List<FileEntry> files = fc.listFiles();
                        Platform.runLater(() -> updateList(files, fileList, search));
                    } catch (Exception ex) {
                        Platform.runLater(() -> showAlert("Upload failed", ex.getMessage()));
                    }
                }).start();
            });

            // Logout
            logoutBtn.setOnAction(e -> new Thread(() -> {
                try {
                    fc.logout();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                Platform.runLater(() -> AppState.navigateTo("login"));
            }).start());

            // Load files on open
            new Thread(() -> {
                try {
                    List<FileEntry> files = fc.listFiles();
                    Platform.runLater(() -> updateList(files, fileList, search));
                } catch (Exception ex) {
                    Platform.runLater(() -> showAlert("Error loading files", ex.getMessage()));
                }
            }).start();

            VBox listContainer = new VBox(columnHeaders, fileList);
            listContainer.getStyleClass().add("list-container");
            VBox.setVgrow(fileList, Priority.ALWAYS);

            VBox content = new VBox(listContainer);
            content.getStyleClass().add("app-content");
            VBox.setVgrow(listContainer, Priority.ALWAYS);
            listContainer.setMaxWidth(Double.MAX_VALUE);
            listContainer.setMaxHeight(Double.MAX_VALUE);

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

    private void updateList(List<FileEntry> files, ListView<FileEntry> fileList, TextField search) {
        allFiles = new ArrayList<>(files);
        String q = search.getText();
        if (q != null && !q.isBlank()) {
            String lower = q.toLowerCase();
            fileList.setItems(FXCollections.observableArrayList(
                    allFiles.stream().filter(f -> f.name().toLowerCase().contains(lower)).toList()
            ));
        } else {
            fileList.setItems(FXCollections.observableArrayList(allFiles));
        }
    }

    private void openSelected(ListView<FileEntry> fileList) {
        FileEntry selected = fileList.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        new Thread(() -> {
            try {
                File localFile = fc.downloadToTemp(selected.name());
                Desktop.getDesktop().open(localFile);
            } catch (Exception ex) {
                Platform.runLater(() -> showAlert("Could not open file", ex.getMessage()));
            }
        }).start();
    }

    private String fileIcon(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".pdf"))  return "📄";
        if (lower.endsWith(".png")  || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg") || lower.endsWith(".gif")
                || lower.endsWith(".webp")) return "🖼";
        if (lower.endsWith(".zip")  || lower.endsWith(".rar")
                || lower.endsWith(".7z")   || lower.endsWith(".tar")
                || lower.endsWith(".gz"))   return "📦";
        if (lower.endsWith(".mp4")  || lower.endsWith(".mov")
                || lower.endsWith(".avi")  || lower.endsWith(".mkv")) return "🎬";
        if (lower.endsWith(".mp3")  || lower.endsWith(".wav")
                || lower.endsWith(".flac")) return "🎵";
        if (lower.endsWith(".html") || lower.endsWith(".css")
                || lower.endsWith(".js")   || lower.endsWith(".java")
                || lower.endsWith(".py")   || lower.endsWith(".ts"))  return "💻";
        if (lower.endsWith(".txt")  || lower.endsWith(".md"))  return "📝";
        if (lower.endsWith(".xlsx") || lower.endsWith(".csv")) return "📊";
        if (lower.endsWith(".docx") || lower.endsWith(".doc")) return "📃";
        return "📁";
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