package com.patrakosh.dao;

import com.patrakosh.model.FileItem;
import com.patrakosh.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileDAO {
    
    public FileItem saveFile(FileItem fileItem) throws SQLException {
        String sql = "INSERT INTO files (user_id, filename, filepath, file_size) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, fileItem.getUserId());
            stmt.setString(2, fileItem.getFilename());
            stmt.setString(3, fileItem.getFilepath());
            stmt.setLong(4, fileItem.getFileSize());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        fileItem.setId(rs.getInt(1));
                    }
                }
            }
            
            return fileItem;
        }
    }
    
    public List<FileItem> getFilesByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM files WHERE user_id = ? ORDER BY upload_time DESC";
        List<FileItem> files = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    files.add(extractFileFromResultSet(rs));
                }
            }
        }
        
        return files;
    }
    
    public FileItem getFileById(int fileId) throws SQLException {
        String sql = "SELECT * FROM files WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, fileId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractFileFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    public boolean deleteFile(int fileId) throws SQLException {
        String sql = "DELETE FROM files WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, fileId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public List<FileItem> searchFiles(int userId, String searchTerm) throws SQLException {
        String sql = "SELECT * FROM files WHERE user_id = ? AND filename LIKE ? ORDER BY upload_time DESC";
        List<FileItem> files = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, "%" + searchTerm + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    files.add(extractFileFromResultSet(rs));
                }
            }
        }
        
        return files;
    }
    
    public long getTotalStorageUsed(int userId) throws SQLException {
        String sql = "SELECT SUM(file_size) as total FROM files WHERE user_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("total");
                }
            }
        }
        
        return 0;
    }
    
    public int getFileCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM files WHERE user_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        
        return 0;
    }
    
    private FileItem extractFileFromResultSet(ResultSet rs) throws SQLException {
        FileItem file = new FileItem();
        file.setId(rs.getInt("id"));
        file.setUserId(rs.getInt("user_id"));
        file.setFilename(rs.getString("filename"));
        file.setFilepath(rs.getString("filepath"));
        file.setFileSize(rs.getLong("file_size"));
        
        Timestamp timestamp = rs.getTimestamp("upload_time");
        if (timestamp != null) {
            file.setUploadTime(timestamp.toLocalDateTime());
        }
        
        return file;
    }
}
