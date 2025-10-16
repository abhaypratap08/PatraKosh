package com.patrakosh.exception;

/**
 * Exception thrown when email format is invalid.
 */
public class InvalidEmailException extends ValidationException {
    
    public InvalidEmailException(String message) {
        super(message);
    }

    public InvalidEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
