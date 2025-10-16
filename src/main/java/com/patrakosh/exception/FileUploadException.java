package com.patrakosh.exception;

/**
 * Exception thrown when file upload fails.
 */
public class FileUploadException extends StorageException {
    
    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
