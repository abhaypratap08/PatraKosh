package com.patrakosh.model;

import java.time.LocalDateTime;

import com.patrakosh.core.Auditable;
import com.patrakosh.core.Identifiable;

/**
 * User entity representing a system user.
 * Implements Identifiable and Auditable for OOP compliance.
 */
public class User implements Identifiable<Integer>, Auditable {
    private Integer id;
    private String username;
    private String email;
    private String password;
    private long storageQuota;  // in bytes
    private long storageUsed;   // in bytes
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public User() {
        this.storageQuota = 1073741824L; // 1GB default
        this.storageUsed = 0L;
    }
    
    public User(int id, String username, String email) {
        this();
        this.id = id;
        this.username = username;
        this.email = email;
    }
    
    public User(String username, String email, String password) {
        this();
        this.username = username;
        this.email = email;
        this.password = password;
    }
    
    // Getters and Setters
    @Override
    public Integer getId() {
        return id;
    }
    
    @Override
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    @Override
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    @Override
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public long getStorageQuota() {
        return storageQuota;
    }
    
    public void setStorageQuota(long storageQuota) {
        this.storageQuota = storageQuota;
    }
    
    public long getStorageUsed() {
        return storageUsed;
    }
    
    public void setStorageUsed(long storageUsed) {
        this.storageUsed = storageUsed;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", storageUsed=" + storageUsed +
                ", storageQuota=" + storageQuota +
                '}';
    }
}
