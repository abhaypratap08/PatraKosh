package com.patrakosh.exception;

/**
 * Exception thrown when authentication fails.
 */
public class AuthenticationException extends PatraKoshException {
    
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
