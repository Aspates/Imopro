package com.imopro.domain;

import java.time.LocalDate;
import java.util.UUID;

public record RentTaskRule(
        UUID id,
        UUID rentId,
        String frequency,
        boolean autoRenew,
        Integer dayOfWeek,
        Integer dayOfMonth,
        Integer monthOfYear,
        String titlePrefix,
        String descriptionPrefix,
        LocalDate lastGeneratedAt,
        boolean active
) {
}
