package com.example.backend.dto;

public class UserIdRequest {
    private String userId;

    // Default constructor for JSON deserialization
    public UserIdRequest() {
    }

    public UserIdRequest(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}