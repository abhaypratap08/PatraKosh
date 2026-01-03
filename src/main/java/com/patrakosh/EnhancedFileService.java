package com.patrakosh.service;

import com.patrakosh.storage.StorageService;
import com.patrakosh.storage.StorageResult;
import com.patrakosh.storage.FileInfo;
import com.patrakosh.model.File;
import com.patrakosh.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enhanced File Service with storage abstraction
 * Supports multiple storage backends (Local, S3, MinIO)
 */
@Service
@Transactional
public class EnhancedFileService {

    private final StorageService storageService;
    private final FileRepository fileRepository;

    @Autowired
    public EnhancedFileService(StorageService storageService, FileRepository fileRepository) {
        this.storageService = storageService;
        this.fileRepository = fileRepository;
    }

    /**
     * Upload file using the configured storage backend
     */
    public File uploadFile(MultipartFile multipartFile, Long userId) {
        try {
            // Generate unique file key
            String fileKey = generateFileKey(multipartFile.getOriginalFilename(), userId);
            
            // Prepare metadata
            Map<String, String> metadata = new HashMap<>();
            metadata.put("original-filename", multipartFile.getOriginalFilename());
            metadata.put("content-type", multipartFile.getContentType());
            metadata.put("file-size", String.valueOf(multipartFile.getSize()));
            metadata.put("user-id", String.valueOf(userId));
            metadata.put("upload-ip", "localhost"); // Would get from request in real implementation
            
            // Upload to storage
            StorageResult result = storageService.uploadFile(
                    fileKey, 
                    multipartFile.getInputStream(), 
                    multipartFile.getContentType(), 
                    metadata
            );
            
            if (!result.isSuccess()) {
                throw new RuntimeException("Storage upload failed: " + result.getErrorMessage());
            }
            
            // Save file metadata to database
            File file = new File();
            file.setFilename(multipartFile.getOriginalFilename());
            file.setFilePath(result.getKey());
            file.setFileSize(multipartFile.getSize());
            file.setContentType(multipartFile.getContentType());
            file.setUserId(userId);
            file.setStorageUrl(result.getFileUrl());
            file.setStorageType(storageService.getStorageType());
            
            return fileRepository.save(file);
            
        } catch (IOException e) {
            throw new RuntimeException("File upload failed", e);
        }
    }

    /**
     * Download file using the configured storage backend
     */
    public InputStream downloadFile(Long fileId, Long userId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
        
        // Verify user ownership
        if (!file.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        return storageService.downloadFile(file.getFilePath());
    }

    /**
     * Delete file from both storage and database
     */
    public boolean deleteFile(Long fileId, Long userId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
        
        // Verify user ownership
        if (!file.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        try {
            // Delete from storage
            boolean storageDeleted = storageService.deleteFile(file.getFilePath());
            
            // Delete from database
            fileRepository.delete(file);
            
            return storageDeleted;
        } catch (Exception e) {
            throw new RuntimeException("File deletion failed", e);
        }
    }

    /**
     * Get file metadata
     */
    public Map<String, Object> getFileMetadata(Long fileId, Long userId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
        
        // Verify user ownership
        if (!file.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        // Get storage metadata
        Map<String, String> storageMetadata = storageService.getFileMetadata(file.getFilePath());
        
        // Combine database and storage metadata
        Map<String, Object> combinedMetadata = new HashMap<>();
        combinedMetadata.put("id", file.getId());
        combinedMetadata.put("filename", file.getFilename());
        combinedMetadata.put("fileSize", file.getFileSize());
        combinedMetadata.put("contentType", file.getContentType());
        combinedMetadata.put("uploadTime", file.getUploadTime());
        combinedMetadata.put("storageType", file.getStorageType());
        combinedMetadata.put("storageUrl", file.getStorageUrl());
        combinedMetadata.putAll(storageMetadata);
        
        return combinedMetadata;
    }

    /**
     * Generate pre-signed URL for direct upload/download
     */
    public String generatePresignedUrl(Long fileId, Long userId, String operation, int expiration) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
        
        // Verify user ownership
        if (!file.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        return storageService.generatePresignedUrl(file.getFilePath(), expiration, operation);
    }

    /**
     * List user's files
     */
    public List<Map<String, Object>> listUserFiles(Long userId, int limit, int offset) {
        List<File> files = fileRepository.findByUserIdOrderByUploadTimeDesc(userId);
        
        return files.stream()
                .skip(offset)
                .limit(limit)
                .map(file -> {
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("id", file.getId());
                    fileInfo.put("filename", file.getFilename());
                    fileInfo.put("fileSize", file.getFileSize());
                    fileInfo.put("contentType", file.getContentType());
                    fileInfo.put("uploadTime", file.getUploadTime());
                    fileInfo.put("storageType", file.getStorageType());
                    fileInfo.put("storageUrl", file.getStorageUrl());
                    return fileInfo;
                })
                .collect(Collectors.toList());
    }

    /**
     * Search files by name
     */
    public List<Map<String, Object>> searchFiles(Long userId, String query, int limit) {
        List<File> files = fileRepository.findByUserIdAndFilenameContainingIgnoreCaseOrderByUploadTimeDesc(userId, query);
        
        return files.stream()
                .limit(limit)
                .map(file -> {
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("id", file.getId());
                    fileInfo.put("filename", file.getFilename());
                    fileInfo.put("fileSize", file.getFileSize());
                    fileInfo.put("contentType", file.getContentType());
                    fileInfo.put("uploadTime", file.getUploadTime());
                    fileInfo.put("storageType", file.getStorageType());
                    fileInfo.put("storageUrl", file.getStorageUrl());
                    return fileInfo;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get storage statistics for user
     */
    public Map<String, Object> getUserStorageStats(Long userId) {
        List<File> files = fileRepository.findByUserId(userId);
        
        long totalSize = files.stream().mapToLong(File::getFileSize).sum();
        int fileCount = files.size();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSize", totalSize);
        stats.put("fileCount", fileCount);
        stats.put("storageType", storageService.getStorageType());
        stats.put("isHealthy", storageService.isHealthy());
        
        // Add storage-specific stats if available
        if (storageService instanceof LocalStorageService) {
            LocalStorageService.LocalStorageStats localStats = ((LocalStorageService) storageService).getStorageStats();
            stats.put("availableSpace", localStats.getAvailableSpace());
        }
        
        return stats;
    }

    /**
     * Migrate files from one storage backend to another
     */
    @Transactional
    public int migrateFiles(String fromStorageType, String toStorageType, int batchSize) {
        // This would be implemented in a separate migration service
        // For now, return placeholder
        return 0;
    }

    /**
     * Generate unique file key
     */
    private String generateFileKey(String originalFilename, Long userId) {
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        
        String baseName = originalFilename.substring(0, dotIndex > 0 ? dotIndex : originalFilename.length());
        baseName = baseName.replaceAll("[^a-zA-Z0-9.-]", "_");
        
        return String.format("users/%d/%s_%d%s", 
                userId, 
                baseName, 
                System.currentTimeMillis(), 
                extension);
    }
}
