package com.barden.library.metadata;

import javax.annotation.Nonnull;

/**
 * Metadata entity class.
 */
public abstract class MetadataEntity {

    protected final Metadata metadata = new Metadata();

    /**
     * Gets metadata.
     *
     * @return Metadata.
     */
    @Nonnull
    public final Metadata metadata() {
        return this.metadata;
    }
}
