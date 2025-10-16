package com.patrakosh.exception;

/**
 * Exception thrown when file download fails.
 */
public class FileDownloadException extends StorageException {
    
    public FileDownloadException(String message) {
        super(message);
    }

    public FileDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
