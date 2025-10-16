package com.patrakosh.model;

import java.time.LocalDateTime;

import com.patrakosh.core.Identifiable;

/**
 * ActivityLog entity representing a user activity record.
 * Implements Identifiable for OOP compliance.
 */
public class ActivityLog implements Identifiable<Integer> {
    private Integer id;
    private Integer userId;
    private String action;  // UPLOAD, DOWNLOAD, DELETE, SHARE, LOGIN, LOGOUT
    private String resourceType;  // FILE, USER, SHARE
    private Integer resourceId;
    private String details;
    private LocalDateTime createdAt;

    public ActivityLog() {
    }

    public ActivityLog(Integer userId, String action, String resourceType, Integer resourceId) {
        this.userId = userId;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public ActivityLog(Integer userId, String action, String resourceType, Integer resourceId, String details) {
        this(userId, action, resourceType, resourceId);
        this.details = details;
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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ActivityLog{" +
                "id=" + id +
                ", userId=" + userId +
                ", action='" + action + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", resourceId=" + resourceId +
                ", createdAt=" + createdAt +
                '}';
    }
}
