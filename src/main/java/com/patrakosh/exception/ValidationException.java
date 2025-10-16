package com.patrakosh.exception;

/**
 * Exception thrown when input validation fails.
 */
public class ValidationException extends PatraKoshException {
    
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
