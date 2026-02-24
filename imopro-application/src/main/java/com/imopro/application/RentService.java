package com.imopro.application;

import com.imopro.domain.Rent;
import com.imopro.domain.RentTaskRule;

import java.util.List;
import java.util.UUID;

public class RentService {
    private final RentRepository repository;

    public RentService(RentRepository repository) {
        this.repository = repository;
    }

    public List<Rent> listRents() { return repository.findAll(); }
    public void save(Rent rent) { repository.save(rent); }
    public void delete(UUID id) { repository.delete(id); }
    public List<RentTaskRule> listRules(UUID rentId) { return repository.findRulesByRent(rentId); }
    public void saveRule(RentTaskRule rule) { repository.saveRule(rule); }
    public void deleteRule(UUID id) { repository.deleteRule(id); }
    public void generateDueTasks() { repository.generateDueTasks(); }
}
