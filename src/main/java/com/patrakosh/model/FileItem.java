package com.patrakosh.model;

import java.time.LocalDateTime;

public class FileItem {
    private int id;
    private int userId;
    private String filename;
    private String filepath;
    private long fileSize;
    private LocalDateTime uploadTime;
    
    public FileItem() {
    }
    
    public FileItem(int userId, String filename, String filepath, long fileSize) {
        this.userId = userId;
        this.filename = filename;
        this.filepath = filepath;
        this.fileSize = fileSize;
    }
    
    public FileItem(int id, int userId, String filename, String filepath, long fileSize, LocalDateTime uploadTime) {
        this.id = id;
        this.userId = userId;
        this.filename = filename;
        this.filepath = filepath;
        this.fileSize = fileSize;
        this.uploadTime = uploadTime;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
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
    
    @Override
    public String toString() {
        return "FileItem{" +
                "id=" + id +
                ", userId=" + userId +
                ", filename='" + filename + '\'' +
                ", fileSize=" + fileSize +
                ", uploadTime=" + uploadTime +
                '}';
    }
}
