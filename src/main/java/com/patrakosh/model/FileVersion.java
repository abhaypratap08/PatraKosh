package com.patrakosh.model;

import java.time.LocalDateTime;

import com.patrakosh.core.Identifiable;

/**
 * FileVersion entity representing a file version history record.
 * Implements Identifiable for OOP compliance.
 */
public class FileVersion implements Identifiable<Integer> {
    private Integer id;
    private Integer fileId;
    private Integer versionNumber;
    private String filepath;
    private long fileSize;
    private LocalDateTime createdAt;

    public FileVersion() {
    }

    public FileVersion(Integer fileId, Integer versionNumber, String filepath, long fileSize) {
        this.fileId = fileId;
        this.versionNumber = versionNumber;
        this.filepath = filepath;
        this.fileSize = fileSize;
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

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "FileVersion{" +
                "id=" + id +
                ", fileId=" + fileId +
                ", versionNumber=" + versionNumber +
                ", filepath='" + filepath + '\'' +
                ", fileSize=" + fileSize +
                ", createdAt=" + createdAt +
                '}';
    }
}
