package com.imopro.ui;

import com.imopro.application.PipelineService;
import com.imopro.domain.PipelineCard;
import com.imopro.domain.PipelineStage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PipelineViewModel {
    private final PipelineService service;
    private final ObservableList<PipelineStage> stages = FXCollections.observableArrayList();
    private final Map<Integer, ObservableList<PipelineCard>> cardsByStage = new HashMap<>();

    public PipelineViewModel(PipelineService service) {
        this.service = service;
        load();
    }

    public ObservableList<PipelineStage> getStages() {
        return stages;
    }

    public ObservableList<PipelineCard> cardsFor(int stageId) {
        return cardsByStage.computeIfAbsent(stageId, id -> FXCollections.observableArrayList());
    }

    public void moveToNext(PipelineCard card) {
        List<PipelineStage> ordered = stages.stream().sorted((a, b) -> Integer.compare(a.position(), b.position())).toList();
        int currentIndex = -1;
        for (int i = 0; i < ordered.size(); i++) {
            if (ordered.get(i).name().equals(card.status())) {
                currentIndex = i;
                break;
            }
        }
        if (currentIndex >= 0 && currentIndex < ordered.size() - 1) {
            PipelineStage next = ordered.get(currentIndex + 1);
            service.moveProperty(card.propertyId(), next.id(), next.name());
            load();
        }
    }

    public void moveToPrevious(PipelineCard card) {
        List<PipelineStage> ordered = stages.stream().sorted((a, b) -> Integer.compare(a.position(), b.position())).toList();
        int currentIndex = -1;
        for (int i = 0; i < ordered.size(); i++) {
            if (ordered.get(i).name().equals(card.status())) {
                currentIndex = i;
                break;
            }
        }
        if (currentIndex > 0) {
            PipelineStage prev = ordered.get(currentIndex - 1);
            service.moveProperty(card.propertyId(), prev.id(), prev.name());
            load();
        }
    }

    public void load() {
        List<PipelineStage> loadedStages = service.listStages();
        stages.setAll(loadedStages);

        Map<Integer, List<PipelineCard>> loadedCards = service.listCardsByStage();
        for (PipelineStage stage : loadedStages) {
            cardsFor(stage.id()).setAll(loadedCards.getOrDefault(stage.id(), List.of()));
        }
    }
}
