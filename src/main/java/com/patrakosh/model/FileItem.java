package com.patrakosh.model;

import java.time.LocalDateTime;

import com.patrakosh.core.Auditable;
import com.patrakosh.core.Identifiable;

/**
 * FileItem entity representing a stored file.
 * Implements Identifiable and Auditable for OOP compliance.
 */
public class FileItem implements Identifiable<Integer>, Auditable {
    private Integer id;
    private Integer userId;
    private String filename;
    private String filepath;
    private long fileSize;
    private String fileHash;  // SHA-256 hash for duplicate detection
    private String mimeType;
    private Integer version;
    private LocalDateTime uploadTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public FileItem() {
        this.version = 1;
    }
    
    public FileItem(int userId, String filename, String filepath, long fileSize) {
        this();
        this.userId = userId;
        this.filename = filename;
        this.filepath = filepath;
        this.fileSize = fileSize;
    }
    
    public FileItem(int id, int userId, String filename, String filepath, long fileSize, LocalDateTime uploadTime) {
        this();
        this.id = id;
        this.userId = userId;
        this.filename = filename;
        this.filepath = filepath;
        this.fileSize = fileSize;
        this.uploadTime = uploadTime;
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
    
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
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
    
    public LocalDateTime getUploadTime() {
        return uploadTime;
    }
    
    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }
    
    public String getFileHash() {
        return fileHash;
    }
    
    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public Integer getVersion() {
        return version;
    }
    
    public void setVersion(Integer version) {
        this.version = version;
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
        return "FileItem{" +
                "id=" + id +
                ", userId=" + userId +
                ", filename='" + filename + '\'' +
                ", fileSize=" + fileSize +
                ", mimeType='" + mimeType + '\'' +
                ", version=" + version +
                ", uploadTime=" + uploadTime +
                '}';
    }
}
