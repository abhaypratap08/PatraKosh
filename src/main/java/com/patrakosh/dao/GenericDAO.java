package com.patrakosh.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.patrakosh.core.Identifiable;
import com.patrakosh.exception.DatabaseException;
import com.patrakosh.util.DBUtil;

/**
 * Abstract generic DAO providing common CRUD operations.
 * Subclasses must implement template methods for specific entity types.
 *
 * @param <T> the entity type that implements Identifiable
 * @param <ID> the identifier type
 */
public abstract class GenericDAO<T extends Identifiable<ID>, ID> {

    /**
     * Gets the table name for this entity.
     *
     * @return the table name
     */
    protected abstract String getTableName();

    /**
     * Maps a ResultSet row to an entity instance.
     *
     * @param rs the ResultSet positioned at a row
     * @return the mapped entity
     * @throws SQLException if mapping fails
     */
    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;

    /**
     * Sets parameters for an INSERT statement.
     *
     * @param stmt the PreparedStatement
     * @param entity the entity to insert
     * @throws SQLException if parameter setting fails
     */
    protected abstract void setInsertParameters(PreparedStatement stmt, T entity) throws SQLException;

    /**
     * Sets parameters for an UPDATE statement.
     *
     * @param stmt the PreparedStatement
     * @param entity the entity to update
     * @throws SQLException if parameter setting fails
     */
    protected abstract void setUpdateParameters(PreparedStatement stmt, T entity) throws SQLException;

    /**
     * Gets the name of the ID column.
     *
     * @return the ID column name (default: "id")
     */
    protected String getIdColumnName() {
        return "id";
    }

    /**
     * Finds an entity by its ID.
     *
     * @param id the entity ID
     * @return the entity, or null if not found
     * @throws DatabaseException if the operation fails
     */
    public T findById(ID id) throws DatabaseException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE " + getIdColumnName() + " = ?";
        Connection conn = null;
        
        try {
            conn = DBUtil.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, id);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToEntity(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding entity by ID: " + id, e);
        } finally {
            DBUtil.closeConnection(conn);
        }

        return null;
    }

    /**
     * Finds all entities.
     *
     * @return list of all entities
     * @throws DatabaseException if the operation fails
     */
    public List<T> findAll() throws DatabaseException {
        String sql = "SELECT * FROM " + getTableName();
        List<T> entities = new ArrayList<>();
        Connection conn = null;

        try {
            conn = DBUtil.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    entities.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding all entities", e);
        } finally {
            DBUtil.closeConnection(conn);
        }

        return entities;
    }

    /**
     * Saves a new entity.
     *
     * @param entity the entity to save
     * @return the saved entity with generated ID
     * @throws DatabaseException if the operation fails
     */
    public T save(T entity) throws DatabaseException {
        String sql = getInsertSQL();
        Connection conn = null;

        try {
            conn = DBUtil.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                setInsertParameters(stmt, entity);
                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            // Get the generated ID and convert to Integer
                            // MySQL returns BigInteger for auto-increment, so we need to convert
                            Object idObj = rs.getObject(1);
                            if (idObj instanceof Number) {
                                @SuppressWarnings("unchecked")
                                ID generatedId = (ID) Integer.valueOf(((Number) idObj).intValue());
                                entity.setId(generatedId);
                            }
                        }
                    }
                }

                return entity;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error saving entity", e);
        } finally {
            DBUtil.closeConnection(conn);
        }
    }

    /**
     * Updates an existing entity.
     *
     * @param entity the entity to update
     * @return the updated entity
     * @throws DatabaseException if the operation fails
     */
    public T update(T entity) throws DatabaseException {
        String sql = getUpdateSQL();
        Connection conn = null;

        try {
            conn = DBUtil.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                setUpdateParameters(stmt, entity);
                stmt.setObject(getUpdateParameterCount() + 1, entity.getId());
                stmt.executeUpdate();

                return entity;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error updating entity", e);
        } finally {
            DBUtil.closeConnection(conn);
        }
    }

    /**
     * Deletes an entity by its ID.
     *
     * @param id the entity ID
     * @return true if deleted, false if not found
     * @throws DatabaseException if the operation fails
     */
    public boolean delete(ID id) throws DatabaseException {
        String sql = "DELETE FROM " + getTableName() + " WHERE " + getIdColumnName() + " = ?";
        Connection conn = null;

        try {
            conn = DBUtil.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setObject(1, id);
                int affectedRows = stmt.executeUpdate();

                return affectedRows > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting entity with ID: " + id, e);
        } finally {
            DBUtil.closeConnection(conn);
        }
    }

    /**
     * Gets the INSERT SQL statement.
     * Subclasses can override to provide custom SQL.
     *
     * @return the INSERT SQL
     */
    protected abstract String getInsertSQL();

    /**
     * Gets the UPDATE SQL statement.
     * Subclasses can override to provide custom SQL.
     *
     * @return the UPDATE SQL
     */
    protected abstract String getUpdateSQL();

    /**
     * Gets the number of parameters in the UPDATE statement (excluding ID).
     *
     * @return the parameter count
     */
    protected abstract int getUpdateParameterCount();
}
