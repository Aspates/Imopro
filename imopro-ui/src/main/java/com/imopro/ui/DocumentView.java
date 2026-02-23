package com.imopro.ui;

import com.imopro.application.DocumentService;
import com.imopro.domain.DocumentItem;
import com.imopro.infra.LocalDocumentStorage;
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
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.regex.Pattern;

public class DocumentView {
    private final DocumentViewModel viewModel;
    private final BorderPane root;

    public DocumentView(DocumentService documentService, LocalDocumentStorage storage) {
        this.viewModel = new DocumentViewModel(documentService, storage);
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
        VBox container = new VBox(10);
        container.setPrefWidth(360);

        Label title = new Label("Documents");
        title.getStyleClass().add("section-title");

        TextField search = new TextField();
        search.setPromptText("Rechercher nom/type/chemin");
        search.textProperty().bindBidirectional(viewModel.searchQueryProperty());

        ListView<DocumentItem> listView = new ListView<>();
        listView.setItems(viewModel.getDocuments());
        listView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(DocumentItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : viewModel.display(item));
            }
        });
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                viewModel.selectedDocumentProperty().set(newVal));

        Button importBtn = new Button("Importer un fichier");
        importBtn.setOnAction(e -> {
            Window window = root.getScene() == null ? null : root.getScene().getWindow();
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choisir un document");
            File file = chooser.showOpenDialog(window);
            if (file != null) {
                viewModel.importDocument(file.toPath());
            }
        });

        container.getChildren().addAll(title, search, listView, importBtn);
        VBox.setVgrow(listView, Priority.ALWAYS);
        return container;
    }

    private Node buildDetailPane() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(0, 0, 0, 24));

        Label title = new Label("Fiche document");
        title.getStyleClass().add("section-title");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);

        TextField tfName = new TextField();
        tfName.textProperty().bindBidirectional(viewModel.fileNameProperty());
        TextField tfPath = new TextField();
        tfPath.textProperty().bindBidirectional(viewModel.relativePathProperty());
        tfPath.setEditable(false);
        TextField tfMime = new TextField();
        tfMime.textProperty().bindBidirectional(viewModel.mimeTypeProperty());
        TextField tfSize = new TextField();
        tfSize.textProperty().bindBidirectional(viewModel.sizeBytesProperty());

        form.addRow(0, new Label("Nom"), ValidationUtils.attachRegexValidation(tfName,
                Pattern.compile("[\\p{L}0-9\\s,._'\\-()]{1,140}"), true,
                "Nom fichier alphanumérique + .,_-()"));
        form.addRow(1, new Label("Chemin relatif"), tfPath);
        form.addRow(2, new Label("MIME"), ValidationUtils.attachRegexValidation(tfMime,
                Pattern.compile("[a-zA-Z0-9.+-]+/[a-zA-Z0-9.+-]+"), true,
                "Format mime attendu (type/sous-type)"));
        form.addRow(3, new Label("Taille (octets)"), ValidationUtils.attachRegexValidation(tfSize,
                Pattern.compile("\\d{1,15}"), true,
                "Chiffres uniquement"));

        HBox actions = new HBox(10);
        Button saveBtn = new Button("Enregistrer");
        saveBtn.disableProperty().bind(viewModel.canActBinding().not());
        saveBtn.setOnAction(e -> viewModel.saveSelected());

        Button openBtn = new Button("Ouvrir");
        openBtn.disableProperty().bind(viewModel.canActBinding().not());
        openBtn.setOnAction(e -> viewModel.openSelected());

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().add("danger");
        deleteBtn.disableProperty().bind(viewModel.canActBinding().not());
        deleteBtn.setOnAction(e -> viewModel.deleteSelected());

        actions.getChildren().addAll(saveBtn, openBtn, deleteBtn);

        container.getChildren().addAll(title, new Separator(), form, actions);
        return container;
    }
}
