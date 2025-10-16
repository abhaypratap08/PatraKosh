package com.patrakosh.cache;

import com.patrakosh.model.FileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches file metadata using concurrent collections.
 * Demonstrates proper use of ConcurrentHashMap for thread-safe caching.
 */
public class FileCache {
    private static final Logger logger = LoggerFactory.getLogger(FileCache.class);
    private static FileCache instance;
    
    // Map of file ID to FileItem
    private final Map<Integer, FileItem> fileCache;
    
    // Map of user ID to list of file IDs
    private final Map<Integer, List<FileItem>> userFilesCache;
    
    // Maximum cache size
    private static final int MAX_CACHE_SIZE = 1000;
    
    private FileCache() {
        this.fileCache = new ConcurrentHashMap<>();
        this.userFilesCache = new ConcurrentHashMap<>();
    }
    
    /**
     * Gets the singleton instance of FileCache.
     *
     * @return the FileCache instance
     */
    public static synchronized FileCache getInstance() {
        if (instance == null) {
            instance = new FileCache();
        }
        return instance;
    }
    
    /**
     * Caches a file item.
     *
     * @param file the file to cache
     */
    public void cacheFile(FileItem file) {
        if (file == null || file.getId() == null) {
            return;
        }
        
        // Check cache size limit
        if (fileCache.size() >= MAX_CACHE_SIZE) {
            evictOldestEntry();
        }
        
        fileCache.put(file.getId(), file);
        
        // Update user files cache
        userFilesCache.compute(file.getUserId(), (userId, files) -> {
            if (files == null) {
                files = new ArrayList<>();
            }
            // Remove if already exists and add updated version
            files.removeIf(f -> f.getId().equals(file.getId()));
            files.add(file);
            return files;
        });
        
        logger.debug("File cached: {} for user: {}", file.getFilename(), file.getUserId());
    }
    
    /**
     * Gets a file from cache.
     *
     * @param fileId the file ID
     * @return the file, or null if not in cache
     */
    public FileItem getFile(Integer fileId) {
        return fileCache.get(fileId);
    }
    
    /**
     * Gets all cached files for a user.
     *
     * @param userId the user ID
     * @return list of files
     */
    public List<FileItem> getUserFiles(Integer userId) {
        return userFilesCache.getOrDefault(userId, new ArrayList<>());
    }
    
    /**
     * Removes a file from cache.
     *
     * @param fileId the file ID
     */
    public void removeFile(Integer fileId) {
        FileItem file = fileCache.remove(fileId);
        
        if (file != null) {
            // Remove from user files cache
            userFilesCache.computeIfPresent(file.getUserId(), (userId, files) -> {
                files.removeIf(f -> f.getId().equals(fileId));
                return files.isEmpty() ? null : files;
            });
            
            logger.debug("File removed from cache: {}", file.getFilename());
        }
    }
    
    /**
     * Clears all cached files for a user.
     *
     * @param userId the user ID
     */
    public void clearUserCache(Integer userId) {
        List<FileItem> files = userFilesCache.remove(userId);
        
        if (files != null) {
            for (FileItem file : files) {
                fileCache.remove(file.getId());
            }
            logger.debug("Cache cleared for user ID: {}", userId);
        }
    }
    
    /**
     * Checks if a file is in cache.
     *
     * @param fileId the file ID
     * @return true if cached, false otherwise
     */
    public boolean isCached(Integer fileId) {
        return fileCache.containsKey(fileId);
    }
    
    /**
     * Gets the current cache size.
     *
     * @return number of cached files
     */
    public int getCacheSize() {
        return fileCache.size();
    }
    
    /**
     * Clears the entire cache.
     */
    public void clearAll() {
        fileCache.clear();
        userFilesCache.clear();
        logger.info("File cache cleared");
    }
    
    /**
     * Evicts the oldest entry when cache is full.
     * Simple implementation - removes first entry found.
     */
    private void evictOldestEntry() {
        if (!fileCache.isEmpty()) {
            Integer firstKey = fileCache.keySet().iterator().next();
            removeFile(firstKey);
            logger.debug("Evicted file from cache due to size limit");
        }
    }
    
    /**
     * Gets cache statistics.
     *
     * @return cache statistics as string
     */
    public String getCacheStats() {
        return String.format("FileCache Stats - Total Files: %d, Users: %d, Max Size: %d",
                fileCache.size(), userFilesCache.size(), MAX_CACHE_SIZE);
    }
}
