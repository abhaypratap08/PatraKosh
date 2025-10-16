package com.patrakosh.transaction;

import com.patrakosh.exception.TransactionException;
import com.patrakosh.util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Manages database transactions with proper commit/rollback handling.
 * Uses ThreadLocal to maintain transaction context per thread.
 */
public class TransactionManager {
    private static TransactionManager instance;
    private static final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();

    private TransactionManager() {
    }

    /**
     * Gets the singleton instance of TransactionManager.
     *
     * @return the TransactionManager instance
     */
    public static synchronized TransactionManager getInstance() {
        if (instance == null) {
            instance = new TransactionManager();
        }
        return instance;
    }

    /**
     * Executes a callback within a transaction.
     * Automatically commits on success or rolls back on failure.
     *
     * @param callback the transaction callback
     * @param <T> the return type
     * @return the result from the callback
     * @throws TransactionException if the transaction fails
     */
    public <T> T executeInTransaction(TransactionCallback<T> callback) throws TransactionException {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            connectionHolder.set(conn);
            conn.setAutoCommit(false);

            T result = callback.execute(conn);
            conn.commit();
            return result;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    throw new TransactionException("Transaction rollback failed", rollbackEx);
                }
            }
            throw new TransactionException("Transaction failed", e);

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    // Log but don't throw
                }
            }
            connectionHolder.remove();
        }
    }

    /**
     * Gets the current transaction connection for the current thread.
     *
     * @return the current connection, or null if not in a transaction
     */
    public Connection getCurrentConnection() {
        return connectionHolder.get();
    }
}
