package com.barden.library.database.mongo.structure;

import com.barden.library.BardenJavaLibrary;
import com.barden.library.database.DatabaseProvider;
import com.barden.library.scheduler.SchedulerProvider;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Database mongo provider class to provider database methods.
 */
@SuppressWarnings("unused")
public abstract class DatabaseMongoProvider {

    private final String databaseId;
    private final String collectionId;

    /**
     * Creates new database mongo provider.
     *
     * @param databaseId   {@link String} database id.
     * @param collectionId {@link String} collection id.
     * @throws NullPointerException If {@param databaseId} or {@param collectionId} is null.
     */
    public DatabaseMongoProvider(@NotNull String databaseId, @NotNull String collectionId) {
        this.databaseId = Objects.requireNonNull(databaseId);
        this.collectionId = Objects.requireNonNull(collectionId);
    }

    /**
     * Gets database id.
     *
     * @return {@link String}
     */
    @NotNull
    public String getDatabaseId() {
        return this.databaseId;
    }

    /**
     * Gets collection id.
     *
     * @return {@link String}
     */
    @NotNull
    public String getCollectionId() {
        return this.collectionId;
    }

    /**
     * Adds index.
     *
     * @param index   {@link Bson}
     * @param options {@link IndexOptions}
     */
    public final void addIndex(@NotNull Bson index, @NotNull IndexOptions options) {
        DatabaseProvider.mongo().createIndex(this.databaseId, this.collectionId, index, options);
    }

    /**
     * Gets mongo collection.
     *
     * @return {@link MongoCollection}
     * @throws NullPointerException If {@param databaseId} or {@param collectionId} is null.
     */
    @NotNull
    public final MongoCollection<BsonDocument> getCollection() {
        return Objects.requireNonNull(DatabaseProvider.mongo().getCollection(this.databaseId, this.collectionId, BsonDocument.class), "database(" + this.databaseId + ") collection cannot be null!");
    }

    /**
     * Saves database structures to the database.
     *
     * @param structures {@link Set} with extends {@link DatabaseObject}
     * @throws NullPointerException If {@param structures} is null.
     */
    public final void save(@NotNull Set<? extends DatabaseObject<?, ?>> structures) {
        //Object null checks.
        Objects.requireNonNull(structures, "Tried to save null database(" + this.databaseId + ") structure list to the database.");
        if (structures.size() == 0)
            return;

        try {
            MongoCollection<BsonDocument> collection = this.getCollection();

            //If there is only one database structure, no need to use bulk since it impacts performance.
            if (structures.size() == 1) {
                structures.iterator().next().getDatabase().save();
                return;
            }

            List<WriteModel<BsonDocument>> writes = new ArrayList<>();
            //Loops through database structures, converts "save module" then adds to the created write models list.
            structures.forEach(structure -> writes.add(new UpdateOneModel<>(structure.getDatabase().toQueryBson(), structure.getDatabase().toSaveBson())));

            //Pass write modules to collection. (UPDATES MONGO BSON DOCUMENTS AND COLLECTION) -> NOT ASYNC!
            collection.bulkWrite(writes, new BulkWriteOptions().bypassDocumentValidation(true));
        } catch (Exception exception) {
            BardenJavaLibrary.getLogger().error("Couldn't save database(" + this.databaseId + ") structures to the database!", exception);
        }
    }

    /**
     * Saves database structures to the database. (ASYNC)
     *
     * @param structures {@link Set} with extends {@link DatabaseObject}
     */
    public final void saveAsync(@NotNull Set<? extends DatabaseObject<?, ?>> structures) {
        SchedulerProvider.schedule(task -> this.save(structures));
    }
}
