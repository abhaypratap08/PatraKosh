package com.patrakosh.exception;

/**
 * Base exception class for all PatraKosh application exceptions.
 * Provides a common parent for all custom exceptions in the system.
 */
public abstract class PatraKoshException extends Exception {
    
    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    public PatraKoshException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public PatraKoshException(String message, Throwable cause) {
        super(message, cause);
    }
}
