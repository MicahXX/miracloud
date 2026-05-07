package org.miracloud.frontend.views;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
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
    private String currentPath = "";

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

            HBox breadcrumb = new HBox(6);
            breadcrumb.setAlignment(Pos.CENTER_LEFT);
            breadcrumb.getStyleClass().add("breadcrumb");

            // On top the files table to name the columns
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

            // New Folder button
            newFolderBtn.setOnAction(e -> {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("New Folder");
                dialog.setHeaderText(null);
                dialog.setContentText("Folder name:");

                dialog.showAndWait().ifPresent(folderName -> {
                    if (folderName.isBlank()) return;
                    new Thread(() -> {
                        try {
                            fc.createFolder(currentPath.isEmpty()
                                    ? folderName
                                    : currentPath + "/" + folderName);
                            List<FileEntry> files = fc.listFiles(currentPath);
                            Platform.runLater(() -> updateList(files, fileList, search));
                        } catch (Exception ex) {
                            Platform.runLater(() -> showAlert("Could not create folder", ex.getMessage()));
                        }
                    }).start();
                });
            });

            fileList.setOnDragOver(event -> {
                if (event.getDragboard().hasFiles())
                    event.acceptTransferModes(TransferMode.COPY);
                event.consume();
            });

            fileList.setOnDragDropped(event -> {
                var db = event.getDragboard();
                if (db.hasFiles()) {
                    for (File droppedFile : db.getFiles()) {
                        new Thread(() -> {
                            try {
                                fc.uploadFile(droppedFile.toPath(), currentPath);
                                List<FileEntry> files = fc.listFiles(currentPath);
                                Platform.runLater(() -> updateList(files, fileList, search));
                            } catch (Exception ex) {
                                Platform.runLater(() -> showAlert("Upload failed", ex.getMessage()));
                            }
                        }).start();
                    }
                }
                event.setDropCompleted(true);
                event.consume();
            });

            // Search
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
                private final HBox row;
                private final Label icon;
                private final Label name;
                private final Label size;
                private final Label date;

                {
                    icon = new Label();
                    icon.getStyleClass().add("file-icon");

                    name = new Label();
                    name.getStyleClass().add("file-name");
                    name.setPrefWidth(340);

                    size = new Label();
                    size.getStyleClass().add("file-meta");
                    size.setPrefWidth(100);

                    date = new Label();
                    date.getStyleClass().add("file-meta");
                    date.setPrefWidth(250);

                    row = new HBox(icon, name, size, date);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setSpacing(10);
                    row.setMaxWidth(Double.MAX_VALUE);
                    row.getStyleClass().add("file-row");

                    row.setOnDragDetected(event -> {
                        FileEntry item = getItem();
                        if (item == null || item.isFolder()) return;
                        Dragboard db = startDragAndDrop(TransferMode.MOVE);
                        ClipboardContent content = new ClipboardContent();
                        content.putString(item.name());
                        db.setContent(content);
                        event.consume();
                    });
                }

                @Override
                protected void updateItem(FileEntry item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                        setStyle("");
                        return;
                    }

                    icon.setText(fileIcon(item.name(), item.isFolder()));
                    name.setText(item.name());
                    size.setText(item.formattedSize());
                    date.setText(item.lastModified());

                    setGraphic(row);
                    setText(null);

                    // accept drop onto folders
                    if (item.isFolder()) {
                        setOnDragOver(event -> {
                            if (event.getDragboard().hasString()) {
                                event.acceptTransferModes(TransferMode.MOVE);
                                setStyle("-fx-background-color: rgba(244,59,158,0.15);");
                            }
                            event.consume();
                        });
                        setOnDragExited(event -> setStyle(""));
                        setOnDragDropped(event -> {
                            String filename = event.getDragboard().getString();
                            setStyle("");
                            new Thread(() -> {
                                try {
                                    String target = currentPath.isEmpty()
                                            ? item.name()
                                            : currentPath + "/" + item.name();
                                    fc.moveFile(filename, target);
                                    List<FileEntry> files = fc.listFiles(currentPath);
                                    Platform.runLater(() -> updateList(files, fileList, search));
                                } catch (Exception ex) {
                                    Platform.runLater(() -> showAlert("Move failed", ex.getMessage()));
                                }
                            }).start();
                            event.setDropCompleted(true);
                            event.consume();
                        });
                    } else {
                        setOnDragOver(null);
                        setOnDragExited(null);
                        setOnDragDropped(null);
                    }
                }
            });

            // Menu
            ContextMenu contextMenu = new ContextMenu();
            MenuItem openItem   = new MenuItem("Open");
            MenuItem renameItem = new MenuItem("Rename");
            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.getStyleClass().add("menu-item-danger");

            contextMenu.getItems().addAll(openItem, renameItem, new SeparatorMenuItem(), deleteItem);
            fileList.setContextMenu(contextMenu);

            // Open
            openItem.setOnAction(e -> openSelected(fileList, breadcrumb, search));
            fileList.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) openSelected(fileList, breadcrumb, search);
            });

            // Rename
            renameItem.setOnAction(e -> {
                FileEntry sel = fileList.getSelectionModel().getSelectedItem();
                if (sel == null) return;

                TextInputDialog dialog = new TextInputDialog(sel.name());
                dialog.setTitle("Rename");
                dialog.setHeaderText(null);
                dialog.setContentText("New name:");

                dialog.showAndWait().ifPresent(newName -> {
                    if (newName.isBlank() || newName.equals(sel.name())) return;
                    String fullOld = currentPath.isEmpty() ? sel.name() : currentPath + "/" + sel.name();
                    String fullNew = currentPath.isEmpty() ? newName : currentPath + "/" + newName;
                    new Thread(() -> {
                        try {
                            fc.renameFile(fullOld, fullNew);
                            List<FileEntry> files = fc.listFiles(currentPath);
                            Platform.runLater(() -> updateList(files, fileList, search));
                        } catch (Exception ex) {
                            Platform.runLater(() -> showAlert("Rename failed", ex.getMessage()));
                        }
                    }).start();
                });
            });

            // Delete
            deleteItem.setOnAction(e -> {
                FileEntry sel = fileList.getSelectionModel().getSelectedItem();
                if (sel == null) return;
                String fullPath = currentPath.isEmpty() ? sel.name() : currentPath + "/" + sel.name();
                new Thread(() -> {
                    try {
                        fc.deleteFile(fullPath);
                        Platform.runLater(() -> {
                            allFiles.remove(sel);
                            fileList.getItems().remove(sel);
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
                        fc.uploadFile(file.toPath(), currentPath);
                        List<FileEntry> files = fc.listFiles(currentPath);
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
                    List<FileEntry> files = fc.listFiles(currentPath);
                    Platform.runLater(() -> {
                        updateList(files, fileList, search);
                        buildBreadcrumb(breadcrumb, fileList, search);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> showAlert("Error loading files", ex.getMessage()));
                }
            }).start();

            VBox listContainer = new VBox(columnHeaders, fileList);
            listContainer.getStyleClass().add("list-container");
            VBox.setVgrow(fileList, Priority.ALWAYS);

            VBox content = new VBox(breadcrumb, listContainer);
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

    private void buildBreadcrumb(HBox breadcrumb, ListView<FileEntry> fileList, TextField search) {
        breadcrumb.getChildren().clear();

        Button rootCrumb = new Button("Files");
        rootCrumb.getStyleClass().add("crumb-btn");
        rootCrumb.setOnAction(e -> {
            currentPath = "";
            buildBreadcrumb(breadcrumb, fileList, search);
            loadFiles(fileList, search);
        });
        breadcrumb.getChildren().add(rootCrumb);

        if (!currentPath.isEmpty()) {
            String[] parts = currentPath.split("/");
            StringBuilder built = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                built.append(i > 0 ? "/" : "").append(part);
                String pathAtThisPoint = built.toString();

                Label sep = new Label("/");
                sep.getStyleClass().add("crumb-sep");

                if (i == parts.length - 1) {
                    Label current = new Label(part);
                    current.getStyleClass().add("crumb-current");
                    breadcrumb.getChildren().addAll(sep, current);
                } else {
                    Button crumb = new Button(part);
                    crumb.getStyleClass().add("crumb-btn");
                    crumb.setOnAction(e -> {
                        currentPath = pathAtThisPoint;
                        buildBreadcrumb(breadcrumb, fileList, search);
                        loadFiles(fileList, search);
                    });
                    breadcrumb.getChildren().addAll(sep, crumb);
                }
            }
        }
    }

    private void loadFiles(ListView<FileEntry> fileList, TextField search) {
        new Thread(() -> {
            try {
                List<FileEntry> files = fc.listFiles(currentPath);
                Platform.runLater(() -> updateList(files, fileList, search));
            } catch (Exception ex) {
                Platform.runLater(() -> showAlert("Error loading files", ex.getMessage()));
            }
        }).start();
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

    private void openSelected(ListView<FileEntry> fileList, HBox breadcrumb, TextField search) {
        FileEntry selected = fileList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (selected.isFolder()) {
            currentPath = currentPath.isEmpty()
                    ? selected.name()
                    : currentPath + "/" + selected.name();
            buildBreadcrumb(breadcrumb, fileList, search);
            loadFiles(fileList, search);
            return;
        }

        // open file
        new Thread(() -> {
            try {
                File localFile = fc.downloadToTemp(selected.name());
                Desktop.getDesktop().open(localFile);
            } catch (Exception ex) {
                Platform.runLater(() -> showAlert("Could not open file", ex.getMessage()));
            }
        }).start();
    }

    private String fileIcon(String name, boolean isFolder) {
        if (isFolder) return "📁";
        String lower = name.toLowerCase();
        if (lower.endsWith(".pdf"))   return "📄";
        if (lower.endsWith(".png")   || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")  || lower.endsWith(".gif")
                || lower.endsWith(".webp"))  return "🖼";
        if (lower.endsWith(".zip")   || lower.endsWith(".rar")
                || lower.endsWith(".7z")    || lower.endsWith(".tar")
                || lower.endsWith(".gz"))    return "📦";
        if (lower.endsWith(".mp4")   || lower.endsWith(".mov")
                || lower.endsWith(".avi")   || lower.endsWith(".mkv")) return "🎬";
        if (lower.endsWith(".mp3")   || lower.endsWith(".wav")
                || lower.endsWith(".flac"))  return "🎵";
        if (lower.endsWith(".html")  || lower.endsWith(".css")
                || lower.endsWith(".js")    || lower.endsWith(".java")
                || lower.endsWith(".py")    || lower.endsWith(".ts"))  return "💻";
        if (lower.endsWith(".txt")   || lower.endsWith(".md"))  return "📝";
        if (lower.endsWith(".xlsx")  || lower.endsWith(".csv")) return "📊";
        if (lower.endsWith(".docx")  || lower.endsWith(".doc")) return "📃";
        return "";
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