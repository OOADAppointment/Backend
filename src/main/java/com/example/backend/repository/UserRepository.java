package com.example.backend.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.example.backend.model.User;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    // In-memory store for users
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public Optional<User> findById(String id) {
        return Optional.ofNullable(users.get(id));
    }

    public User save(User user) {
        if (user.getId() == null || user.getId().isEmpty()) {
            user.setId(UUID.randomUUID().toString());
        } 
        users.put(user.getId(), user);
        return user;
    }

    public void deleteById(String id) {
        users.remove(id);
    }
    

    public Optional<User> findByUsername(String username) {
        return users.values().stream()
                .filter(user -> username.equals(user.getUsername()))
                .findFirst();
    }

    // Helper method to clear users, useful for testing
    public void deleteAll() {
        users.clear();
    }
}
