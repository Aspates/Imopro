package com.imopro.ui;

import com.imopro.application.PipelineService;
import com.imopro.domain.PipelineCard;
import com.imopro.domain.PipelineStage;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class PipelineView {
    private final PipelineViewModel viewModel;
    private final VBox root;

    public PipelineView(PipelineService service) {
        this.viewModel = new PipelineViewModel(service);
        this.root = new VBox(12);
        root.setPadding(new Insets(16));
        root.getStyleClass().add("content");

        Label title = new Label("Pipeline");
        title.getStyleClass().add("section-title");

        HBox board = new HBox(12);
        for (PipelineStage stage : viewModel.getStages()) {
            board.getChildren().add(buildColumn(stage));
        }

        root.getChildren().addAll(title, board);
        VBox.setVgrow(board, Priority.ALWAYS);
    }

    public Node getRoot() {
        return root;
    }

    private Node buildColumn(PipelineStage stage) {
        VBox col = new VBox(8);
        col.setPadding(new Insets(8));
        col.setPrefWidth(220);
        col.setStyle("-fx-background-color:#f2f4f8; -fx-background-radius:8;");

        Label header = new Label(stage.name());
        header.setStyle("-fx-font-weight:bold;");

        ListView<PipelineCard> list = new ListView<>();
        list.setItems(viewModel.cardsFor(stage.id()));
        list.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(PipelineCard item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                VBox box = new VBox(4);
                Label name = new Label(item.display());
                name.setWrapText(true);
                HBox actions = new HBox(4);
                Button left = new Button("←");
                Button right = new Button("→");
                left.setOnAction(e -> viewModel.moveToPrevious(item));
                right.setOnAction(e -> viewModel.moveToNext(item));
                actions.getChildren().addAll(left, right);
                box.getChildren().addAll(name, actions);
                setGraphic(box);
            }
        });

        col.getChildren().addAll(header, list);
        VBox.setVgrow(list, Priority.ALWAYS);
        return col;
    }
}
