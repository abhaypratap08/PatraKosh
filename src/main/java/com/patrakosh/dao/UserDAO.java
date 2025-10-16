package com.patrakosh.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.patrakosh.exception.DatabaseException;
import com.patrakosh.model.User;
import com.patrakosh.util.DBUtil;

/**
 * Data Access Object for User entity.
 * Extends GenericDAO to inherit CRUD operations and adds user-specific queries.
 */
public class UserDAO extends GenericDAO<User, Integer> {

    @Override
    protected String getTableName() {
        return "users";
    }

    @Override
    protected User mapResultSetToEntity(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setStorageQuota(rs.getLong("storage_quota"));
        user.setStorageUsed(rs.getLong("storage_used"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return user;
    }

    @Override
    protected void setInsertParameters(PreparedStatement stmt, User user) throws SQLException {
        stmt.setString(1, user.getUsername());
        stmt.setString(2, user.getEmail());
        stmt.setString(3, user.getPassword());
        stmt.setLong(4, user.getStorageQuota());
        stmt.setLong(5, user.getStorageUsed());
    }

    @Override
    protected void setUpdateParameters(PreparedStatement stmt, User user) throws SQLException {
        stmt.setString(1, user.getUsername());
        stmt.setString(2, user.getEmail());
        stmt.setString(3, user.getPassword());
        stmt.setLong(4, user.getStorageQuota());
        stmt.setLong(5, user.getStorageUsed());
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO users (username, email, password, storage_quota, storage_used) VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE users SET username = ?, email = ?, password = ?, storage_quota = ?, storage_used = ? WHERE id = ?";
    }

    @Override
    protected int getUpdateParameterCount() {
        return 5;
    }

    // User-specific query methods

    /**
     * Finds a user by username.
     *
     * @param username the username to search for
     * @return the user, or null if not found
     * @throws DatabaseException if the operation fails
     */
    public User getUserByUsername(String username) throws DatabaseException {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by username: " + username, e);
        }
        
        return null;
    }

    /**
     * Finds a user by email address.
     *
     * @param email the email to search for
     * @return the user, or null if not found
     * @throws DatabaseException if the operation fails
     */
    public User getUserByEmail(String email) throws DatabaseException {
        String sql = "SELECT * FROM users WHERE email = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by email: " + email, e);
        }
        
        return null;
    }

    /**
     * Checks if a username already exists.
     *
     * @param username the username to check
     * @return true if exists, false otherwise
     * @throws DatabaseException if the operation fails
     */
    public boolean isUsernameExists(String username) throws DatabaseException {
        return getUserByUsername(username) != null;
    }

    /**
     * Checks if an email already exists.
     *
     * @param email the email to check
     * @return true if exists, false otherwise
     * @throws DatabaseException if the operation fails
     */
    public boolean isEmailExists(String email) throws DatabaseException {
        return getUserByEmail(email) != null;
    }

    /**
     * Updates user storage usage.
     *
     * @param userId the user ID
     * @param additionalSize the size to add (can be negative for deletion)
     * @throws DatabaseException if the operation fails
     */
    public void updateStorageUsed(Integer userId, long additionalSize) throws DatabaseException {
        String sql = "UPDATE users SET storage_used = storage_used + ? WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, additionalSize);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new DatabaseException("Error updating storage for user: " + userId, e);
        }
    }
}
