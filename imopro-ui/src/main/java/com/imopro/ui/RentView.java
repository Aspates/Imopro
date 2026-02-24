package com.imopro.ui;

import com.imopro.application.ContactService;
import com.imopro.application.DocumentService;
import com.imopro.application.PropertyService;
import com.imopro.application.RentService;
import com.imopro.application.TaskService;
import com.imopro.domain.Contact;
import com.imopro.domain.DocumentItem;
import com.imopro.domain.Property;
import com.imopro.domain.Rent;
import com.imopro.domain.RentTaskRule;
import com.imopro.domain.TaskItem;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.UUID;
import java.util.function.Consumer;

public class RentView {
    private final RentViewModel viewModel;
    private final BorderPane root;

    public RentView(RentService rentService,
                    ContactService contactService,
                    PropertyService propertyService,
                    TaskService taskService,
                    DocumentService documentService,
                    Consumer<UUID> goContact,
                    Consumer<UUID> goProperty,
                    Consumer<UUID> goTask,
                    Consumer<UUID> goDocument) {
        this.viewModel = new RentViewModel(rentService, contactService, propertyService, taskService, documentService);
        this.root = new BorderPane();
        root.setPadding(new Insets(16));
        root.getStyleClass().add("content");
        root.setLeft(buildListPane());
        root.setCenter(buildDetailPane(goContact, goProperty, goTask, goDocument));
    }

    public Node getRoot() { return root; }
    public void refresh() { viewModel.refreshAll(); }

