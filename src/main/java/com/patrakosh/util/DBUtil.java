package com.patrakosh.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.patrakosh.transaction.TransactionManager;

public class DBUtil {
    
    /**
     * Gets a database connection.
     * If called within a transaction context, returns the transaction connection.
     * Otherwise, creates a new connection.
     *
     * @return a database connection
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        // Check if we're in a transaction context
        Connection transactionConn = TransactionManager.getInstance().getCurrentConnection();
        if (transactionConn != null) {
            return transactionConn;
        }
        
        // Otherwise create a new connection
        try {
            Class.forName(Config.getDbDriver());
            return DriverManager.getConnection(
                Config.getDbUrl(),
                Config.getDbUsername(),
                Config.getDbPassword()
            );
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }
    
    /**
     * Closes a connection if it's not part of a transaction.
     * Transaction connections should not be closed manually.
     *
     * @param conn the connection to close
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                // Only close if not in transaction context
                Connection transactionConn = TransactionManager.getInstance().getCurrentConnection();
                if (transactionConn == null || transactionConn != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
