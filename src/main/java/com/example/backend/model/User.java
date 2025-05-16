package com.example.backend.model;

import java.util.UUID;

public class User {
    private String id;
    private String username; // Added for findByUsername
    private String password; // For authentication (should be hashed)
    // other fields like email, name, etc.

    public User() {}

    public User(String username, String password) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.password = password; // In a real app, hash this
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