    private Node buildListPane() {
        VBox box = new VBox(10);
        box.setPrefWidth(320);
        Label t = new Label("Loyers");
        t.getStyleClass().add("section-title");

        ListView<Rent> list = new ListView<>(viewModel.rents());
        list.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Rent item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : viewModel.rentDisplay(item));
            }
        });
        list.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> viewModel.selectedRentProperty().set(b));

        Button add = new Button("Nouveau loyer");
        add.setOnAction(e -> viewModel.createRent());
        box.getChildren().addAll(t, list, add);
        VBox.setVgrow(list, Priority.ALWAYS);
        return box;
    }

    private Node buildDetailPane(Consumer<UUID> goContact, Consumer<UUID> goProperty, Consumer<UUID> goTask, Consumer<UUID> goDocument) {
        VBox c = new VBox(10);
        c.setPadding(new Insets(0, 0, 0, 24));
        Label t = new Label("Fiche loyer");
        t.getStyleClass().add("section-title");

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(10);

        ComboBox<Contact> contact = new ComboBox<>(viewModel.contacts());
        contact.setCellFactory(lv -> new ListCell<>() { @Override protected void updateItem(Contact i, boolean e) { super.updateItem(i, e); setText(e || i == null ? null : i.displayName()); }});
        contact.setButtonCell(new ListCell<>() { @Override protected void updateItem(Contact i, boolean e) { super.updateItem(i, e); setText(e || i == null ? null : i.displayName()); }});
        contact.valueProperty().bindBidirectional(viewModel.selectedContactProperty());

        ComboBox<Property> property = new ComboBox<>(viewModel.properties());
        property.setCellFactory(lv -> new ListCell<>() { @Override protected void updateItem(Property i, boolean e) { super.updateItem(i, e); setText(e || i == null ? null : i.displayTitle()); }});
        property.setButtonCell(new ListCell<>() { @Override protected void updateItem(Property i, boolean e) { super.updateItem(i, e); setText(e || i == null ? null : i.displayTitle()); }});
        property.valueProperty().bindBidirectional(viewModel.selectedPropertyProperty());

        TextField amount = new TextField(); amount.textProperty().bindBidirectional(viewModel.amountProperty());
        DatePicker start = new DatePicker(); start.valueProperty().bindBidirectional(viewModel.startDateProperty());
        DatePicker end = new DatePicker(); end.valueProperty().bindBidirectional(viewModel.endDateProperty());
        TextField notes = new TextField(); notes.textProperty().bindBidirectional(viewModel.notesProperty());

        form.addRow(0, new Label("Locataire"), contact);
        form.addRow(1, new Label("Bien"), property);
        form.addRow(2, new Label("Montant mensuel"), amount);
        form.addRow(3, new Label("Début"), start);
        form.addRow(4, new Label("Fin"), end);
        form.addRow(5, new Label("Notes"), notes);

        HBox actions = new HBox(10);
        Button save = new Button("Enregistrer"); save.disableProperty().bind(Bindings.isNull(viewModel.selectedRentProperty())); save.setOnAction(e -> viewModel.saveRent());
        Button del = new Button("Supprimer"); del.disableProperty().bind(Bindings.isNull(viewModel.selectedRentProperty())); del.getStyleClass().add("danger"); del.setOnAction(e -> viewModel.deleteRent());
        actions.getChildren().addAll(save, del);

        HBox links = new HBox(8);
        Button bC = new Button("Voir contact"); bC.setOnAction(e -> { if (viewModel.selectedContactProperty().get() != null) goContact.accept(viewModel.selectedContactProperty().get().getId()); });
        Button bP = new Button("Voir bien"); bP.setOnAction(e -> { if (viewModel.selectedPropertyProperty().get() != null) goProperty.accept(viewModel.selectedPropertyProperty().get().getId()); });
        links.getChildren().addAll(bC, bP);

        VBox rulesBox = buildRulesBox(goTask, goDocument);

        c.getChildren().addAll(t, new Separator(), form, actions, links, new Separator(), rulesBox);
        return c;
    }

    private VBox buildRulesBox(Consumer<UUID> goTask, Consumer<UUID> goDocument) {
        VBox box = new VBox(8);
        Label l = new Label("Tâches automatiques du loyer");

        ComboBox<String> frequency = new ComboBox<>(FXCollections.observableArrayList("Hebdomadaire", "Mensuelle", "Trimestrielle", "Annuelle"));
        frequency.setValue("Mensuelle");
        CheckBox auto = new CheckBox("Renouvelable automatiquement");
        auto.setSelected(true);

        ComboBox<String> dayOfWeek = new ComboBox<>(FXCollections.observableArrayList("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"));
        dayOfWeek.setValue("Lundi");
        ComboBox<Integer> dayOfMonth = new ComboBox<>(FXCollections.observableArrayList());
        for (int i = 1; i <= 31; i++) dayOfMonth.getItems().add(i);
        dayOfMonth.setValue(1);
        ComboBox<Integer> monthInPeriod = new ComboBox<>(FXCollections.observableArrayList(1, 2, 3));
        monthInPeriod.setValue(1);

        frequency.valueProperty().addListener((o, a, b) -> applyFrequencyVisibility(b, dayOfWeek, dayOfMonth, monthInPeriod));
        applyFrequencyVisibility(frequency.getValue(), dayOfWeek, dayOfMonth, monthInPeriod);

        Button addRule = new Button("Ajouter règle");
        addRule.setOnAction(e -> viewModel.addRule(
                toCode(frequency.getValue()),
                auto.isSelected(),
                dayOfWeek.isVisible() ? mapDow(dayOfWeek.getValue()) : null,
                dayOfMonth.isVisible() ? dayOfMonth.getValue() : null,
                monthInPeriod.isVisible() ? monthInPeriod.getValue() : null
        ));

        ListView<RentTaskRule> ruleList = new ListView<>(viewModel.rules());
        ruleList.setCellFactory(lv -> new ListCell<>() { @Override protected void updateItem(RentTaskRule i, boolean e) { super.updateItem(i, e); setText(e || i == null ? null : viewModel.ruleDisplay(i)); }});
        Button delRule = new Button("Supprimer règle sélectionnée");
        delRule.setOnAction(e -> viewModel.removeRule(ruleList.getSelectionModel().getSelectedItem()));

        TableView<TaskItem> taskTable = buildTaskTable(goTask);
        TableView<DocumentItem> docTable = buildDocumentTable(goDocument);

        box.getChildren().addAll(l, new HBox(8, frequency, auto, dayOfWeek, dayOfMonth, monthInPeriod, addRule), ruleList, delRule,
                new Label("Tâches liées"), taskTable,
                new Label("Documents liés"), docTable);
        return box;
    }

    private TableView<TaskItem> buildTaskTable(Consumer<UUID> goTask) {
        TableView<TaskItem> table = new TableView<>(viewModel.tasks());
        TableColumn<TaskItem, String> title = new TableColumn<>("Titre");
        title.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getTitle()));

        TableColumn<TaskItem, TaskItem> action = new TableColumn<>("Action");
        action.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        action.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Voir tâche");
            @Override protected void updateItem(TaskItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                btn.setOnAction(e -> goTask.accept(item.getId()));
                setGraphic(btn);
            }
        });
        table.getColumns().addAll(title, action);
        table.setPrefHeight(140);
        return table;
    }

    private TableView<DocumentItem> buildDocumentTable(Consumer<UUID> goDocument) {
        TableView<DocumentItem> table = new TableView<>(viewModel.documents());
        TableColumn<DocumentItem, String> name = new TableColumn<>("Nom");
        name.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getFileName()));

        TableColumn<DocumentItem, DocumentItem> action = new TableColumn<>("Action");
        action.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        action.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Voir document");
            @Override protected void updateItem(DocumentItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                btn.setOnAction(e -> goDocument.accept(item.getId()));
                setGraphic(btn);
            }
        });
        table.getColumns().addAll(name, action);
        table.setPrefHeight(140);
        return table;
    }

    private void applyFrequencyVisibility(String value, Node dayOfWeek, Node dayOfMonth, Node monthInPeriod) {
        boolean weekly = "Hebdomadaire".equals(value);
        boolean monthly = "Mensuelle".equals(value);
        boolean quarterly = "Trimestrielle".equals(value);
        boolean yearly = "Annuelle".equals(value);
        setVisibleManaged(dayOfWeek, weekly);
        setVisibleManaged(dayOfMonth, monthly || quarterly || yearly);
        setVisibleManaged(monthInPeriod, quarterly || yearly);
    }

    private void setVisibleManaged(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private int mapDow(String value) {
        return switch (value) {
            case "Lundi" -> 1;
            case "Mardi" -> 2;
            case "Mercredi" -> 3;
            case "Jeudi" -> 4;
            case "Vendredi" -> 5;
            case "Samedi" -> 6;
            default -> 7;
        };
    }

    private String toCode(String label) {
        return switch (label) {
            case "Hebdomadaire" -> "WEEKLY";
            case "Mensuelle" -> "MONTHLY";
            case "Trimestrielle" -> "QUARTERLY";
            case "Annuelle" -> "YEARLY";
            default -> "MONTHLY";
        };
    }
}
