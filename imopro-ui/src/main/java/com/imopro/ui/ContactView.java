package com.imopro.ui;

import com.imopro.application.ContactService;
import com.imopro.domain.Contact;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.UUID;
import java.util.regex.Pattern;

public class ContactView {
    private final ContactViewModel viewModel;
    private final BorderPane root;

    public ContactView(ContactService contactService) {
        this.viewModel = new ContactViewModel(contactService);
        this.root = new BorderPane();
        root.setPadding(new Insets(16));
        root.getStyleClass().add("content");
        root.setLeft(buildListPane());
        root.setCenter(buildDetailPane());
    }

    public Node getRoot() {
        return root;
    }

    public void refresh() {
        viewModel.loadContacts();
    }

    public void openContact(UUID id) {
        refresh();
        viewModel.selectById(id);
    }

    private Node buildListPane() {
        VBox container = new VBox(12);
        container.setPrefWidth(320);

        Label title = new Label("Contacts");
        title.getStyleClass().add("section-title");

        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher par nom, email, téléphone");
        searchField.textProperty().bindBidirectional(viewModel.searchQueryProperty());

        ListView<Contact> listView = new ListView<>();
        listView.setItems(viewModel.getContacts());
        listView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Contact item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(viewModel.contactDisplayName(item));
                }
            }
        });
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) ->
                viewModel.selectedContactProperty().set(newValue));

        Button addButton = new Button("Nouveau");
        addButton.setOnAction(event -> viewModel.createContact());

        container.getChildren().addAll(title, searchField, listView, addButton);
        VBox.setVgrow(listView, Priority.ALWAYS);
        return container;
    }

    private Node buildDetailPane() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(0, 0, 0, 24));

        Label title = new Label("Fiche contact");
        title.getStyleClass().add("section-title");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);

        TextField firstName = new TextField();
        firstName.textProperty().bindBidirectional(viewModel.firstNameProperty());
        TextField lastName = new TextField();
        lastName.textProperty().bindBidirectional(viewModel.lastNameProperty());
        TextField phone = new TextField();
        phone.textProperty().bindBidirectional(viewModel.phoneProperty());
        TextField email = new TextField();
        email.textProperty().bindBidirectional(viewModel.emailProperty());
        TextField address = new TextField();
        address.textProperty().bindBidirectional(viewModel.addressProperty());
        TextArea notes = new TextArea();
        notes.textProperty().bindBidirectional(viewModel.notesProperty());
        notes.setPrefRowCount(6);

        form.addRow(0, new Label("Prénom"), ValidationUtils.attachRegexValidation(firstName,
                Pattern.compile("[\\p{L}0-9\\s'\\-]{1,80}"), true,
                "Lettres/chiffres/espaces/'/- uniquement"));
        form.addRow(1, new Label("Nom"), ValidationUtils.attachRegexValidation(lastName,
                Pattern.compile("[\\p{L}0-9\\s'\\-]{1,80}"), true,
                "Lettres/chiffres/espaces/'/- uniquement"));
        form.addRow(2, new Label("Téléphone"), ValidationUtils.attachRegexValidation(phone,
                Pattern.compile("[0-9+()\\s\\-]{6,20}"), true,
                "Chiffres et + ( ) - uniquement"));
        form.addRow(3, new Label("Email"), ValidationUtils.attachRegexValidation(email,
                Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"), true,
                "Format email attendu (ex: nom@domaine.fr)"));
        form.addRow(4, new Label("Adresse"), ValidationUtils.attachRegexValidation(address,
                Pattern.compile("[\\p{L}0-9\\s,.'\\-/#]{1,180}"), true,
                "Caractères adresse autorisés"));
        form.addRow(5, new Label("Notes"), ValidationUtils.attachValidation(notes,
                value -> value.length() <= 1000, true,
                "1000 caractères max"));

        HBox actions = new HBox(12);
        Button saveButton = new Button("Enregistrer");
        saveButton.disableProperty().bind(viewModel.canSaveBinding().not());
        saveButton.setOnAction(event -> viewModel.saveContact());

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("danger");
        deleteButton.disableProperty().bind(viewModel.canDeleteBinding().not());
        deleteButton.setOnAction(event -> viewModel.deleteSelectedContact());

        actions.getChildren().addAll(saveButton, deleteButton);

        container.getChildren().addAll(title, new Separator(), form, actions);

        Label hint = new Label("Sélectionnez ou créez un élément pour modifier la fiche.");
        StackPane placeholder = new StackPane(hint);
        placeholder.setPadding(new Insets(24));

        StackPane wrapper = new StackPane(placeholder, container);
        container.visibleProperty().bind(Bindings.isNotNull(viewModel.selectedContactProperty()));
        container.managedProperty().bind(container.visibleProperty());
        placeholder.visibleProperty().bind(container.visibleProperty().not());
        placeholder.managedProperty().bind(placeholder.visibleProperty());
        return wrapper;
    }
}
