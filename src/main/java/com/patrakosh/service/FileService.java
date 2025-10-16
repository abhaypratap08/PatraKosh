package com.patrakosh.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.patrakosh.dao.ActivityLogDAO;
import com.patrakosh.dao.FileDAO;
import com.patrakosh.dao.UserDAO;
import com.patrakosh.exception.DatabaseException;
import com.patrakosh.exception.FileDownloadException;
import com.patrakosh.exception.FileUploadException;
import com.patrakosh.exception.StorageException;
import com.patrakosh.exception.ValidationException;
import com.patrakosh.model.FileItem;
import com.patrakosh.util.Config;

/**
 * File service handling file operations (upload, download, delete, search).
 * Extends BaseService to inherit logging and transaction management capabilities.
 */
public class FileService extends BaseService {
    private final FileDAO fileDAO;
    private final UserDAO userDAO;
    private final ActivityLogDAO activityLogDAO;
    
    public FileService() {
        super();
        this.fileDAO = new FileDAO();
        this.userDAO = new UserDAO();
        this.activityLogDAO = new ActivityLogDAO();
    }
    
    /**
     * Uploads a file for a user with transaction support.
     *
     * @param userId the user ID
     * @param sourceFile the file to upload
     * @return the created file item
     * @throws StorageException if upload fails
     */
    public FileItem uploadFile(Integer userId, File sourceFile) throws StorageException {
        logOperation("uploadFile", userId, sourceFile.getName());
        
        try {
            try {
                validateInput(sourceFile);
            } catch (ValidationException e) {
                throw new FileUploadException("Invalid input", e);
            }
            
            if (!sourceFile.exists()) {
                throw new FileUploadException("File does not exist: " + sourceFile.getName());
            }
            
            // Create user directory if it doesn't exist
            String userStoragePath = Config.getStorageBasePath() + "/user_" + userId;
            File userDir = new File(userStoragePath);
            if (!userDir.exists()) {
                userDir.mkdirs();
            }
            
            // Generate unique filename if file already exists
            String filename = sourceFile.getName();
            String destinationPath = userStoragePath + "/" + filename;
            File destFile = new File(destinationPath);
            
            int counter = 1;
            while (destFile.exists()) {
                int dotIndex = filename.lastIndexOf('.');
                if (dotIndex > 0) {
                    String nameWithoutExt = filename.substring(0, dotIndex);
                    String extension = filename.substring(dotIndex);
                    filename = nameWithoutExt + "_" + counter + extension;
                } else {
                    filename = filename + "_" + counter;
                }
                destinationPath = userStoragePath + "/" + filename;
                destFile = new File(destinationPath);
                counter++;
            }
            
            // Calculate file hash
            String fileHash = calculateFileHash(sourceFile);
            
            // Detect MIME type
            String mimeType = detectMimeType(sourceFile);
            
            // Copy file to storage
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            // Save file metadata and update storage in transaction
            final String finalFilename = filename;
            final String finalDestinationPath = destinationPath;
            final long fileSize = sourceFile.length();
            
            FileItem fileItem = transactionManager.executeInTransaction(conn -> {
                // Create file item
                FileItem item = new FileItem(userId, finalFilename, finalDestinationPath, fileSize);
                item.setFileHash(fileHash);
                item.setMimeType(mimeType);
                item.setVersion(1);
                
                // Save file
                FileItem savedItem = fileDAO.save(item);
                
                // Update user storage
                userDAO.updateStorageUsed(userId, fileSize);
                
                // Log activity
                activityLogDAO.logActivity(userId, "UPLOAD", "FILE", savedItem.getId(), 
                    "Uploaded file: " + finalFilename);
                
                return savedItem;
            });
            
            logger.info("File uploaded successfully: {} for user: {}", filename, userId);
            return fileItem;
            
        } catch (IOException e) {
            logError("uploadFile", e);
            throw new FileUploadException("Failed to upload file: " + sourceFile.getName(), e);
        } catch (Exception e) {
            logError("uploadFile", e);
            throw new FileUploadException("Failed to save file metadata", e);
        }
    }
    
    /**
     * Gets all files for a user.
     *
     * @param userId the user ID
     * @return list of files
     * @throws StorageException if operation fails
     */
    public List<FileItem> getUserFiles(Integer userId) throws StorageException {
        logOperation("getUserFiles", userId);
        
        try {
            return fileDAO.getFilesByUserId(userId);
        } catch (DatabaseException e) {
            logError("getUserFiles", e);
            throw new StorageException("Failed to get files for user: " + userId, e);
        }
    }
    
    /**
     * Downloads a file.
     *
     * @param fileItem the file to download
     * @param destinationFile the destination
     * @return true if successful
     * @throws StorageException if download fails
     */
    public boolean downloadFile(FileItem fileItem, File destinationFile) throws StorageException {
        logOperation("downloadFile", fileItem.getId(), destinationFile.getName());
        
        try {
            File sourceFile = new File(fileItem.getFilepath());
            
            if (!sourceFile.exists()) {
                throw new FileDownloadException("Source file not found: " + fileItem.getFilename());
            }
            
            Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            // Log download activity
            try {
                activityLogDAO.logActivity(fileItem.getUserId(), "DOWNLOAD", "FILE", fileItem.getId(),
                    "Downloaded file: " + fileItem.getFilename());
            } catch (DatabaseException e) {
                logger.warn("Failed to log download activity", e);
            }
            
            logger.info("File downloaded successfully: {}", fileItem.getFilename());
            return true;
            
        } catch (IOException e) {
            logError("downloadFile", e);
            throw new FileDownloadException("Failed to download file: " + fileItem.getFilename(), e);
        }
    }
    
