package com.patrakosh.model;

import java.time.Instant;

public class User {

    private final long id;
    private final String username;
    private final String email;
    private final Instant createdAt;

    public User(long id, String username, String email, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
