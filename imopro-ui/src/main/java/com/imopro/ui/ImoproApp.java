package com.imopro.ui;

import com.imopro.application.ContactService;
import com.imopro.application.DocumentService;
import com.imopro.application.PipelineService;
import com.imopro.application.PropertyService;
import com.imopro.application.RentService;
import com.imopro.application.TaskService;
import com.imopro.infra.DataPaths;
import com.imopro.infra.Database;
import com.imopro.infra.LocalDocumentStorage;
import com.imopro.infra.SQLiteContactRepository;
import com.imopro.infra.SQLiteDocumentRepository;
import com.imopro.infra.SQLitePipelineRepository;
import com.imopro.infra.SQLitePropertyRepository;
import com.imopro.infra.SQLiteRentRepository;
import com.imopro.infra.SQLiteTaskRepository;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ImoproApp extends Application {
    @Override
    public void start(Stage stage) {
        Database database = new Database();
        database.migrate();

        ContactService contactService = new ContactService(new SQLiteContactRepository(database));
        PropertyService propertyService = new PropertyService(new SQLitePropertyRepository(database));
        TaskService taskService = new TaskService(new SQLiteTaskRepository(database));
        RentService rentService = new RentService(new SQLiteRentRepository(database));
        DocumentService documentService = new DocumentService(new SQLiteDocumentRepository(database));
        PipelineService pipelineService = new PipelineService(new SQLitePipelineRepository(database));
        LocalDocumentStorage documentStorage = new LocalDocumentStorage();
        PropertyDefaultsStore defaultsStore = new PropertyDefaultsStore();

        StackPane contentPane = new StackPane();

        final ContactView[] contactViewRef = new ContactView[1];
        DocumentView documentView = new DocumentView(documentService, documentStorage);
        PipelineView pipelineView = new PipelineView(pipelineService);
        final PropertyView[] propertyViewRef = new PropertyView[1];
        final TaskView[] taskViewRef = new TaskView[1];
        final RentView[] rentViewRef = new RentView[1];
        ContactView contactView = new ContactView(contactService, rentService, rentId -> {
            rentViewRef[0].openRent(rentId);
            contentPane.getChildren().setAll(rentViewRef[0].getRoot());
        });
        contactViewRef[0] = contactView;
        RentView rentView = new RentView(rentService, contactService, propertyService, taskService, documentService, documentStorage,
                contactId -> {
                    contactViewRef[0].openContact(contactId);
                    contentPane.getChildren().setAll(contactViewRef[0].getRoot());
                },
                propertyId -> {
                    propertyViewRef[0].openProperty(propertyId);
                    contentPane.getChildren().setAll(propertyViewRef[0].getRoot());
                },
                taskId -> {
                    taskViewRef[0].openTask(taskId);
                    contentPane.getChildren().setAll(taskViewRef[0].getRoot());
                });
        rentViewRef[0] = rentView;
        PropertyView propertyView = new PropertyView(propertyService, pipelineService, defaultsStore, rentService, rentId -> {
            rentViewRef[0].openRent(rentId);
            contentPane.getChildren().setAll(rentViewRef[0].getRoot());
        });
        propertyViewRef[0] = propertyView;
        TaskView taskView = new TaskView(taskService, rentService, rentId -> {
            rentViewRef[0].openRent(rentId);
            contentPane.getChildren().setAll(rentViewRef[0].getRoot());
        });
        taskViewRef[0] = taskView;

        contentPane.getChildren().add(contactView.getRoot());

        VBox sidebar = new VBox(12);
        sidebar.setPadding(new Insets(16));
        sidebar.getStyleClass().add("sidebar");

        Button contactsButton = new Button("Contacts");
        Button propertiesButton = new Button("Biens");
        Button tasksButton = new Button("T\u00E2ches");
        Button documentsButton = new Button("Documents");
        Button pipelineButton = new Button("Pipeline");
        Button rentsButton = new Button("Loyers");

        contactsButton.setMaxWidth(Double.MAX_VALUE);
        propertiesButton.setMaxWidth(Double.MAX_VALUE);
        tasksButton.setMaxWidth(Double.MAX_VALUE);
        documentsButton.setMaxWidth(Double.MAX_VALUE);
        pipelineButton.setMaxWidth(Double.MAX_VALUE);
        rentsButton.setMaxWidth(Double.MAX_VALUE);

        contactsButton.setOnAction(event -> contentPane.getChildren().setAll(contactView.getRoot()));
        propertiesButton.setOnAction(event -> {
            propertyView.refresh();
            contentPane.getChildren().setAll(propertyView.getRoot());
        });
        tasksButton.setOnAction(event -> contentPane.getChildren().setAll(taskView.getRoot()));
        documentsButton.setOnAction(event -> contentPane.getChildren().setAll(documentView.getRoot()));
        pipelineButton.setOnAction(event -> {
            pipelineView.refresh();
            contentPane.getChildren().setAll(pipelineView.getRoot());
        });
        rentsButton.setOnAction(event -> {
            rentView.refresh();
            contentPane.getChildren().setAll(rentView.getRoot());
        });

        sidebar.getChildren().addAll(contactsButton, propertiesButton, tasksButton, documentsButton, pipelineButton, rentsButton);

        MenuBar menuBar = buildMenuBar(defaultsStore, stage);

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setLeft(sidebar);
        root.setCenter(contentPane);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        stage.setTitle("Imopro");
        stage.setScene(scene);
        stage.show();
    }

    private MenuBar buildMenuBar(PropertyDefaultsStore defaultsStore, Stage stage) {
        MenuBar menuBar = new MenuBar();
        Menu settingsMenu = new Menu("Param\u00E8tres");
        MenuItem defaultsItem = new MenuItem("Valeurs par d\u00E9faut");
        defaultsItem.setOnAction(event -> openDefaultsDialog(defaultsStore));
        MenuItem backupItem = new MenuItem("Voir sauvegarde");
        backupItem.setOnAction(event -> openBackupDialog(stage));
        settingsMenu.getItems().addAll(defaultsItem, backupItem);
        menuBar.getMenus().add(settingsMenu);
        return menuBar;
    }

    private void openBackupDialog(Stage owner) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Sauvegarde");
        dialog.initOwner(owner);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        Path[] currentBackupDir = new Path[]{DataPaths.ensureDataDir()};
        Label pathLabel = new Label(currentBackupDir[0].toAbsolutePath().toString());
        pathLabel.setWrapText(true);

        Button openButton = new Button("Ouvrir r\u00E9pertoire");
        openButton.setOnAction(event -> openDirectoryInExplorer(currentBackupDir[0]));

        Button editButton = new Button("Modifier");
        editButton.setOnAction(event -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Choisir le r\u00E9pertoire de sauvegarde");
            File initial = resolveInitialDirectory(currentBackupDir[0]);
            if (initial != null) {
                chooser.setInitialDirectory(initial);
            }

            File selected = chooser.showDialog(owner);
            if (selected != null) {
                try {
                    DataPaths.setDataDir(selected.toPath());
                    currentBackupDir[0] = DataPaths.ensureDataDir();
                    pathLabel.setText(currentBackupDir[0].toAbsolutePath().toString());
                } catch (IllegalStateException e) {
                    showError("Impossible de modifier le r\u00E9pertoire de sauvegarde.", e.getMessage());
                }
            }
        });

        Label hint = new Label("Le changement de r\u00E9pertoire est appliqu\u00E9 pour les prochaines ouvertures de l'application.");
        hint.setWrapText(true);

        HBox actions = new HBox(8, openButton, editButton);
        VBox content = new VBox(10,
                new Label("R\u00E9pertoire de sauvegarde des informations :"),
                pathLabel,
                actions,
                hint
        );
        content.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(content);

        dialog.showAndWait();
    }

    private File resolveInitialDirectory(Path currentPath) {
        if (currentPath != null) {
            Path absolute = currentPath.toAbsolutePath();
            if (Files.isDirectory(absolute)) {
                return absolute.toFile();
            }
            Path parent = absolute.getParent();
            if (parent != null && Files.isDirectory(parent)) {
                return parent.toFile();
            }
        }
        Path home = Path.of(System.getProperty("user.home"));
        return Files.isDirectory(home) ? home.toFile() : null;
    }

    private void openDirectoryInExplorer(Path directory) {
        if (directory == null) {
            showError("Impossible d'ouvrir le r\u00E9pertoire.", "R\u00E9pertoire invalide.");
            return;
        }

        try {
            Path ensuredDir = Files.createDirectories(directory);
            if (!Desktop.isDesktopSupported()) {
                showError("Impossible d'ouvrir le r\u00E9pertoire.", "Desktop n'est pas support\u00E9 sur ce syst\u00E8me.");
                return;
            }
            Desktop.getDesktop().open(ensuredDir.toFile());
        } catch (IOException e) {
            showError("Impossible d'ouvrir le r\u00E9pertoire.", e.getMessage());
        }
    }

    private void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(header);
        alert.setContentText(message == null ? "Une erreur est survenue." : message);
        alert.showAndWait();
    }

    private void openDefaultsDialog(PropertyDefaultsStore defaultsStore) {
        PropertyDefaultsStore.PropertyDefaults defaults = defaultsStore.load();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Valeurs par d\u00E9faut - Module Bien");
        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField addressField = new TextField(defaults.address());
        TextField cityField = new TextField(defaults.city());
        TextField postalField = new TextField(defaults.postalCode());
        TextField typeField = new TextField(defaults.propertyType());
        TextField rentPerSqmField = new TextField(defaults.rentPerSquareMeter());

        grid.addRow(0, new Label("Adresse"), addressField);
        grid.addRow(1, new Label("Ville"), cityField);
        grid.addRow(2, new Label("CP"), postalField);
        grid.addRow(3, new Label("Type"), typeField);
        grid.addRow(4, new Label("Loyer au m²"), rentPerSqmField);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(result -> {
            if (result == saveType) {
                defaultsStore.save(new PropertyDefaultsStore.PropertyDefaults(
                        addressField.getText() == null ? "" : addressField.getText(),
                        cityField.getText() == null ? "" : cityField.getText(),
                        postalField.getText() == null ? "" : postalField.getText(),
                        typeField.getText() == null ? "" : typeField.getText(),
                        rentPerSqmField.getText() == null ? "" : rentPerSqmField.getText()
                ));
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
