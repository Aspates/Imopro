package com.imopro.ui;

import com.imopro.application.ContactService;
import com.imopro.application.DocumentService;
import com.imopro.application.PropertyService;
import com.imopro.application.RentService;
import com.imopro.application.TaskService;
import com.imopro.domain.Contact;
import com.imopro.domain.Property;
import com.imopro.domain.Rent;
import com.imopro.domain.RentTaskRule;
import javafx.beans.binding.Bindings;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.DayOfWeek;

public class RentView {
    private final RentViewModel viewModel;
    private final BorderPane root;

    public RentView(RentService rentService,
                    ContactService contactService,
                    PropertyService propertyService,
                    TaskService taskService,
                    DocumentService documentService,
                    Runnable goContacts,
                    Runnable goProperties,
                    Runnable goTasks,
                    Runnable goDocuments) {
        this.viewModel = new RentViewModel(rentService, contactService, propertyService, taskService, documentService);
        this.root = new BorderPane();
        root.setPadding(new Insets(16));
        root.getStyleClass().add("content");
        root.setLeft(buildListPane());
        root.setCenter(buildDetailPane(goContacts, goProperties, goTasks, goDocuments));
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
        list.getSelectionModel().selectedItemProperty().addListener((o,a,b)->viewModel.selectedRentProperty().set(b));

        Button add = new Button("Nouveau loyer");
        add.setOnAction(e->viewModel.createRent());
        box.getChildren().addAll(t, list, add);
        VBox.setVgrow(list, Priority.ALWAYS);
        return box;
    }

    private Node buildDetailPane(Runnable goContacts, Runnable goProperties, Runnable goTasks, Runnable goDocuments) {
        VBox c = new VBox(10);
        c.setPadding(new Insets(0,0,0,24));
        Label t = new Label("Fiche loyer");
        t.getStyleClass().add("section-title");

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(10);

        ComboBox<Contact> contact = new ComboBox<>(viewModel.contacts());
        contact.setCellFactory(lv -> new ListCell<>() { @Override protected void updateItem(Contact i, boolean e){ super.updateItem(i,e); setText(e||i==null?null:i.displayName()); }});
        contact.setButtonCell(new ListCell<>() { @Override protected void updateItem(Contact i, boolean e){ super.updateItem(i,e); setText(e||i==null?null:i.displayName()); }});
        contact.valueProperty().bindBidirectional(viewModel.selectedContactProperty());

        ComboBox<Property> property = new ComboBox<>(viewModel.properties());
        property.setCellFactory(lv -> new ListCell<>() { @Override protected void updateItem(Property i, boolean e){ super.updateItem(i,e); setText(e||i==null?null:i.displayTitle()); }});
        property.setButtonCell(new ListCell<>() { @Override protected void updateItem(Property i, boolean e){ super.updateItem(i,e); setText(e||i==null?null:i.displayTitle()); }});
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
        Button save = new Button("Enregistrer"); save.disableProperty().bind(Bindings.isNull(viewModel.selectedRentProperty())); save.setOnAction(e->viewModel.saveRent());
        Button del = new Button("Supprimer"); del.disableProperty().bind(Bindings.isNull(viewModel.selectedRentProperty())); del.getStyleClass().add("danger"); del.setOnAction(e->viewModel.deleteRent());
        actions.getChildren().addAll(save, del);

        HBox links = new HBox(8);
        Button bC = new Button("Voir contact"); bC.setOnAction(e->goContacts.run());
        Button bP = new Button("Voir bien"); bP.setOnAction(e->goProperties.run());
        Button bT = new Button("Voir tâches"); bT.setOnAction(e->goTasks.run());
        Button bD = new Button("Voir documents"); bD.setOnAction(e->goDocuments.run());
        links.getChildren().addAll(bC,bP,bT,bD);

        VBox rulesBox = buildRulesBox();

        c.getChildren().addAll(t, new Separator(), form, actions, links, new Separator(), rulesBox);
        return c;
    }

    private Node buildRulesBox() {
        VBox box = new VBox(8);
        Label l = new Label("Tâches automatiques du loyer");

        ComboBox<String> frequency = new ComboBox<>(FXCollections.observableArrayList("WEEKLY","MONTHLY","QUARTERLY","YEARLY"));
        frequency.setValue("MONTHLY");
        CheckBox auto = new CheckBox("Renouvelable automatiquement");
        auto.setSelected(true);
        ComboBox<String> dayOfWeek = new ComboBox<>(FXCollections.observableArrayList("LUNDI","MARDI","MERCREDI","JEUDI","VENDREDI","SAMEDI","DIMANCHE"));
        dayOfWeek.setValue("LUNDI");
        ComboBox<Integer> dayOfMonth = new ComboBox<>(FXCollections.observableArrayList());
        for (int i=1;i<=31;i++) dayOfMonth.getItems().add(i);
        dayOfMonth.setValue(1);
        ComboBox<Integer> month = new ComboBox<>(FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10,11,12));
        month.setValue(1);

        Button addRule = new Button("Ajouter règle");
        addRule.setOnAction(e -> viewModel.addRule(
                frequency.getValue(),
                auto.isSelected(),
                dayOfWeek.getValue()==null?null: mapDow(dayOfWeek.getValue()),
                dayOfMonth.getValue(),
                month.getValue()
        ));

        ListView<RentTaskRule> ruleList = new ListView<>(viewModel.rules());
        ruleList.setCellFactory(lv -> new ListCell<>() { @Override protected void updateItem(RentTaskRule i, boolean e){ super.updateItem(i,e); setText(e||i==null?null:i.frequency()+" auto="+i.autoRenew()+" dW="+i.dayOfWeek()+" dM="+i.dayOfMonth()+" m="+i.monthOfYear()); }});
        Button delRule = new Button("Supprimer règle sélectionnée");
        delRule.setOnAction(e -> viewModel.removeRule(ruleList.getSelectionModel().getSelectedItem()));

        TableView<Object> linked = new TableView<>();
        linked.setPlaceholder(new Label("Tâches/Documents liés visibles depuis les modules dédiés."));

        box.getChildren().addAll(l, new HBox(8, frequency, auto, dayOfWeek, dayOfMonth, month, addRule), ruleList, delRule, linked);
        return box;
    }

    private int mapDow(String value) {
        return switch (value) {
            case "LUNDI" -> DayOfWeek.MONDAY.getValue();
            case "MARDI" -> DayOfWeek.TUESDAY.getValue();
            case "MERCREDI" -> DayOfWeek.WEDNESDAY.getValue();
            case "JEUDI" -> DayOfWeek.THURSDAY.getValue();
            case "VENDREDI" -> DayOfWeek.FRIDAY.getValue();
            case "SAMEDI" -> DayOfWeek.SATURDAY.getValue();
            default -> DayOfWeek.SUNDAY.getValue();
        };
    }
}
