package com.patrakosh.thread;

import com.patrakosh.model.FileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.*;

/**
 * Manages thread pools for asynchronous file operations.
 * Demonstrates proper use of ExecutorService and CompletableFuture for multithreading.
 */
public class ThreadPoolManager {
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolManager.class);
    private static ThreadPoolManager instance;
    
    // Thread pool for file upload operations (5 threads)
    private final ExecutorService uploadExecutor;
    
    // Thread pool for file download operations (10 threads)
    private final ExecutorService downloadExecutor;
    
    // Scheduled executor for periodic tasks (2 threads)
    private final ScheduledExecutorService scheduledExecutor;
    
    // Thread pool configuration
    private static final int UPLOAD_POOL_SIZE = 5;
    private static final int DOWNLOAD_POOL_SIZE = 10;
    private static final int SCHEDULED_POOL_SIZE = 2;
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 60;
    
    private ThreadPoolManager() {
        // Create upload thread pool with custom thread factory
        this.uploadExecutor = Executors.newFixedThreadPool(
            UPLOAD_POOL_SIZE,
            new CustomThreadFactory("Upload-Worker")
        );
        
        // Create download thread pool with custom thread factory
        this.downloadExecutor = Executors.newFixedThreadPool(
            DOWNLOAD_POOL_SIZE,
            new CustomThreadFactory("Download-Worker")
        );
        
        // Create scheduled executor for periodic tasks
        this.scheduledExecutor = Executors.newScheduledThreadPool(
            SCHEDULED_POOL_SIZE,
            new CustomThreadFactory("Scheduled-Worker")
        );
        
        logger.info("ThreadPoolManager initialized - Upload: {} threads, Download: {} threads, Scheduled: {} threads",
                UPLOAD_POOL_SIZE, DOWNLOAD_POOL_SIZE, SCHEDULED_POOL_SIZE);
    }
    
    /**
     * Gets the singleton instance of ThreadPoolManager.
     *
     * @return the ThreadPoolManager instance
     */
    public static synchronized ThreadPoolManager getInstance() {
        if (instance == null) {
            instance = new ThreadPoolManager();
        }
        return instance;
    }
    
    /**
     * Submits a file upload task for asynchronous execution.
     *
     * @param task the upload task
     * @return CompletableFuture with the result
     */
    public CompletableFuture<FileItem> submitUpload(Callable<FileItem> task) {
        logger.debug("Submitting upload task to thread pool");
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                logger.error("Upload task failed", e);
                throw new CompletionException(e);
            }
        }, uploadExecutor);
    }
    
    /**
     * Submits a file download task for asynchronous execution.
     *
     * @param task the download task
     * @return CompletableFuture with the result
     */
    public CompletableFuture<File> submitDownload(Callable<File> task) {
        logger.debug("Submitting download task to thread pool");
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                logger.error("Download task failed", e);
                throw new CompletionException(e);
            }
        }, downloadExecutor);
    }
    
    /**
     * Submits a generic task for asynchronous execution on upload pool.
     *
     * @param task the task to execute
     * @param <T> the result type
     * @return CompletableFuture with the result
     */
    public <T> CompletableFuture<T> submitTask(Callable<T> task) {
        logger.debug("Submitting generic task to upload thread pool");
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                logger.error("Task execution failed", e);
                throw new CompletionException(e);
            }
        }, uploadExecutor);
    }
    
    /**
     * Schedules a task to run periodically.
     *
     * @param task the task to run
     * @param initialDelay initial delay before first execution
     * @param period period between executions
     * @param unit time unit
     * @return ScheduledFuture representing the scheduled task
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        logger.debug("Scheduling periodic task with initial delay: {} {}, period: {} {}",
                initialDelay, unit, period, unit);
        
        return scheduledExecutor.scheduleAtFixedRate(task, initialDelay, period, unit);
    }
    
    /**
     * Schedules a task to run once after a delay.
     *
     * @param task the task to run
     * @param delay delay before execution
     * @param unit time unit
     * @return ScheduledFuture representing the scheduled task
     */
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        logger.debug("Scheduling one-time task with delay: {} {}", delay, unit);
        
        return scheduledExecutor.schedule(task, delay, unit);
    }
    
    /**
     * Gets the number of active upload threads.
     *
     * @return active thread count
     */
    public int getActiveUploadThreads() {
        if (uploadExecutor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) uploadExecutor).getActiveCount();
        }
        return 0;
    }
    
    /**
     * Gets the number of active download threads.
     *
     * @return active thread count
     */
    public int getActiveDownloadThreads() {
        if (downloadExecutor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) downloadExecutor).getActiveCount();
        }
        return 0;
    }
    
    /**
     * Gets the number of queued upload tasks.
     *
     * @return queued task count
     */
    public int getQueuedUploadTasks() {
        if (uploadExecutor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) uploadExecutor).getQueue().size();
        }
        return 0;
    }
    
    /**
     * Gets the number of queued download tasks.
     *
     * @return queued task count
     */
    public int getQueuedDownloadTasks() {
        if (downloadExecutor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) downloadExecutor).getQueue().size();
        }
        return 0;
    }
    
    /**
     * Gets thread pool statistics as a formatted string.
     *
     * @return statistics string
     */
    public String getStatistics() {
        return String.format(
            "ThreadPool Stats - Upload: %d active/%d queued, Download: %d active/%d queued",
            getActiveUploadThreads(), getQueuedUploadTasks(),
            getActiveDownloadThreads(), getQueuedDownloadTasks()
        );
    }
    
    /**
     * Initiates an orderly shutdown of all thread pools.
     * Previously submitted tasks are executed, but no new tasks will be accepted.
     */
    public void shutdown() {
        logger.info("Initiating thread pool shutdown");
        
        uploadExecutor.shutdown();
        downloadExecutor.shutdown();
        scheduledExecutor.shutdown();
        
        try {
            // Wait for tasks to complete
            if (!uploadExecutor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                logger.warn("Upload executor did not terminate in time, forcing shutdown");
                uploadExecutor.shutdownNow();
            }
            
            if (!downloadExecutor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                logger.warn("Download executor did not terminate in time, forcing shutdown");
                downloadExecutor.shutdownNow();
            }
            
            if (!scheduledExecutor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                logger.warn("Scheduled executor did not terminate in time, forcing shutdown");
                scheduledExecutor.shutdownNow();
            }
            
            logger.info("Thread pool shutdown completed");
            
        } catch (InterruptedException e) {
            logger.error("Thread pool shutdown interrupted", e);
            uploadExecutor.shutdownNow();
            downloadExecutor.shutdownNow();
            scheduledExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Attempts to stop all actively executing tasks and halts processing of waiting tasks.
     */
    public void shutdownNow() {
        logger.warn("Forcing immediate thread pool shutdown");
        
        uploadExecutor.shutdownNow();
        downloadExecutor.shutdownNow();
        scheduledExecutor.shutdownNow();
    }
    
    /**
     * Custom thread factory for creating named threads.
     */
    private static class CustomThreadFactory implements ThreadFactory {
        private final String namePrefix;
        private int threadCount = 0;
        
        public CustomThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }
        
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, namePrefix + "-" + threadCount++);
            thread.setDaemon(false);
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        }
    }
}
