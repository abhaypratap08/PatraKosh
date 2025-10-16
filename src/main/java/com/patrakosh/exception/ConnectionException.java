package com.patrakosh.exception;

/**
 * Exception thrown when database connection fails.
 */
public class ConnectionException extends DatabaseException {
    
    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
