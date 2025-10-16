package com.patrakosh.dao;

import com.patrakosh.exception.DatabaseException;
import com.patrakosh.model.FileShare;
import com.patrakosh.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for FileShare entity.
 * Extends GenericDAO to inherit CRUD operations and adds share-specific queries.
 */
public class FileShareDAO extends GenericDAO<FileShare, Integer> {

    @Override
    protected String getTableName() {
        return "file_shares";
    }

    @Override
    protected FileShare mapResultSetToEntity(ResultSet rs) throws SQLException {
        FileShare share = new FileShare();
        share.setId(rs.getInt("id"));
        share.setFileId(rs.getInt("file_id"));
        share.setSharedByUserId(rs.getInt("shared_by_user_id"));
        
        int sharedWithUserId = rs.getInt("shared_with_user_id");
        if (!rs.wasNull()) {
            share.setSharedWithUserId(sharedWithUserId);
        }
        
        share.setShareToken(rs.getString("share_token"));
        share.setPublic(rs.getBoolean("is_public"));
        
        Timestamp expiresAt = rs.getTimestamp("expires_at");
        if (expiresAt != null) {
            share.setExpiresAt(expiresAt.toLocalDateTime());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            share.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            share.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return share;
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, FileShare share) throws SQLException {
        stmt.setInt(1, share.getFileId());
        stmt.setInt(2, share.getSharedByUserId());
        
        if (share.getSharedWithUserId() != null) {
            stmt.setInt(3, share.getSharedWithUserId());
        } else {
            stmt.setNull(3, Types.INTEGER);
        }
        
        stmt.setString(4, share.getShareToken());
        
        if (share.getExpiresAt() != null) {
            stmt.setTimestamp(5, Timestamp.valueOf(share.getExpiresAt()));
        } else {
            stmt.setNull(5, Types.TIMESTAMP);
        }
        
        stmt.setBoolean(6, share.isPublic());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, FileShare share) throws SQLException {
        stmt.setInt(1, share.getFileId());
        stmt.setInt(2, share.getSharedByUserId());
        
        if (share.getSharedWithUserId() != null) {
            stmt.setInt(3, share.getSharedWithUserId());
        } else {
            stmt.setNull(3, Types.INTEGER);
        }
        
        stmt.setString(4, share.getShareToken());
        
        if (share.getExpiresAt() != null) {
            stmt.setTimestamp(5, Timestamp.valueOf(share.getExpiresAt()));
        } else {
            stmt.setNull(5, Types.TIMESTAMP);
        }
        
        stmt.setBoolean(6, share.isPublic());
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO file_shares (file_id, shared_by_user_id, shared_with_user_id, share_token, expires_at, is_public) VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE file_shares SET file_id = ?, shared_by_user_id = ?, shared_with_user_id = ?, share_token = ?, expires_at = ?, is_public = ? WHERE id = ?";
    }

    @Override
    protected int getUpdateParameterCount() {
        return 6;
    }

    // FileShare-specific query methods

    /**
     * Gets all shares for a specific file.
     *
     * @param fileId the file ID
     * @return list of shares
     * @throws DatabaseException if the operation fails
     */
    public List<FileShare> getSharesByFileId(Integer fileId) throws DatabaseException {
        String sql = "SELECT * FROM file_shares WHERE file_id = ?";
        List<FileShare> shares = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, fileId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    shares.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting shares for file: " + fileId, e);
        }
        
        return shares;
    }

    /**
     * Gets all files shared with a specific user.
     *
     * @param userId the user ID
     * @return list of shares
     * @throws DatabaseException if the operation fails
     */
    public List<FileShare> getSharesForUser(Integer userId) throws DatabaseException {
        String sql = "SELECT * FROM file_shares WHERE shared_with_user_id = ?";
        List<FileShare> shares = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    shares.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting shares for user: " + userId, e);
        }
        
        return shares;
    }

    /**
     * Gets all files shared by a specific user.
     *
     * @param userId the user ID
     * @return list of shares
     * @throws DatabaseException if the operation fails
     */
    public List<FileShare> getSharesByUser(Integer userId) throws DatabaseException {
        String sql = "SELECT * FROM file_shares WHERE shared_by_user_id = ?";
        List<FileShare> shares = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    shares.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting shares by user: " + userId, e);
        }
        
        return shares;
    }

    /**
     * Finds a share by its token.
     *
     * @param token the share token
     * @return the share, or null if not found
     * @throws DatabaseException if the operation fails
     */
    public FileShare getShareByToken(String token) throws DatabaseException {
        String sql = "SELECT * FROM file_shares WHERE share_token = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, token);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding share by token", e);
        }
        
        return null;
    }
}
