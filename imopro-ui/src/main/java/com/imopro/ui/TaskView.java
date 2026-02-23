package com.imopro.ui;

import com.imopro.application.TaskService;
import com.imopro.domain.TaskItem;
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
import javafx.scene.layout.VBox;

public class TaskView {
    private final TaskViewModel viewModel;
    private final BorderPane root;

    public TaskView(TaskService taskService) {
        this.viewModel = new TaskViewModel(taskService);
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

        Label title = new Label("Tâches");
        title.getStyleClass().add("section-title");

        HBox quickFilters = new HBox(6);
        Button allBtn = new Button("Toutes");
        Button todayBtn = new Button("Aujourd'hui");
        Button overdueBtn = new Button("En retard");
        Button weekBtn = new Button("Cette semaine");
        allBtn.setOnAction(e -> viewModel.setFilterAll());
        todayBtn.setOnAction(e -> viewModel.setFilterToday());
        overdueBtn.setOnAction(e -> viewModel.setFilterOverdue());
        weekBtn.setOnAction(e -> viewModel.setFilterWeek());
        quickFilters.getChildren().addAll(allBtn, todayBtn, overdueBtn, weekBtn);

        TextField search = new TextField();
        search.setPromptText("Rechercher titre/description/statut");
        search.textProperty().bindBidirectional(viewModel.searchQueryProperty());

        ListView<TaskItem> listView = new ListView<>();
        listView.setItems(viewModel.getTasks());
        listView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(TaskItem item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : viewModel.display(item));
            }
        });
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                viewModel.selectedTaskProperty().set(newVal));

        Button newBtn = new Button("Nouvelle tâche");
        newBtn.setOnAction(e -> viewModel.createTask());

        container.getChildren().addAll(title, quickFilters, search, listView, newBtn);
        VBox.setVgrow(listView, Priority.ALWAYS);
        return container;
    }

    private Node buildDetailPane() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(0, 0, 0, 24));

        Label title = new Label("Fiche tâche");
        title.getStyleClass().add("section-title");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);

        TextField tfTitle = new TextField();
        tfTitle.textProperty().bindBidirectional(viewModel.titleProperty());
        TextArea tfDescription = new TextArea();
        tfDescription.setPrefRowCount(5);
        tfDescription.textProperty().bindBidirectional(viewModel.descriptionProperty());
        TextField tfDueDate = new TextField();
        tfDueDate.setPromptText("YYYY-MM-DD");
        tfDueDate.textProperty().bindBidirectional(viewModel.dueDateProperty());
        TextField tfStatus = new TextField();
        tfStatus.setPromptText("TODO ou DONE");
        tfStatus.textProperty().bindBidirectional(viewModel.statusProperty());

        form.addRow(0, new Label("Titre"), tfTitle);
        form.addRow(1, new Label("Description"), tfDescription);
        form.addRow(2, new Label("Échéance"), tfDueDate);
        form.addRow(3, new Label("Statut"), tfStatus);

        HBox actions = new HBox(10);
        Button saveBtn = new Button("Enregistrer");
        saveBtn.disableProperty().bind(viewModel.canActBinding().not());
        saveBtn.setOnAction(e -> viewModel.saveSelectedTask());

        Button doneBtn = new Button("Marquer fait");
        doneBtn.disableProperty().bind(viewModel.canActBinding().not());
        doneBtn.setOnAction(e -> viewModel.markDone());

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().add("danger");
        deleteBtn.disableProperty().bind(viewModel.canActBinding().not());
        deleteBtn.setOnAction(e -> viewModel.deleteSelectedTask());

        actions.getChildren().addAll(saveBtn, doneBtn, deleteBtn);

        container.getChildren().addAll(title, new Separator(), form, actions);
        return container;
    }
}
