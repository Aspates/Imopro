package com.imopro.domain;

import java.util.UUID;

public record PipelineCard(UUID propertyId, String title, String city, String status) {
    public String display() {
        String t = (title == null || title.isBlank()) ? "(Sans titre)" : title;
        String c = (city == null || city.isBlank()) ? "" : " - " + city;
        return t + c;
    }
}
