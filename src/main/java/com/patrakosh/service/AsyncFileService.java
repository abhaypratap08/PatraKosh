package com.patrakosh.service;

import com.patrakosh.exception.StorageException;
import com.patrakosh.listener.FileOperationListener;
import com.patrakosh.model.FileItem;
import com.patrakosh.thread.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous file service using ThreadPoolManager.
 * Demonstrates multithreading with progress callbacks.
 */
public class AsyncFileService {
    private static final Logger logger = LoggerFactory.getLogger(AsyncFileService.class);
    private final FileService fileService;
    private final ThreadPoolManager threadPool;
    
    public AsyncFileService() {
        this.fileService = new FileService();
        this.threadPool = ThreadPoolManager.getInstance();
    }
    
    /**
     * Uploads a file asynchronously with progress callbacks.
     *
     * @param userId the user ID
     * @param file the file to upload
     * @param listener the progress listener
     * @return CompletableFuture with the uploaded file
     */
    public CompletableFuture<FileItem> uploadFileAsync(Integer userId, File file, FileOperationListener listener) {
        logger.info("Starting async upload for file: {}", file.getName());
        
        return threadPool.submitUpload(() -> {
            try {
                // Notify start
                if (listener != null) {
                    listener.onProgress(0);
                }
                
                // Simulate progress during upload
                if (listener != null) {
                    listener.onProgress(25);
                }
                
                // Perform actual upload
                FileItem result = fileService.uploadFile(userId, file);
                
                // Notify progress
                if (listener != null) {
                    listener.onProgress(75);
                }
                
                // Complete
                if (listener != null) {
                    listener.onProgress(100);
                    listener.onComplete(result);
                }
                
                logger.info("Async upload completed for file: {}", file.getName());
                return result;
                
            } catch (Exception e) {
                logger.error("Async upload failed for file: {}", file.getName(), e);
                if (listener != null) {
                    listener.onError(e);
                }
                throw e;
            }
        });
    }
    
    /**
     * Downloads a file asynchronously.
     *
     * @param fileItem the file to download
     * @param destination the destination file
     * @param listener the progress listener
     * @return CompletableFuture with the downloaded file
     */
    public CompletableFuture<File> downloadFileAsync(FileItem fileItem, File destination, FileOperationListener listener) {
        logger.info("Starting async download for file: {}", fileItem.getFilename());
        
        return threadPool.submitDownload(() -> {
            try {
                // Notify start
                if (listener != null) {
                    listener.onProgress(0);
                }
                
                // Simulate progress
                if (listener != null) {
                    listener.onProgress(30);
                }
                
                // Perform actual download
                fileService.downloadFile(fileItem, destination);
                
                // Notify progress
                if (listener != null) {
                    listener.onProgress(80);
                }
                
                // Complete
                if (listener != null) {
                    listener.onProgress(100);
                    listener.onComplete(fileItem);
                }
                
                logger.info("Async download completed for file: {}", fileItem.getFilename());
                return destination;
                
            } catch (Exception e) {
                logger.error("Async download failed for file: {}", fileItem.getFilename(), e);
                if (listener != null) {
                    listener.onError(e);
                }
                throw e;
            }
        });
    }
    
    /**
     * Uploads multiple files concurrently.
     *
     * @param userId the user ID
     * @param files the files to upload
     * @return CompletableFuture with all results
     */
    public CompletableFuture<Void> uploadMultipleFiles(Integer userId, File[] files) {
        logger.info("Starting concurrent upload of {} files", files.length);
        
        CompletableFuture<?>[] futures = new CompletableFuture[files.length];
        
        for (int i = 0; i < files.length; i++) {
            final File file = files[i];
            futures[i] = uploadFileAsync(userId, file, null);
        }
        
        return CompletableFuture.allOf(futures);
    }
}
