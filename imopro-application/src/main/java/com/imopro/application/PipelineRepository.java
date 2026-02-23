package com.imopro.application;

import com.imopro.domain.PipelineCard;
import com.imopro.domain.PipelineStage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PipelineRepository {
    List<PipelineStage> listStages();

    Map<Integer, List<PipelineCard>> listCardsByStage();

    void movePropertyToStage(UUID propertyId, int stageId, String stageName);
}
