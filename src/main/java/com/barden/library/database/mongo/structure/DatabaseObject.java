package com.barden.library.database.mongo.structure;

import javax.annotation.Nonnull;

/**
 * Database object.
 *
 * @param <P> Parent.
 * @param <F> Database field.
 */
public interface DatabaseObject<P, F extends DatabaseField<P>> {

    /**
     * Gets database structure.
     *
     * @return {@link DatabaseStructure}
     */
    @Nonnull
    DatabaseStructure<P, F> getDatabase();
}
