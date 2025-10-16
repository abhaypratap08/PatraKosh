package com.patrakosh.core;

/**
 * Interface for entities that have an identifier.
 * Provides type-safe ID management for domain models.
 *
 * @param <ID> the type of the identifier
 */
public interface Identifiable<ID> {
    /**
     * Gets the identifier of the entity.
     *
     * @return the entity identifier
     */
    ID getId();

    /**
     * Sets the identifier of the entity.
     *
     * @param id the entity identifier
     */
    void setId(ID id);
}
