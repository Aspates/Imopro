package com.imopro.application;

import com.imopro.domain.Rent;
import com.imopro.domain.RentTaskRule;

import java.util.List;
import java.util.UUID;

public interface RentRepository {
    List<Rent> findAll();
    void save(Rent rent);
    void delete(UUID id);
    List<RentTaskRule> findRulesByRent(UUID rentId);
    void saveRule(RentTaskRule rule);
    void deleteRule(UUID ruleId);
    void generateDueTasks();
}
