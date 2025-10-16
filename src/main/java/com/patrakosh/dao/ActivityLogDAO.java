package com.patrakosh.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.patrakosh.exception.DatabaseException;
import com.patrakosh.model.ActivityLog;
import com.patrakosh.util.DBUtil;

/**
 * Data Access Object for ActivityLog entity.
 * Extends GenericDAO to inherit CRUD operations and adds activity-specific queries.
 */
public class ActivityLogDAO extends GenericDAO<ActivityLog, Integer> {

    @Override
    protected String getTableName() {
        return "activity_logs";
    }

    @Override
    protected ActivityLog mapResultSetToEntity(ResultSet rs) throws SQLException {
        ActivityLog log = new ActivityLog();
        log.setId(rs.getInt("id"));
        log.setUserId(rs.getInt("user_id"));
        log.setAction(rs.getString("action"));
        log.setResourceType(rs.getString("resource_type"));
        
        int resourceId = rs.getInt("resource_id");
        if (!rs.wasNull()) {
            log.setResourceId(resourceId);
        }
        
        log.setDetails(rs.getString("details"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            log.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return log;
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, ActivityLog log) throws SQLException {
        stmt.setInt(1, log.getUserId());
        stmt.setString(2, log.getAction());
        stmt.setString(3, log.getResourceType());
        
        if (log.getResourceId() != null) {
            stmt.setInt(4, log.getResourceId());
        } else {
            stmt.setNull(4, Types.INTEGER);
        }
        
        stmt.setString(5, log.getDetails());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, ActivityLog log) throws SQLException {
        stmt.setInt(1, log.getUserId());
        stmt.setString(2, log.getAction());
        stmt.setString(3, log.getResourceType());
        
        if (log.getResourceId() != null) {
            stmt.setInt(4, log.getResourceId());
        } else {
            stmt.setNull(4, Types.INTEGER);
        }
        
        stmt.setString(5, log.getDetails());
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO activity_logs (user_id, action, resource_type, resource_id, details) VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE activity_logs SET user_id = ?, action = ?, resource_type = ?, resource_id = ?, details = ? WHERE id = ?";
    }

    @Override
    protected int getUpdateParameterCount() {
        return 5;
    }

    // ActivityLog-specific query methods

    /**
     * Gets activity logs for a specific user.
     *
     * @param userId the user ID
     * @param limit maximum number of logs to return
     * @return list of activity logs
     * @throws DatabaseException if the operation fails
     */
    public List<ActivityLog> getLogsByUserId(Integer userId, int limit) throws DatabaseException {
        String sql = "SELECT * FROM activity_logs WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";
        List<ActivityLog> logs = new ArrayList<>();
        Connection conn = null;
        
        try {
            conn = DBUtil.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, userId);
                stmt.setInt(2, limit);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        logs.add(mapResultSetToEntity(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting logs for user: " + userId, e);
        } finally {
            DBUtil.closeConnection(conn);
        }
        
        return logs;
    }

    /**
     * Gets activity logs by action type.
     *
     * @param userId the user ID
     * @param action the action type
     * @return list of activity logs
     * @throws DatabaseException if the operation fails
     */
    public List<ActivityLog> getLogsByAction(Integer userId, String action) throws DatabaseException {
        String sql = "SELECT * FROM activity_logs WHERE user_id = ? AND action = ? ORDER BY created_at DESC";
        List<ActivityLog> logs = new ArrayList<>();
        Connection conn = null;
        
        try {
            conn = DBUtil.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, userId);
                stmt.setString(2, action);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        logs.add(mapResultSetToEntity(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting logs by action for user: " + userId, e);
        } finally {
            DBUtil.closeConnection(conn);
        }
        
        return logs;
    }

    /**
     * Gets activity logs within a date range.
     *
     * @param userId the user ID
     * @param startDate the start date
     * @param endDate the end date
     * @return list of activity logs
     * @throws DatabaseException if the operation fails
     */
    public List<ActivityLog> getLogsByDateRange(Integer userId, LocalDateTime startDate, LocalDateTime endDate) throws DatabaseException {
        String sql = "SELECT * FROM activity_logs WHERE user_id = ? AND created_at BETWEEN ? AND ? ORDER BY created_at DESC";
        List<ActivityLog> logs = new ArrayList<>();
        Connection conn = null;
        
        try {
            conn = DBUtil.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, userId);
                stmt.setTimestamp(2, Timestamp.valueOf(startDate));
                stmt.setTimestamp(3, Timestamp.valueOf(endDate));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        logs.add(mapResultSetToEntity(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting logs by date range for user: " + userId, e);
        } finally {
            DBUtil.closeConnection(conn);
        }
        
        return logs;
    }

    /**
     * Logs an activity (convenience method).
     *
     * @param userId the user ID
     * @param action the action type
     * @param resourceType the resource type
     * @param resourceId the resource ID
     * @return the created log
     * @throws DatabaseException if the operation fails
     */
    public ActivityLog logActivity(Integer userId, String action, String resourceType, Integer resourceId) throws DatabaseException {
        ActivityLog log = new ActivityLog(userId, action, resourceType, resourceId);
        return save(log);
    }

    /**
     * Logs an activity with details (convenience method).
     *
     * @param userId the user ID
     * @param action the action type
     * @param resourceType the resource type
     * @param resourceId the resource ID
     * @param details additional details
     * @return the created log
     * @throws DatabaseException if the operation fails
     */
    public ActivityLog logActivity(Integer userId, String action, String resourceType, Integer resourceId, String details) throws DatabaseException {
        ActivityLog log = new ActivityLog(userId, action, resourceType, resourceId, details);
        return save(log);
    }
}
