package com.patrakosh.exception;

/**
 * Exception thrown when database transaction fails.
 */
public class TransactionException extends DatabaseException {
    
    public TransactionException(String message) {
        super(message);
    }

    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
