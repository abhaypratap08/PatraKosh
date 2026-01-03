package com.patrakosh.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Configuration properties for storage services
 */
@ConfigurationProperties(prefix = "storage")
@Validated
public class StorageProperties {

    @NotBlank
    private String type = "local";

    @NotNull
    private LocalStorageProperties local = new LocalStorageProperties();

    @NotNull
    private S3StorageProperties s3 = new S3StorageProperties();

    @NotNull
    private MinIOStorageProperties minio = new MinIOStorageProperties();

    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalStorageProperties getLocal() { return local; }
    public void setLocal(LocalStorageProperties local) { this.local = local; }

    public S3StorageProperties getS3() { return s3; }
    public void setS3(S3StorageProperties s3) { this.s3 = s3; }

    public MinIOStorageProperties getMinio() { return minio; }
    public void setMinio(MinIOStorageProperties minio) { this.minio = minio; }
}

/**
 * Local storage configuration
 */
@ConfigurationProperties(prefix = "storage.local")
class LocalStorageProperties {
    private String basePath = "./storage";
    private long maxFileSize = 100 * 1024 * 1024; // 100MB
    private boolean enableCleanup = false;
    private int cleanupDaysOld = 30;

    // Getters and setters
    public String getBasePath() { return basePath; }
    public void setBasePath(String basePath) { this.basePath = basePath; }

    public long getMaxFileSize() { return maxFileSize; }
    public void setMaxFileSize(long maxFileSize) { this.maxFileSize = maxFileSize; }

    public boolean isEnableCleanup() { return enableCleanup; }
    public void setEnableCleanup(boolean enableCleanup) { this.enableCleanup = enableCleanup; }

    public int getCleanupDaysOld() { return cleanupDaysOld; }
    public void setCleanupDaysOld(int cleanupDaysOld) { this.cleanupDaysOld = cleanupDaysOld; }
}

/**
 * S3 storage configuration
 */
@ConfigurationProperties(prefix = "storage.s3")
class S3StorageProperties {
    @NotBlank
    private String region = "us-east-1";

    @NotBlank
    private String bucket;

    private String accessKey;
    private String secretKey;
    private String endpoint;
    private boolean forcePathStyle = false;
    private int presignedUrlExpiration = 3600;
    private long multipartThreshold = 5 * 1024 * 1024; // 5MB
    private int maxConnections = 50;
    private boolean enableVersioning = false;
    private String storageClass = "STANDARD";

    // Getters and setters
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }

    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public boolean isForcePathStyle() { return forcePathStyle; }
    public void setForcePathStyle(boolean forcePathStyle) { this.forcePathStyle = forcePathStyle; }

    public int getPresignedUrlExpiration() { return presignedUrlExpiration; }
    public void setPresignedUrlExpiration(int presignedUrlExpiration) { this.presignedUrlExpiration = presignedUrlExpiration; }

    public long getMultipartThreshold() { return multipartThreshold; }
    public void setMultipartThreshold(long multipartThreshold) { this.multipartThreshold = multipartThreshold; }

    public int getMaxConnections() { return maxConnections; }
    public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }

    public boolean isEnableVersioning() { return enableVersioning; }
    public void setEnableVersioning(boolean enableVersioning) { this.enableVersioning = enableVersioning; }

    public String getStorageClass() { return storageClass; }
    public void setStorageClass(String storageClass) { this.storageClass = storageClass; }
}

/**
 * MinIO storage configuration
 */
@ConfigurationProperties(prefix = "storage.minio")
class MinIOStorageProperties {
    @NotBlank
    private String endpoint;

    @NotBlank
    private String bucket;

    @NotBlank
    private String accessKey;

    @NotBlank
    private String secretKey;

    private String region = "us-east-1";
    private boolean secure = true;
    private int presignedUrlExpiration = 3600;
    private long multipartThreshold = 5 * 1024 * 1024; // 5MB
    private boolean enableVersioning = false;

    // Getters and setters
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }

    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public boolean isSecure() { return secure; }
    public void setSecure(boolean secure) { this.secure = secure; }

    public int getPresignedUrlExpiration() { return presignedUrlExpiration; }
    public void setPresignedUrlExpiration(int presignedUrlExpiration) { this.presignedUrlExpiration = presignedUrlExpiration; }

    public long getMultipartThreshold() { return multipartThreshold; }
    public void setMultipartThreshold(long multipartThreshold) { this.multipartThreshold = multipartThreshold; }

    public boolean isEnableVersioning() { return enableVersioning; }
    public void setEnableVersioning(boolean enableVersioning) { this.enableVersioning = enableVersioning; }
}
