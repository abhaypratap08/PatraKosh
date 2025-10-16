package com.patrakosh.transaction;

import java.sql.Connection;

/**
 * Functional interface for transaction callbacks.
 * Allows executing operations within a transaction context.
 *
 * @param <T> the return type of the callback
 */
@FunctionalInterface
public interface TransactionCallback<T> {
    /**
     * Executes the callback logic within a transaction.
     *
     * @param connection the database connection
     * @return the result of the operation
     * @throws Exception if the operation fails
     */
    T execute(Connection connection) throws Exception;
}
