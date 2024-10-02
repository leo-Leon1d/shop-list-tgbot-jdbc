package com.example.demo;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BotService {

    private final BotRepository repository;

    public BotService(BotRepository repository) {
        this.repository = repository;
    }

    public List<String> listName() {
        return repository.listName();
    }

    public List<Map<String, Object>> listNameAmount() {
        return repository.listNameAmount();
    }

    public void remove(String name) {
        repository.remove(name);
    }

    public void removeALl() {
        repository.removeAll();
    }

    public void add(String name, int amount) {
        repository.add(name, amount);
    }
}
