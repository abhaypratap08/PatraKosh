package com.patrakosh.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Factory for creating storage service instances based on configuration
 */
@Component
@EnableConfigurationProperties(StorageProperties.class)
public class StorageFactory {

    private final StorageProperties storageProperties;

    @Autowired
    public StorageFactory(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    /**
     * Create storage service based on configuration
     */
    public StorageService createStorageService() {
        switch (storageProperties.getType().toLowerCase()) {
            case "local":
                return new LocalStorageService(storageProperties.getLocal());
            case "s3":
                return new S3StorageService(storageProperties.getS3());
            case "minio":
                return new MinIOStorageService(storageProperties.getMinio());
            default:
                throw new IllegalArgumentException("Unsupported storage type: " + storageProperties.getType());
        }
    }

    /**
     * Get current storage type
     */
    public String getCurrentStorageType() {
        return storageProperties.getType();
    }

    /**
     * Check if the current storage type supports the given feature
     */
    public boolean supportsFeature(String feature) {
        switch (feature.toLowerCase()) {
            case "presigned-urls":
                return !storageProperties.getType().equalsIgnoreCase("local");
            case "multipart-upload":
                return !storageProperties.getType().equalsIgnoreCase("local");
            case "metadata":
                return true; // All storage types support metadata
            case "versioning":
                return storageProperties.getType().equalsIgnoreCase("s3") || storageProperties.getType().equalsIgnoreCase("minio");
            default:
                return false;
        }
    }
}
