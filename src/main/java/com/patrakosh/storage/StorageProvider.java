package com.patrakosh.storage;

import com.patrakosh.exception.StorageException;
import java.io.File;

/**
 * Interface for storage providers that handle file operations.
 * Allows different storage implementations (local, cloud, etc.).
 */
public interface StorageProvider {
    /**
     * Uploads a file to the storage.
     *
     * @param source the source file to upload
     * @param destination the destination path in storage
     * @throws StorageException if upload fails
     */
    void uploadFile(File source, String destination) throws StorageException;

    /**
     * Downloads a file from the storage.
     *
     * @param source the source path in storage
     * @param destination the destination file
     * @throws StorageException if download fails
     */
    void downloadFile(String source, File destination) throws StorageException;

    /**
     * Deletes a file from the storage.
     *
     * @param path the path of the file to delete
     * @throws StorageException if deletion fails
     */
    void deleteFile(String path) throws StorageException;

    /**
     * Checks if a file exists in the storage.
     *
     * @param path the path to check
     * @return true if the file exists, false otherwise
     */
    boolean fileExists(String path);

    /**
     * Gets the size of a file in the storage.
     *
     * @param path the path of the file
     * @return the file size in bytes
     * @throws StorageException if the file doesn't exist or size cannot be determined
     */
    long getFileSize(String path) throws StorageException;
}
