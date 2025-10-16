package com.patrakosh.service;

import com.patrakosh.exception.ValidationException;
import com.patrakosh.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Abstract base class for all service classes.
 * Provides common functionality like logging and transaction management.
 */
public abstract class BaseService {
    protected final Logger logger;
    protected final TransactionManager transactionManager;

    /**
     * Constructs a new BaseService with logger and transaction manager.
     */
    public BaseService() {
        this.logger = LoggerFactory.getLogger(getClass());
        this.transactionManager = TransactionManager.getInstance();
    }

    /**
     * Validates input data before processing.
     * Subclasses must implement this method to provide specific validation logic.
     *
     * @param input the input to validate
     * @throws ValidationException if validation fails
     */
    protected abstract void validateInput(Object input) throws ValidationException;

    /**
     * Logs an operation with its parameters.
     *
     * @param operation the operation name
     * @param params the operation parameters
     */
    protected void logOperation(String operation, Object... params) {
        logger.info("Operation: {} with params: {}", operation, Arrays.toString(params));
    }

    /**
     * Logs an error with context.
     *
     * @param operation the operation that failed
     * @param exception the exception that occurred
     */
    protected void logError(String operation, Exception exception) {
        logger.error("Error in operation: {}", operation, exception);
    }
}
