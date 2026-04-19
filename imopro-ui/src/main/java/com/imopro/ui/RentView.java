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
import com.imopro.domain.TaskItem;
import com.imopro.infra.LocalDocumentStorage;
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
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
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
                    LocalDocumentStorage storage,
                    Consumer<UUID> goContact,
                    Consumer<UUID> goProperty,
                    Consumer<UUID> goTask) {
        this.viewModel = new RentViewModel(rentService, contactService, propertyService, taskService, documentService, storage);
        this.root = new BorderPane();
        root.setPadding(new Insets(16));
        root.getStyleClass().add("content");
        root.setLeft(buildListPane());
        root.setCenter(buildDetailPane(goContact, goProperty, goTask));
    }

    public Node getRoot() { return root; }
    public void refresh() { viewModel.refreshAll(); }
    public void openRent(UUID id) {
        refresh();
        viewModel.selectById(id);
    }

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

    private Node buildDetailPane(Consumer<UUID> goContact, Consumer<UUID> goProperty, Consumer<UUID> goTask) {
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
        Button goContactFromField = new Button("Voir contact");
        goContactFromField.disableProperty().bind(Bindings.isNull(viewModel.selectedContactProperty()));
        goContactFromField.setOnAction(e -> {
            if (viewModel.selectedContactProperty().get() != null) {
                goContact.accept(viewModel.selectedContactProperty().get().getId());
            }
        });
        HBox contactField = new HBox(8, contact, goContactFromField);
        HBox.setHgrow(contact, Priority.ALWAYS);

        ComboBox<Property> property = new ComboBox<>(viewModel.properties());
        property.setCellFactory(lv -> new ListCell<>() { @Override protected void updateItem(Property i, boolean e) { super.updateItem(i, e); setText(e || i == null ? null : i.displayTitle()); }});
        property.setButtonCell(new ListCell<>() { @Override protected void updateItem(Property i, boolean e) { super.updateItem(i, e); setText(e || i == null ? null : i.displayTitle()); }});
        property.valueProperty().bindBidirectional(viewModel.selectedPropertyProperty());

        TextField amount = new TextField(); amount.textProperty().bindBidirectional(viewModel.amountProperty());
        DatePicker start = new DatePicker(); start.valueProperty().bindBidirectional(viewModel.startDateProperty());
        DatePicker end = new DatePicker(); end.valueProperty().bindBidirectional(viewModel.endDateProperty());
        TextField notes = new TextField(); notes.textProperty().bindBidirectional(viewModel.notesProperty());

        form.addRow(0, new Label("Locataire"), contactField);
        form.addRow(1, new Label("Bien"), property);
        form.addRow(2, new Label("Montant"), amount);
        form.addRow(3, new Label("D\u00e9but"), start);
        form.addRow(4, new Label("Fin"), end);
        form.addRow(5, new Label("Notes"), notes);

        HBox actions = new HBox(10);
        Button save = new Button("Enregistrer"); save.disableProperty().bind(Bindings.isNull(viewModel.selectedRentProperty())); save.setOnAction(e -> viewModel.saveRent());
        Button del = new Button("Supprimer"); del.disableProperty().bind(Bindings.isNull(viewModel.selectedRentProperty())); del.getStyleClass().add("danger"); del.setOnAction(e -> viewModel.deleteRent());
        actions.getChildren().addAll(save, del);

        HBox links = new HBox(8);
        Button bP = new Button("Voir bien"); bP.setOnAction(e -> { if (viewModel.selectedPropertyProperty().get() != null) goProperty.accept(viewModel.selectedPropertyProperty().get().getId()); });
        links.getChildren().addAll(bP);

        VBox rulesBox = buildRulesBox(goTask);

        c.getChildren().addAll(t, new Separator(), form, actions, links, new Separator(), rulesBox);
        return c;
    }

    private VBox buildRulesBox(Consumer<UUID> goTask) {
        VBox box = new VBox(8);
        Label l = new Label("T\u00e2ches automatiques du loyer");

        ComboBox<String> frequency = new ComboBox<>(FXCollections.observableArrayList("Hebdomadaire", "Mensuelle", "Trimestrielle", "Annuelle"));
        frequency.setValue("Mensuelle");
        CheckBox auto = new CheckBox("Renouvelable automatiquement");
        auto.setSelected(true);

        ComboBox<String> dayOfWeek = new ComboBox<>(FXCollections.observableArrayList("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"));
        dayOfWeek.setValue("Lundi");
        ComboBox<Integer> dayOfMonth = new ComboBox<>(FXCollections.observableArrayList());
        for (int i = 1; i <= 31; i++) dayOfMonth.getItems().add(i);
        dayOfMonth.setValue(1);
        ComboBox<Integer> monthOfYear = new ComboBox<>(FXCollections.observableArrayList());
        for (int i = 1; i <= 12; i++) monthOfYear.getItems().add(i);
        monthOfYear.setValue(1);
        ComboBox<Integer> monthInPeriod = new ComboBox<>(FXCollections.observableArrayList(1, 2, 3));
        monthInPeriod.setValue(1);

        frequency.valueProperty().addListener((o, a, b) -> applyFrequencyVisibility(b, dayOfWeek, dayOfMonth, monthInPeriod, monthOfYear));
        applyFrequencyVisibility(frequency.getValue(), dayOfWeek, dayOfMonth, monthInPeriod, monthOfYear);

        Button addRule = new Button("Ajouter r\u00e8gle");
        addRule.setOnAction(e -> viewModel.addRule(
                toCode(frequency.getValue()),
                auto.isSelected(),
                dayOfWeek.isVisible() ? mapDow(dayOfWeek.getValue()) : null,
                dayOfMonth.isVisible() ? dayOfMonth.getValue() : null,
                monthInPeriod.isVisible() ? monthInPeriod.getValue() : (monthOfYear.isVisible() ? monthOfYear.getValue() : null)
        ));

        TableView<TaskItem> taskTable = buildTaskTable(goTask);
        TableView<DocumentItem> docTable = buildDocumentTable();
        Button addDocBtn = new Button("Ajouter document");
        addDocBtn.disableProperty().bind(Bindings.isNull(viewModel.selectedRentProperty()));
        addDocBtn.setOnAction(e -> {
            Window window = root.getScene() == null ? null : root.getScene().getWindow();
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choisir un document");
            File file = chooser.showOpenDialog(window);
            if (file != null) {
                viewModel.importDocumentForSelectedRent(file.toPath());
            }
        });

        box.getChildren().addAll(l, new HBox(8, frequency, auto, dayOfWeek, dayOfMonth, monthInPeriod, monthOfYear, addRule),
                new Label("T\u00e2ches li\u00e9es"), taskTable,
                new HBox(8, new Label("Documents li\u00e9s"), addDocBtn), docTable);
        return box;
    }

    private TableView<TaskItem> buildTaskTable(Consumer<UUID> goTask) {
        TableView<TaskItem> table = new TableView<>(viewModel.tasks());
        TableColumn<TaskItem, String> title = new TableColumn<>("Titre");
        title.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getTitle()));
        title.setCellFactory(col -> coloredTextCell());

        TableColumn<TaskItem, String> type = new TableColumn<>("Type");
        type.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(viewModel.taskType(c.getValue())));
        type.setCellFactory(col -> coloredTextCell());

        TableColumn<TaskItem, String> dueDate = new TableColumn<>("Date d'échéance");
        dueDate.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(viewModel.taskDueDateDisplay(c.getValue())));
        dueDate.setCellFactory(col -> coloredTextCell());

        TableColumn<TaskItem, String> renewable = new TableColumn<>("Renouvelable");
        renewable.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(viewModel.taskRenewableIcon(c.getValue())));
        renewable.setCellFactory(col -> coloredTextCell());

        TableColumn<TaskItem, TaskItem> action = new TableColumn<>("Action");
        action.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        action.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Voir t\u00e2che");
            @Override protected void updateItem(TaskItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                btn.setOnAction(e -> goTask.accept(item.getId()));
                setGraphic(btn);
            }
        });
        table.getColumns().addAll(title, type, dueDate, renewable, action);
        table.setPrefHeight(140);
        return table;
    }

    private TableCell<TaskItem, String> coloredTextCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                TaskItem rowItem = getTableRow() == null ? null : getTableRow().getItem();
                if (rowItem == null) {
                    setStyle("");
                    return;
                }
                if (viewModel.isTaskOverdueTodo(rowItem)) {
                    setStyle("-fx-text-fill: #c62828;");
                } else if (viewModel.isTaskDoneAndNonRenewable(rowItem)) {
                    setStyle("-fx-text-fill: #2e7d32;");
                } else {
                    setStyle("");
                }
            }
        };
    }

    private TableView<DocumentItem> buildDocumentTable() {
        TableView<DocumentItem> table = new TableView<>(viewModel.documents());
        TableColumn<DocumentItem, String> name = new TableColumn<>("Nom");
        name.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().displayName()));

        TableColumn<DocumentItem, String> createdAt = new TableColumn<>("Date d'ajout");
        createdAt.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(viewModel.documentAddedDate(c.getValue())));

        TableColumn<DocumentItem, DocumentItem> open = new TableColumn<>("Ouvrir document");
        open.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        open.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Ouvrir document");
            @Override protected void updateItem(DocumentItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                btn.setOnAction(e -> viewModel.openLinkedDocument(item));
                setGraphic(btn);
            }
        });
        table.getColumns().addAll(name, createdAt, open);
        table.setPrefHeight(180);
        return table;
    }

    private void applyFrequencyVisibility(String value, Node dayOfWeek, Node dayOfMonth, Node monthInPeriod, Node monthOfYear) {
        boolean weekly = "Hebdomadaire".equals(value);
        boolean monthly = "Mensuelle".equals(value);
        boolean quarterly = "Trimestrielle".equals(value);
        boolean yearly = "Annuelle".equals(value);
        setVisibleManaged(dayOfWeek, weekly);
        setVisibleManaged(dayOfMonth, monthly || quarterly || yearly);
        setVisibleManaged(monthInPeriod, quarterly);
        setVisibleManaged(monthOfYear, yearly);
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
