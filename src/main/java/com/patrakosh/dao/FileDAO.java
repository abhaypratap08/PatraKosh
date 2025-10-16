package com.patrakosh.dao;

import com.patrakosh.exception.DatabaseException;
import com.patrakosh.model.FileItem;
import com.patrakosh.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for FileItem entity.
 * Extends GenericDAO to inherit CRUD operations and adds file-specific queries.
 */
public class FileDAO extends GenericDAO<FileItem, Integer> {

    @Override
    protected String getTableName() {
        return "files";
    }

    @Override
    protected FileItem mapResultSetToEntity(ResultSet rs) throws SQLException {
        FileItem file = new FileItem();
        file.setId(rs.getInt("id"));
        file.setUserId(rs.getInt("user_id"));
        file.setFilename(rs.getString("filename"));
        file.setFilepath(rs.getString("filepath"));
        file.setFileSize(rs.getLong("file_size"));
        file.setFileHash(rs.getString("file_hash"));
        file.setMimeType(rs.getString("mime_type"));
        file.setVersion(rs.getInt("version"));
        
        Timestamp uploadTime = rs.getTimestamp("upload_time");
        if (uploadTime != null) {
            file.setUploadTime(uploadTime.toLocalDateTime());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            file.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            file.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return file;
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, FileItem file) throws SQLException {
        stmt.setInt(1, file.getUserId());
        stmt.setString(2, file.getFilename());
        stmt.setString(3, file.getFilepath());
        stmt.setLong(4, file.getFileSize());
        stmt.setString(5, file.getFileHash());
        stmt.setString(6, file.getMimeType());
        stmt.setInt(7, file.getVersion());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, FileItem file) throws SQLException {
        stmt.setInt(1, file.getUserId());
        stmt.setString(2, file.getFilename());
        stmt.setString(3, file.getFilepath());
        stmt.setLong(4, file.getFileSize());
        stmt.setString(5, file.getFileHash());
        stmt.setString(6, file.getMimeType());
        stmt.setInt(7, file.getVersion());
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO files (user_id, filename, filepath, file_size, file_hash, mime_type, version) VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE files SET user_id = ?, filename = ?, filepath = ?, file_size = ?, file_hash = ?, mime_type = ?, version = ? WHERE id = ?";
    }

    @Override
    protected int getUpdateParameterCount() {
        return 7;
    }

    // File-specific query methods

    /**
     * Gets all files for a specific user.
     *
     * @param userId the user ID
     * @return list of files
     * @throws DatabaseException if the operation fails
     */
    public List<FileItem> getFilesByUserId(Integer userId) throws DatabaseException {
        String sql = "SELECT * FROM files WHERE user_id = ? ORDER BY upload_time DESC";
        List<FileItem> files = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    files.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting files for user: " + userId, e);
        }
        
        return files;
    }

    /**
     * Searches files by filename for a specific user.
     *
     * @param userId the user ID
     * @param searchTerm the search term
     * @return list of matching files
     * @throws DatabaseException if the operation fails
     */
    public List<FileItem> searchFiles(Integer userId, String searchTerm) throws DatabaseException {
        String sql = "SELECT * FROM files WHERE user_id = ? AND filename LIKE ? ORDER BY upload_time DESC";
        List<FileItem> files = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, "%" + searchTerm + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    files.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error searching files for user: " + userId, e);
        }
        
        return files;
    }

    /**
     * Gets total storage used by a user.
     *
     * @param userId the user ID
     * @return total storage in bytes
     * @throws DatabaseException if the operation fails
     */
    public long getTotalStorageUsed(Integer userId) throws DatabaseException {
        String sql = "SELECT SUM(file_size) as total FROM files WHERE user_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("total");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error calculating storage for user: " + userId, e);
        }
        
        return 0;
    }

    /**
     * Gets file count for a user.
     *
     * @param userId the user ID
     * @return number of files
     * @throws DatabaseException if the operation fails
     */
    public int getFileCount(Integer userId) throws DatabaseException {
        String sql = "SELECT COUNT(*) as count FROM files WHERE user_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting files for user: " + userId, e);
        }
        
        return 0;
    }

    /**
     * Finds a file by its hash.
     *
     * @param fileHash the file hash
     * @return the file, or null if not found
     * @throws DatabaseException if the operation fails
     */
    public FileItem getFileByHash(String fileHash) throws DatabaseException {
        String sql = "SELECT * FROM files WHERE file_hash = ? LIMIT 1";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, fileHash);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding file by hash", e);
        }
        
        return null;
    }

    /**
     * Gets files by MIME type for a user.
     *
     * @param userId the user ID
     * @param mimeType the MIME type
     * @return list of matching files
     * @throws DatabaseException if the operation fails
     */
    public List<FileItem> getFilesByMimeType(Integer userId, String mimeType) throws DatabaseException {
        String sql = "SELECT * FROM files WHERE user_id = ? AND mime_type = ? ORDER BY upload_time DESC";
        List<FileItem> files = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, mimeType);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    files.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting files by MIME type for user: " + userId, e);
        }
        
        return files;
    }
}
