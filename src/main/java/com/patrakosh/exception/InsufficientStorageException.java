package com.patrakosh.exception;

/**
 * Exception thrown when user has insufficient storage quota.
 */
public class InsufficientStorageException extends StorageException {
    
    public InsufficientStorageException(String message) {
        super(message);
    }

    public InsufficientStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
