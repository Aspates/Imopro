package com.imopro.ui;

import com.imopro.application.ContactService;
import com.imopro.application.DocumentService;
import com.imopro.application.PipelineService;
import com.imopro.application.PropertyService;
import com.imopro.application.TaskService;
import com.imopro.infra.Database;
import com.imopro.infra.LocalDocumentStorage;
import com.imopro.infra.SQLiteContactRepository;
import com.imopro.infra.SQLiteDocumentRepository;
import com.imopro.infra.SQLitePipelineRepository;
import com.imopro.infra.SQLitePropertyRepository;
import com.imopro.infra.SQLiteTaskRepository;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ImoproApp extends Application {
    @Override
    public void start(Stage stage) {
        Database database = new Database();
        database.migrate();

        ContactService contactService = new ContactService(new SQLiteContactRepository(database));
        PropertyService propertyService = new PropertyService(new SQLitePropertyRepository(database));
        TaskService taskService = new TaskService(new SQLiteTaskRepository(database));
        DocumentService documentService = new DocumentService(new SQLiteDocumentRepository(database));
        PipelineService pipelineService = new PipelineService(new SQLitePipelineRepository(database));
        LocalDocumentStorage documentStorage = new LocalDocumentStorage();
        PropertyDefaultsStore defaultsStore = new PropertyDefaultsStore();

        ContactView contactView = new ContactView(contactService);
        PropertyView propertyView = new PropertyView(propertyService, pipelineService, defaultsStore);
        TaskView taskView = new TaskView(taskService);
        DocumentView documentView = new DocumentView(documentService, documentStorage);
        PipelineView pipelineView = new PipelineView(pipelineService);

        StackPane contentPane = new StackPane();
        contentPane.getChildren().add(contactView.getRoot());

        VBox sidebar = new VBox(12);
        sidebar.setPadding(new Insets(16));
        sidebar.getStyleClass().add("sidebar");

        Button contactsButton = new Button("Contacts");
        Button propertiesButton = new Button("Biens");
        Button tasksButton = new Button("Tâches");
        Button documentsButton = new Button("Documents");
        Button pipelineButton = new Button("Pipeline");

        contactsButton.setMaxWidth(Double.MAX_VALUE);
        propertiesButton.setMaxWidth(Double.MAX_VALUE);
        tasksButton.setMaxWidth(Double.MAX_VALUE);
        documentsButton.setMaxWidth(Double.MAX_VALUE);
        pipelineButton.setMaxWidth(Double.MAX_VALUE);

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

        sidebar.getChildren().addAll(contactsButton, propertiesButton, tasksButton, documentsButton, pipelineButton);

        MenuBar menuBar = buildMenuBar(defaultsStore);

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

    private MenuBar buildMenuBar(PropertyDefaultsStore defaultsStore) {
        MenuBar menuBar = new MenuBar();
        Menu settingsMenu = new Menu("Paramètres");
        MenuItem defaultsItem = new MenuItem("Valeurs par défaut");
        defaultsItem.setOnAction(event -> openDefaultsDialog(defaultsStore));
        settingsMenu.getItems().add(defaultsItem);
        menuBar.getMenus().add(settingsMenu);
        return menuBar;
    }

    private void openDefaultsDialog(PropertyDefaultsStore defaultsStore) {
        PropertyDefaultsStore.PropertyDefaults defaults = defaultsStore.load();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Valeurs par défaut - Module Bien");
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

        grid.addRow(0, new Label("Adresse"), addressField);
        grid.addRow(1, new Label("Ville"), cityField);
        grid.addRow(2, new Label("CP"), postalField);
        grid.addRow(3, new Label("Type"), typeField);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(result -> {
            if (result == saveType) {
                defaultsStore.save(new PropertyDefaultsStore.PropertyDefaults(
                        addressField.getText() == null ? "" : addressField.getText(),
                        cityField.getText() == null ? "" : cityField.getText(),
                        postalField.getText() == null ? "" : postalField.getText(),
                        typeField.getText() == null ? "" : typeField.getText()
                ));
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