    /**
     * Deletes a file with transaction support.
     *
     * @param fileId the file ID
     * @return true if successful
     * @throws StorageException if deletion fails
     */
    public boolean deleteFile(Integer fileId) throws StorageException {
        logOperation("deleteFile", fileId);
        
        try {
            FileItem fileItem = fileDAO.findById(fileId);
            
            if (fileItem == null) {
                throw new StorageException("File not found in database: " + fileId);
            }
            
            // Delete file and update storage in transaction
            return transactionManager.executeInTransaction(conn -> {
                // Delete from database
                boolean deleted = fileDAO.delete(fileId);
                
                if (deleted) {
                    // Update user storage
                    userDAO.updateStorageUsed(fileItem.getUserId(), -fileItem.getFileSize());
                    
                    // Log activity
                    activityLogDAO.logActivity(fileItem.getUserId(), "DELETE", "FILE", fileId,
                        "Deleted file: " + fileItem.getFilename());
                    
                    // Delete physical file
                    File file = new File(fileItem.getFilepath());
                    if (file.exists()) {
                        file.delete();
                    }
                }
                
                return deleted;
            });
            
        } catch (Exception e) {
            logError("deleteFile", e);
            throw new StorageException("Failed to delete file: " + fileId, e);
        }
    }
    
    /**
     * Updates a file's metadata (e.g., filename).
     *
     * @param fileItem the file item with updated information
     * @return the updated file item
     * @throws StorageException if update fails
     */
    public FileItem updateFile(FileItem fileItem) throws StorageException {
        logOperation("updateFile", fileItem.getId(), fileItem.getFilename());
        
        try {
            FileItem updatedFile = fileDAO.update(fileItem);
            
            // Log activity
            try {
                activityLogDAO.logActivity(fileItem.getUserId(), "RENAME", "FILE", fileItem.getId(), 
                    "Renamed to: " + fileItem.getFilename());
            } catch (DatabaseException e) {
                logger.warn("Failed to log rename activity", e);
            }
            
            logger.info("File updated successfully: {}", fileItem.getId());
            return updatedFile;
            
        } catch (DatabaseException e) {
            logError("updateFile", e);
            throw new StorageException("Failed to update file: " + fileItem.getId(), e);
        }
    }
    
    /**
     * Searches files by filename.
     *
     * @param userId the user ID
     * @param searchTerm the search term
     * @return list of matching files
     * @throws StorageException if search fails
     */
    public List<FileItem> searchFiles(Integer userId, String searchTerm) throws StorageException {
        logOperation("searchFiles", userId, searchTerm);
        
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return getUserFiles(userId);
            }
            return fileDAO.searchFiles(userId, searchTerm);
        } catch (DatabaseException e) {
            logError("searchFiles", e);
            throw new StorageException("Failed to search files for user: " + userId, e);
        }
    }
    
    /**
     * Gets total storage used by a user.
     *
     * @param userId the user ID
     * @return storage used in bytes
     * @throws StorageException if operation fails
     */
    public long getTotalStorageUsed(Integer userId) throws StorageException {
        try {
            return fileDAO.getTotalStorageUsed(userId);
        } catch (DatabaseException e) {
            logError("getTotalStorageUsed", e);
            throw new StorageException("Failed to calculate storage for user: " + userId, e);
        }
    }
    
    /**
     * Gets file count for a user.
     *
     * @param userId the user ID
     * @return number of files
     * @throws StorageException if operation fails
     */
    public int getFileCount(Integer userId) throws StorageException {
        try {
            return fileDAO.getFileCount(userId);
        } catch (DatabaseException e) {
            logError("getFileCount", e);
            throw new StorageException("Failed to count files for user: " + userId, e);
        }
    }
    
    @Override
    protected void validateInput(Object input) throws ValidationException {
        if (input == null) {
            throw new ValidationException("Input cannot be null");
        }
        
        if (input instanceof File) {
            File file = (File) input;
            if (!file.exists()) {
                throw new ValidationException("File does not exist: " + file.getName());
            }
        }
    }
    
    /**
     * Calculates SHA-256 hash of a file.
     *
     * @param file the file
     * @return the hash string
     */
    private String calculateFileHash(File file) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            byte[] hash = md.digest(fileBytes);
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            logger.warn("Failed to calculate file hash", e);
            return null;
        }
    }
    
    /**
     * Detects MIME type of a file.
     *
     * @param file the file
     * @return the MIME type
     */
    private String detectMimeType(File file) {
        try {
            String mimeType = Files.probeContentType(file.toPath());
            return mimeType != null ? mimeType : "application/octet-stream";
        } catch (IOException e) {
            logger.warn("Failed to detect MIME type", e);
            return "application/octet-stream";
        }
    }
}
