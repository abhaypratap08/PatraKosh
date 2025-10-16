package com.patrakosh.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Detects duplicate files using file hashes.
 * Demonstrates proper use of ConcurrentHashMap.newKeySet() for thread-safe Set operations.
 */
public class DuplicateDetector {
    private static final Logger logger = LoggerFactory.getLogger(DuplicateDetector.class);
    private static DuplicateDetector instance;
    
    // Set of file hashes for duplicate detection
    private final Set<String> fileHashes;
    
    private DuplicateDetector() {
        this.fileHashes = ConcurrentHashMap.newKeySet();
    }
    
    /**
     * Gets the singleton instance of DuplicateDetector.
     *
     * @return the DuplicateDetector instance
     */
    public static synchronized DuplicateDetector getInstance() {
        if (instance == null) {
            instance = new DuplicateDetector();
        }
        return instance;
    }
    
    /**
     * Checks if a file hash already exists (duplicate).
     *
     * @param hash the file hash
     * @return true if duplicate, false otherwise
     */
    public boolean isDuplicate(String hash) {
        if (hash == null || hash.isEmpty()) {
            return false;
        }
        return fileHashes.contains(hash);
    }
    
    /**
     * Adds a file hash to the set.
     *
     * @param hash the file hash
     * @return true if added (not duplicate), false if already exists
     */
    public boolean addHash(String hash) {
        if (hash == null || hash.isEmpty()) {
            return false;
        }
        
        boolean added = fileHashes.add(hash);
        if (added) {
            logger.debug("File hash added to duplicate detector");
        } else {
            logger.debug("Duplicate file hash detected");
        }
        return added;
    }
    
    /**
     * Removes a file hash from the set.
     *
     * @param hash the file hash
     * @return true if removed, false if not found
     */
    public boolean removeHash(String hash) {
        if (hash == null || hash.isEmpty()) {
            return false;
        }
        
        boolean removed = fileHashes.remove(hash);
        if (removed) {
            logger.debug("File hash removed from duplicate detector");
        }
        return removed;
    }
    
    /**
     * Gets the count of unique file hashes.
     *
     * @return number of unique hashes
     */
    public int getHashCount() {
        return fileHashes.size();
    }
    
    /**
     * Clears all file hashes.
     */
    public void clearAll() {
        fileHashes.clear();
        logger.info("Duplicate detector cleared");
    }
    
    /**
     * Checks if the detector contains any hashes.
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return fileHashes.isEmpty();
    }
}
