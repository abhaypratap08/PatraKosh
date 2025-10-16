package com.patrakosh.exception;

/**
 * Exception thrown when password format is invalid.
 */
public class InvalidPasswordException extends ValidationException {
    
    public InvalidPasswordException(String message) {
        super(message);
    }

    public InvalidPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}
