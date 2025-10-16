package com.patrakosh.exception;

/**
 * Exception thrown when storage operations fail.
 */
public class StorageException extends PatraKoshException {
    
    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
