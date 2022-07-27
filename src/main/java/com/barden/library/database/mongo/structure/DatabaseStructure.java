package com.barden.library.database.mongo.structure;

import com.barden.library.scheduler.SchedulerProvider;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Database structure class to handle database methods.
 *
 * @param <P> Parent object.
 * @param <F> Database field enum.
 */
@SuppressWarnings({"unchecked", "unused"})
public abstract class DatabaseStructure<P, F extends DatabaseField<P>> {

    protected final P parent;
    protected final Class<F> field;
    private final DatabaseMongoProvider mongoProvider;

    /**
     * Creates a database structure with given parent and field.
     *
     * @param parent        {@link P}
     * @param field         {@link F} Database field enum.
     * @param mongoProvider {@link DatabaseMongoProvider}
     * @throws NullPointerException If {@param parent} or {@param field} or {@param mongoProvider} is null.
     */
    public DatabaseStructure(@NotNull P parent, @NotNull Class<F> field, @NotNull DatabaseMongoProvider mongoProvider) {
        this.parent = Objects.requireNonNull(parent);
        this.field = Objects.requireNonNull(field);
        this.mongoProvider = Objects.requireNonNull(mongoProvider);
    }

    /**
     * Gets parent.
     *
     * @return {@link P}
     */
    @NotNull
    public final P getParent() {
        return this.parent;
    }

    /**
     * Gets field class.
     *
     * @return {@link Class} of {@link F}
     */
    @NotNull
    public final Class<F> getField() {
        return this.field;
    }

    /**
     * Gets mongo provider.
     *
     * @return {@link DatabaseMongoProvider}
     */
    @NotNull
    public DatabaseMongoProvider getMongoProvider() {
        return this.mongoProvider;
    }


    /*
    CALLS
     */

    /**
     * Saves database structure to the database.
     *
     * @param fields {@link F} fields to save.
     * @throws NullPointerException If {@param fields} is null.
     */
    public void save(@NotNull F... fields) {
        //Object null checks.
        Objects.requireNonNull(fields, "Tried to save database(" + this.mongoProvider.getDatabaseId() + ") structure(" + this.parent + ") without fields.");
        this.mongoProvider.getCollection().updateOne(this.toQueryBson(), this.toSaveBson(fields), new UpdateOptions().upsert(true));
    }

    /**
     * Saves database structure to the database.
     *
     * @param fields {@link F} fields to save.
     * @throws NullPointerException If {@param fields} is null.
     */
    public void save(@NotNull Collection<F> fields) {
        //Object null checks.
        Objects.requireNonNull(fields, "Tried to save database(" + this.mongoProvider.getDatabaseId() + ") structure(" + this.parent + ") without fields.");
        this.save((F[]) fields.toArray(DatabaseField[]::new));
    }

    /**
     * Saves database structure to the database with all fields.
     */
    public void save() {
        this.save(this.field.getEnumConstants());
    }

    /**
     * Saves database structure to the database async.
     *
     * @param fields {@link F} fields to save.
     */
    public void saveAsync(@NotNull F... fields) {
        SchedulerProvider.schedule(task -> this.save(fields));
    }

    /**
     * Saves database structure to the database. (ASYNC)
     *
     * @param fields {@link F} fields to save.
     */
    public void saveAsync(@NotNull Collection<F> fields) {
        //Object null checks.
        Objects.requireNonNull(fields, "Tried to save database(" + this.mongoProvider.getDatabaseId() + ") structure(" + this.parent + ") to database without fields.");
        this.saveAsync((F[]) fields.toArray(DatabaseField[]::new));
    }

    /**
     * Saves database structure to the database with all fields.  (ASYNC)
     */
    public final void saveAsync() {
        this.saveAsync(this.field.getEnumConstants());
    }

    /**
     * Deletes database structure from the database.
     */
    public final void delete() {
        this.mongoProvider.getCollection().deleteOne(this.toQueryBson());
    }

    /**
     * Deletes database structure from the database. (ASYNC)
     */
    public final void deleteAsync() {
        SchedulerProvider.schedule(task -> this.delete());
    }


    /*
    CONVERTERS
     */

    /**
     * Gets query field.
     *
     * @return {@link Bson}
     */
    @NotNull
    public final Bson toQueryBson() {
        for (F f : this.field.getEnumConstants())
            if (f.isQuery()) return new BsonDocument(f.getPath(), f.toBsonValue(this.parent));
        return new BsonDocument();
    }

    /**
     * Gets parent as a save bson.
     * With save bson, we can update mongo document.
     *
     * @return {@link Bson}
     */
    @NotNull
    public final Bson toSaveBson() {
        return this.toSaveBson(this.field.getEnumConstants());
    }

    /**
     * Gets save bson with target fields.
     *
     * @param fields {@link F} fields to save.
     * @return {@link Bson}
     * @throws NullPointerException If {@param fields} is null.
     */
    @SafeVarargs
    @NotNull
    public final Bson toSaveBson(@NotNull F... fields) {
        //Object null checks.
        Objects.requireNonNull(fields, "Database(" + this.mongoProvider.getDatabaseId() + ") structure(" + this.parent + ") fields cannot be null!");
        List<Bson> list = new ArrayList<>();
        for (F field : fields)
            list.add(Updates.set(field.getPath(), this.toBsonValue(field)));
        return Updates.combine(list);
    }

    /**
     * Gets bson value from field.
     *
     * @param field {@link F} field to get value from.
     * @return {@link Bson}
     */
    @NotNull
    public final BsonValue toBsonValue(@NotNull F field) {
        return field.toBsonValue(this.parent);
    }
}
