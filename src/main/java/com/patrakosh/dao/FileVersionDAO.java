package com.patrakosh.dao;

import com.patrakosh.exception.DatabaseException;
import com.patrakosh.model.FileVersion;
import com.patrakosh.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for FileVersion entity.
 */
public class FileVersionDAO extends GenericDAO<FileVersion, Integer> {

    @Override
    protected String getTableName() {
        return "file_versions";
    }

    @Override
    protected FileVersion mapResultSetToEntity(ResultSet rs) throws SQLException {
        FileVersion version = new FileVersion();
        version.setId(rs.getInt("id"));
        version.setFileId(rs.getInt("file_id"));
        version.setVersionNumber(rs.getInt("version_number"));
        version.setFilepath(rs.getString("filepath"));
        version.setFileSize(rs.getLong("file_size"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            version.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return version;
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, FileVersion version) throws SQLException {
        stmt.setInt(1, version.getFileId());
        stmt.setInt(2, version.getVersionNumber());
        stmt.setString(3, version.getFilepath());
        stmt.setLong(4, version.getFileSize());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, FileVersion version) throws SQLException {
        stmt.setInt(1, version.getFileId());
        stmt.setInt(2, version.getVersionNumber());
        stmt.setString(3, version.getFilepath());
        stmt.setLong(4, version.getFileSize());
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO file_versions (file_id, version_number, filepath, file_size) VALUES (?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE file_versions SET file_id = ?, version_number = ?, filepath = ?, file_size = ? WHERE id = ?";
    }

    @Override
    protected int getUpdateParameterCount() {
        return 4;
    }

    public List<FileVersion> getVersionsByFileId(Integer fileId) throws DatabaseException {
        String sql = "SELECT * FROM file_versions WHERE file_id = ? ORDER BY version_number DESC";
        List<FileVersion> versions = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, fileId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    versions.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting versions for file: " + fileId, e);
        }
        
        return versions;
    }

    public FileVersion getVersion(Integer fileId, Integer versionNumber) throws DatabaseException {
        String sql = "SELECT * FROM file_versions WHERE file_id = ? AND version_number = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, fileId);
            stmt.setInt(2, versionNumber);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting version for file: " + fileId, e);
        }
        
        return null;
    }

    public int getLatestVersionNumber(Integer fileId) throws DatabaseException {
        String sql = "SELECT MAX(version_number) as max_version FROM file_versions WHERE file_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, fileId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("max_version");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting latest version for file: " + fileId, e);
        }
        
        return 0;
    }
}
