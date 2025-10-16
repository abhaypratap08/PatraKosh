package com.patrakosh.quota;

import com.patrakosh.dao.UserDAO;
import com.patrakosh.exception.DatabaseException;
import com.patrakosh.exception.InsufficientStorageException;
import com.patrakosh.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages storage quotas with synchronized operations.
 * Demonstrates thread-safe quota management with synchronized methods.
 */
public class StorageQuotaManager {
    private static final Logger logger = LoggerFactory.getLogger(StorageQuotaManager.class);
    private static StorageQuotaManager instance;
    
    // Cache of user quotas for fast access
    private final Map<Integer, Long> userQuotas;
    private final Map<Integer, Long> userUsage;
    private final UserDAO userDAO;
    
    private StorageQuotaManager() {
        this.userQuotas = new ConcurrentHashMap<>();
        this.userUsage = new ConcurrentHashMap<>();
        this.userDAO = new UserDAO();
    }
    
    /**
     * Gets the singleton instance.
     *
     * @return the StorageQuotaManager instance
     */
    public static synchronized StorageQuotaManager getInstance() {
        if (instance == null) {
            instance = new StorageQuotaManager();
        }
        return instance;
    }
    
    /**
     * Checks and updates quota atomically (synchronized).
     *
     * @param userId the user ID
     * @param fileSize the file size to add
     * @return true if quota available, false otherwise
     * @throws InsufficientStorageException if quota exceeded
     */
    public synchronized boolean checkAndUpdateQuota(Integer userId, long fileSize) 
            throws InsufficientStorageException, DatabaseException {
        
        // Load user data if not cached
        if (!userQuotas.containsKey(userId)) {
            loadUserQuota(userId);
        }
        
        Long currentUsed = userUsage.get(userId);
        Long quota = userQuotas.get(userId);
        
        if (currentUsed == null || quota == null) {
            throw new DatabaseException("User quota data not found for user: " + userId);
        }
        
        long newUsage = currentUsed + fileSize;
        
        if (newUsage > quota) {
            logger.warn("Quota exceeded for user {}: {} + {} > {}", userId, currentUsed, fileSize, quota);
            throw new InsufficientStorageException(
                String.format("Storage quota exceeded. Used: %d, Quota: %d, Requested: %d", 
                    currentUsed, quota, fileSize)
            );
        }
        
        // Update cache
        userUsage.put(userId, newUsage);
        
        logger.debug("Quota updated for user {}: {} -> {}", userId, currentUsed, newUsage);
        return true;
    }
    
    /**
     * Updates quota after file deletion (synchronized).
     *
     * @param userId the user ID
     * @param fileSize the file size to subtract
     */
    public synchronized void releaseQuota(Integer userId, long fileSize) {
        Long currentUsed = userUsage.get(userId);
        
        if (currentUsed != null) {
            long newUsage = Math.max(0, currentUsed - fileSize);
            userUsage.put(userId, newUsage);
            logger.debug("Quota released for user {}: {} -> {}", userId, currentUsed, newUsage);
        }
    }
    
    /**
     * Gets current usage for a user.
     *
     * @param userId the user ID
     * @return current usage in bytes
     */
    public synchronized long getCurrentUsage(Integer userId) throws DatabaseException {
        if (!userUsage.containsKey(userId)) {
            loadUserQuota(userId);
        }
        return userUsage.getOrDefault(userId, 0L);
    }
    
    /**
     * Gets quota limit for a user.
     *
     * @param userId the user ID
     * @return quota limit in bytes
     */
    public synchronized long getQuotaLimit(Integer userId) throws DatabaseException {
        if (!userQuotas.containsKey(userId)) {
            loadUserQuota(userId);
        }
        return userQuotas.getOrDefault(userId, 1073741824L); // 1GB default
    }
    
    /**
     * Gets usage percentage for a user.
     *
     * @param userId the user ID
     * @return usage percentage (0-100)
     */
    public synchronized double getUsagePercentage(Integer userId) throws DatabaseException {
        long usage = getCurrentUsage(userId);
        long quota = getQuotaLimit(userId);
        
        if (quota == 0) {
            return 0.0;
        }
        
        return (usage * 100.0) / quota;
    }
    
    /**
     * Checks if user is approaching quota limit (>90%).
     *
     * @param userId the user ID
     * @return true if approaching limit
     */
    public synchronized boolean isApproachingLimit(Integer userId) throws DatabaseException {
        return getUsagePercentage(userId) > 90.0;
    }
    
    /**
     * Loads user quota from database.
     *
     * @param userId the user ID
     */
    private void loadUserQuota(Integer userId) throws DatabaseException {
        User user = userDAO.findById(userId);
        
        if (user != null) {
            userQuotas.put(userId, user.getStorageQuota());
            userUsage.put(userId, user.getStorageUsed());
            logger.debug("Loaded quota for user {}: {}/{}", userId, user.getStorageUsed(), user.getStorageQuota());
        } else {
            throw new DatabaseException("User not found: " + userId);
        }
    }
    
    /**
     * Refreshes quota data from database.
     *
     * @param userId the user ID
     */
    public synchronized void refreshQuota(Integer userId) throws DatabaseException {
        userQuotas.remove(userId);
        userUsage.remove(userId);
        loadUserQuota(userId);
    }
    
    /**
     * Clears all cached quota data.
     */
    public synchronized void clearCache() {
        userQuotas.clear();
        userUsage.clear();
        logger.info("Quota cache cleared");
    }
    
    /**
     * Gets quota statistics.
     *
     * @return statistics string
     */
    public String getStatistics() {
        return String.format("QuotaManager Stats - Cached users: %d", userQuotas.size());
    }
}
