package com.patrakosh.exception;

/**
 * Exception thrown when a user is not found in the system.
 */
public class UserNotFoundException extends AuthenticationException {
    
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
