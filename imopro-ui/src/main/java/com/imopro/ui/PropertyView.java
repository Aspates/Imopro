package com.imopro.ui;

import com.imopro.application.PipelineService;
import com.imopro.application.PropertyService;
import com.imopro.application.RentService;
import com.imopro.domain.Property;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class PropertyView {
    private final PropertyViewModel viewModel;
    private final BorderPane root;

    public PropertyView(PropertyService propertyService, PipelineService pipelineService, PropertyDefaultsStore defaultsStore, RentService rentService, Consumer<UUID> goRent) {
        this.viewModel = new PropertyViewModel(propertyService, pipelineService, defaultsStore, rentService);
        this.root = new BorderPane();
        root.setPadding(new Insets(16));
        root.getStyleClass().add("content");
        root.setLeft(buildListPane());
        root.setCenter(buildDetailPane(goRent));
    }

    public Node getRoot() {
        return root;
    }

    public void refresh() {
        viewModel.loadProperties();
    }

    public void openProperty(UUID id) {
        refresh();
        viewModel.selectById(id);
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

    private Node buildDetailPane(Consumer<UUID> goRent) {
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
        Label estimatedRentLabel = new Label();
        estimatedRentLabel.getStyleClass().add("compact-hint");
        estimatedRentLabel.textProperty().bind(viewModel.estimatedRentLabelProperty());
        ComboBox<String> tfStatus = new ComboBox<>();
        tfStatus.setItems(viewModel.availableStatuses());
        tfStatus.valueProperty().bindBidirectional(viewModel.statusProperty());
        tfStatus.setMaxWidth(Double.MAX_VALUE);

        form.addRow(0, new Label("Titre"), ValidationUtils.attachRegexValidation(tfTitle,
                Pattern.compile("[\\p{L}0-9\\s,.'\\-/#]{1,120}"), true,
                "Lettres/chiffres/ponctuation simple"));
        form.addRow(1, new Label("Adresse"), ValidationUtils.attachRegexValidation(tfAddress,
                Pattern.compile("[\\p{L}0-9\\s,.'\\-/#]{1,180}"), true,
                "Caractères adresse autorisés"));
        form.addRow(2, new Label("Ville"), ValidationUtils.attachRegexValidation(tfCity,
                Pattern.compile("[\\p{L}\\s'\\-]{1,80}"), true,
                "Lettres/espaces/'/- uniquement"));
        form.addRow(3, new Label("CP"), ValidationUtils.attachRegexValidation(tfPostalCode,
                Pattern.compile("\\d{4,10}"), true,
                "Chiffres uniquement (4 à 10)"));
        form.addRow(4, new Label("Type"), ValidationUtils.attachRegexValidation(tfType,
                Pattern.compile("[\\p{L}0-9\\s'\\-]{1,60}"), true,
                "Lettres/chiffres/espaces"));
        form.addRow(5, new Label("Surface"), ValidationUtils.attachRegexValidation(tfSurface,
                Pattern.compile("\\d{1,5}([.,]\\d{1,2})?"), true,
                "Nombre décimal (ex: 45.5)"));
        form.addRow(6, new Label("Pièces"), ValidationUtils.attachRegexValidation(tfRooms,
                Pattern.compile("\\d{1,3}"), true,
                "Nombre entier attendu"));
        Node validatedPriceField = ValidationUtils.attachRegexValidation(tfPrice,
                Pattern.compile("\\d{1,10}([.,]\\d{1,2})?"), true,
                "Nombre (ex: 1200.00)");
        HBox priceWithEstimate = new HBox(10, validatedPriceField, estimatedRentLabel);
        HBox.setHgrow(validatedPriceField, Priority.ALWAYS);
        form.addRow(7, new Label("Prix"), priceWithEstimate);
        form.addRow(8, new Label("Statut"), tfStatus);

        HBox actions = new HBox(12);
        Button saveButton = new Button("Enregistrer");
        saveButton.disableProperty().bind(viewModel.canSaveBinding().not());
        saveButton.setOnAction(event -> viewModel.saveSelected());

        Button deleteButton = new Button("Supprimer");
        deleteButton.disableProperty().bind(viewModel.canSaveBinding().not());
        deleteButton.getStyleClass().add("danger");
        deleteButton.setOnAction(event -> viewModel.deleteSelected());

        Button goRentButton = new Button("Voir loyer");
        goRentButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> viewModel.selectedPropertyRentId() == null,
                viewModel.selectedPropertyProperty()));
        goRentButton.setOnAction(event -> {
            UUID rentId = viewModel.selectedPropertyRentId();
            if (rentId != null) goRent.accept(rentId);
        });

        actions.getChildren().addAll(saveButton, deleteButton, goRentButton);

        container.getChildren().addAll(title, new Separator(), form, actions);

        Label hint = new Label("Sélectionnez ou créez un élément pour modifier la fiche.");
        StackPane placeholder = new StackPane(hint);
        placeholder.setPadding(new Insets(24));

        StackPane wrapper = new StackPane(placeholder, container);
        container.visibleProperty().bind(Bindings.isNotNull(viewModel.selectedPropertyProperty()));
        container.managedProperty().bind(container.visibleProperty());
        placeholder.visibleProperty().bind(container.visibleProperty().not());
        placeholder.managedProperty().bind(placeholder.visibleProperty());
        return wrapper;
    }
}
