package com.patrakosh.core;

import java.time.LocalDateTime;

/**
 * Interface for entities that track creation and modification timestamps.
 * Provides audit trail capabilities for domain models.
 */
public interface Auditable {
    /**
     * Gets the timestamp when the entity was created.
     *
     * @return the creation timestamp
     */
    LocalDateTime getCreatedAt();

    /**
     * Sets the timestamp when the entity was created.
     *
     * @param createdAt the creation timestamp
     */
    void setCreatedAt(LocalDateTime createdAt);

    /**
     * Gets the timestamp when the entity was last updated.
     *
     * @return the last update timestamp
     */
    LocalDateTime getUpdatedAt();

    /**
     * Sets the timestamp when the entity was last updated.
     *
     * @param updatedAt the last update timestamp
     */
    void setUpdatedAt(LocalDateTime updatedAt);
}
