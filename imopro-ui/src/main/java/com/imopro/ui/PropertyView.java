package com.imopro.ui;

import com.imopro.application.PropertyService;
import com.imopro.domain.Property;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class PropertyView {
    private final PropertyViewModel viewModel;
    private final BorderPane root;

    public PropertyView(PropertyService propertyService) {
        this.viewModel = new PropertyViewModel(propertyService);
        this.root = new BorderPane();
        root.setPadding(new Insets(16));
        root.getStyleClass().add("content");
        root.setLeft(buildListPane());
        root.setCenter(buildDetailPane());
    }

    public Node getRoot() {
        return root;
    }

    private Node buildListPane() {
        VBox container = new VBox(12);
        container.setPrefWidth(320);

        Label title = new Label("Biens");
        title.getStyleClass().add("section-title");

        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher un bien");
        searchField.textProperty().bindBidirectional(viewModel.searchQueryProperty());

        ListView<Property> listView = new ListView<>();
        listView.setItems(viewModel.getProperties());
        listView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Property item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : viewModel.display(item));
            }
        });
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> viewModel.selectedPropertyProperty().set(newV));

        Button addButton = new Button("Nouveau bien");
        addButton.setOnAction(event -> viewModel.createProperty());

        container.getChildren().addAll(title, searchField, listView, addButton);
        VBox.setVgrow(listView, Priority.ALWAYS);
        return container;
    }

    private Node buildDetailPane() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(0, 0, 0, 24));

        Label title = new Label("Fiche bien");
        title.getStyleClass().add("section-title");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);

        TextField tfTitle = new TextField();
        tfTitle.textProperty().bindBidirectional(viewModel.titleProperty());
        TextField tfAddress = new TextField();
        tfAddress.textProperty().bindBidirectional(viewModel.addressProperty());
        TextField tfCity = new TextField();
        tfCity.textProperty().bindBidirectional(viewModel.cityProperty());
        TextField tfPostalCode = new TextField();
        tfPostalCode.textProperty().bindBidirectional(viewModel.postalCodeProperty());
        TextField tfType = new TextField();
        tfType.textProperty().bindBidirectional(viewModel.propertyTypeProperty());
        TextField tfSurface = new TextField();
        tfSurface.textProperty().bindBidirectional(viewModel.surfaceProperty());
        TextField tfRooms = new TextField();
        tfRooms.textProperty().bindBidirectional(viewModel.roomsProperty());
        TextField tfPrice = new TextField();
        tfPrice.textProperty().bindBidirectional(viewModel.priceProperty());
        TextField tfStatus = new TextField();
        tfStatus.textProperty().bindBidirectional(viewModel.statusProperty());

        form.addRow(0, new Label("Titre"), tfTitle);
        form.addRow(1, new Label("Adresse"), tfAddress);
        form.addRow(2, new Label("Ville"), tfCity);
        form.addRow(3, new Label("CP"), tfPostalCode);
        form.addRow(4, new Label("Type"), tfType);
        form.addRow(5, new Label("Surface"), tfSurface);
        form.addRow(6, new Label("Pièces"), tfRooms);
        form.addRow(7, new Label("Prix"), tfPrice);
        form.addRow(8, new Label("Statut"), tfStatus);

        HBox actions = new HBox(12);
        Button saveButton = new Button("Enregistrer");
        saveButton.disableProperty().bind(viewModel.canSaveBinding().not());
        saveButton.setOnAction(event -> viewModel.saveSelected());

        Button deleteButton = new Button("Supprimer");
        deleteButton.disableProperty().bind(viewModel.canSaveBinding().not());
        deleteButton.getStyleClass().add("danger");
        deleteButton.setOnAction(event -> viewModel.deleteSelected());

        actions.getChildren().addAll(saveButton, deleteButton);

        container.getChildren().addAll(title, new Separator(), form, actions);
        return container;
    }
}
