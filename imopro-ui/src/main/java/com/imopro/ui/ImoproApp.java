package com.imopro.ui;

import com.imopro.application.ContactService;
import com.imopro.infra.Database;
import com.imopro.application.PropertyService;
import com.imopro.infra.SQLiteContactRepository;
import com.imopro.infra.SQLitePropertyRepository;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
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

        ContactView contactView = new ContactView(contactService);
        PropertyView propertyView = new PropertyView(propertyService);

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

        Label placeholder = new Label("Module en cours de construction");
        StackPane placeholderPane = new StackPane(placeholder);

        contactsButton.setOnAction(event -> {
            contentPane.getChildren().setAll(contactView.getRoot());
        });
        propertiesButton.setOnAction(event -> contentPane.getChildren().setAll(propertyView.getRoot()));
        tasksButton.setOnAction(event -> contentPane.getChildren().setAll(placeholderPane));
        documentsButton.setOnAction(event -> contentPane.getChildren().setAll(placeholderPane));
        pipelineButton.setOnAction(event -> contentPane.getChildren().setAll(placeholderPane));

        sidebar.getChildren().addAll(contactsButton, propertiesButton, tasksButton, documentsButton, pipelineButton);

        BorderPane root = new BorderPane();
        root.setLeft(sidebar);
        root.setCenter(contentPane);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        stage.setTitle("Imopro");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
