package com.example.demo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class BotRepository {

    private final JdbcTemplate jdbcTemplate;

    public BotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> listName() {
        String sql = "SELECT name FROM shopping";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    public List<Map<String, Object>> listNameAmount() {
        String sql = "SELECT name, amount FROM shopping";
        return jdbcTemplate.queryForList(sql);
    }

    public void remove(String name) {
        String sql = "DELETE FROM shopping WHERE name = ?";
        jdbcTemplate.update(sql, name);
    }

    public void removeAll() {
        String sql = "DELETE FROM shopping";
        jdbcTemplate.update(sql);
    }

    public void add(String name, int amount) {
        String sql = "INSERT INTO shopping (name, amount) VALUES (?, ?)";
        jdbcTemplate.update(sql, name, amount);
    }
}