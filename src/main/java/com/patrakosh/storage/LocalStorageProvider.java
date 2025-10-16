package com.patrakosh.storage;

import com.patrakosh.exception.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Local file system implementation of StorageProvider.
 * Handles file operations on the local file system.
 */
public class LocalStorageProvider implements StorageProvider {
    private static final Logger logger = LoggerFactory.getLogger(LocalStorageProvider.class);
    
    @Override
    public void uploadFile(File source, String destination) throws StorageException {
        if (source == null || !source.exists()) {
            throw new StorageException("Source file does not exist");
        }
        
        if (destination == null || destination.isEmpty()) {
            throw new StorageException("Destination path cannot be empty");
        }
        
        try {
            File destFile = new File(destination);
            
            // Create parent directories if they don't exist
            File parentDir = destFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    throw new StorageException("Failed to create destination directory: " + parentDir.getPath());
                }
            }
            
            // Copy file
            Files.copy(source.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            logger.info("File uploaded successfully: {} -> {}", source.getName(), destination);
            
        } catch (IOException e) {
            logger.error("Failed to upload file: {}", source.getName(), e);
            throw new StorageException("Failed to upload file: " + source.getName(), e);
        }
    }
    
    @Override
    public void downloadFile(String source, File destination) throws StorageException {
        if (source == null || source.isEmpty()) {
            throw new StorageException("Source path cannot be empty");
        }
        
        if (destination == null) {
            throw new StorageException("Destination file cannot be null");
        }
        
        File sourceFile = new File(source);
        
        if (!sourceFile.exists()) {
            throw new StorageException("Source file does not exist: " + source);
        }
        
        try {
            // Create parent directories if they don't exist
            File parentDir = destination.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    throw new StorageException("Failed to create destination directory: " + parentDir.getPath());
                }
            }
            
            // Copy file
            Files.copy(sourceFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            logger.info("File downloaded successfully: {} -> {}", source, destination.getName());
            
        } catch (IOException e) {
            logger.error("Failed to download file: {}", source, e);
            throw new StorageException("Failed to download file: " + source, e);
        }
    }
    
    @Override
    public void deleteFile(String path) throws StorageException {
        if (path == null || path.isEmpty()) {
            throw new StorageException("File path cannot be empty");
        }
        
        File file = new File(path);
        
        if (!file.exists()) {
            logger.warn("File does not exist, cannot delete: {}", path);
            return;
        }
        
        try {
            if (!file.delete()) {
                throw new StorageException("Failed to delete file: " + path);
            }
            
            logger.info("File deleted successfully: {}", path);
            
        } catch (SecurityException e) {
            logger.error("Security exception while deleting file: {}", path, e);
            throw new StorageException("Permission denied to delete file: " + path, e);
        }
    }
    
    @Override
    public boolean fileExists(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        
        File file = new File(path);
        return file.exists() && file.isFile();
    }
    
    @Override
    public long getFileSize(String path) throws StorageException {
        if (path == null || path.isEmpty()) {
            throw new StorageException("File path cannot be empty");
        }
        
        File file = new File(path);
        
        if (!file.exists()) {
            throw new StorageException("File does not exist: " + path);
        }
        
        if (!file.isFile()) {
            throw new StorageException("Path is not a file: " + path);
        }
        
        return file.length();
    }
    
    /**
     * Creates a directory at the specified path.
     *
     * @param path the directory path
     * @return true if created, false if already exists
     * @throws StorageException if creation fails
     */
    public boolean createDirectory(String path) throws StorageException {
        if (path == null || path.isEmpty()) {
            throw new StorageException("Directory path cannot be empty");
        }
        
        File dir = new File(path);
        
        if (dir.exists()) {
            return false;
        }
        
        if (!dir.mkdirs()) {
            throw new StorageException("Failed to create directory: " + path);
        }
        
        logger.info("Directory created: {}", path);
        return true;
    }
    
    /**
     * Checks if a directory exists.
     *
     * @param path the directory path
     * @return true if exists and is a directory
     */
    public boolean directoryExists(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        
        File dir = new File(path);
        return dir.exists() && dir.isDirectory();
    }
    
    /**
     * Moves a file from source to destination.
     *
     * @param source the source path
     * @param destination the destination path
     * @throws StorageException if move fails
     */
    public void moveFile(String source, String destination) throws StorageException {
        if (source == null || source.isEmpty()) {
            throw new StorageException("Source path cannot be empty");
        }
        
        if (destination == null || destination.isEmpty()) {
            throw new StorageException("Destination path cannot be empty");
        }
        
        File sourceFile = new File(source);
        File destFile = new File(destination);
        
        if (!sourceFile.exists()) {
            throw new StorageException("Source file does not exist: " + source);
        }
        
        try {
            Files.move(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("File moved: {} -> {}", source, destination);
        } catch (IOException e) {
            logger.error("Failed to move file: {} -> {}", source, destination, e);
            throw new StorageException("Failed to move file", e);
        }
    }
}
