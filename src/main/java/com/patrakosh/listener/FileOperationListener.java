package com.patrakosh.listener;

import com.patrakosh.model.FileItem;

/**
 * Listener interface for asynchronous file operations.
 * Provides callbacks for progress tracking and completion notification.
 */
public interface FileOperationListener {
    /**
     * Called when the operation makes progress.
     *
     * @param progress the progress percentage (0-100)
     */
    void onProgress(int progress);

    /**
     * Called when the operation completes successfully.
     *
     * @param fileItem the resulting file item
     */
    void onComplete(FileItem fileItem);

    /**
     * Called when the operation encounters an error.
     *
     * @param exception the exception that occurred
     */
    void onError(Exception exception);
}
