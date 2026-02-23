package com.imopro.application;

import com.imopro.domain.PipelineCard;
import com.imopro.domain.PipelineStage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PipelineService {
    private final PipelineRepository repository;

    public PipelineService(PipelineRepository repository) {
        this.repository = repository;
    }

    public List<PipelineStage> listStages() {
        return repository.listStages();
    }

    public Map<Integer, List<PipelineCard>> listCardsByStage() {
        return repository.listCardsByStage();
    }

    public void moveProperty(UUID propertyId, int stageId, String stageName) {
        repository.movePropertyToStage(propertyId, stageId, stageName);
    }
}
