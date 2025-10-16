package com.patrakosh.model;

import java.time.LocalDateTime;

import com.patrakosh.core.Auditable;
import com.patrakosh.core.Identifiable;

/**
 * FileShare entity representing a file sharing record.
 * Implements Identifiable and Auditable for OOP compliance.
 */
public class FileShare implements Identifiable<Integer>, Auditable {
    private Integer id;
    private Integer fileId;
    private Integer sharedByUserId;
    private Integer sharedWithUserId;  // null for public shares
    private String shareToken;  // unique token for public links
    private LocalDateTime expiresAt;
    private boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FileShare() {
        this.isPublic = false;
    }

    public FileShare(Integer fileId, Integer sharedByUserId, Integer sharedWithUserId) {
        this();
        this.fileId = fileId;
        this.sharedByUserId = sharedByUserId;
        this.sharedWithUserId = sharedWithUserId;
    }

    public FileShare(Integer fileId, Integer sharedByUserId, String shareToken, boolean isPublic) {
        this();
        this.fileId = fileId;
        this.sharedByUserId = sharedByUserId;
        this.shareToken = shareToken;
        this.isPublic = isPublic;
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

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    public Integer getSharedByUserId() {
        return sharedByUserId;
    }

    public void setSharedByUserId(Integer sharedByUserId) {
        this.sharedByUserId = sharedByUserId;
    }

    public Integer getSharedWithUserId() {
        return sharedWithUserId;
    }

    public void setSharedWithUserId(Integer sharedWithUserId) {
        this.sharedWithUserId = sharedWithUserId;
    }

    public String getShareToken() {
        return shareToken;
    }

    public void setShareToken(String shareToken) {
        this.shareToken = shareToken;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
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

    @Override
    public String toString() {
        return "FileShare{" +
                "id=" + id +
                ", fileId=" + fileId +
                ", sharedByUserId=" + sharedByUserId +
                ", sharedWithUserId=" + sharedWithUserId +
                ", shareToken='" + shareToken + '\'' +
                ", isPublic=" + isPublic +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
