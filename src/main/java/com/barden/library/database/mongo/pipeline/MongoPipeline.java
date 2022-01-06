package com.barden.library.database.mongo.pipeline;

import org.bson.Document;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Mongo pipeline.
 */
public final class MongoPipeline {

    /**
     * Index object.
     */
    public final record Index(@Nonnull MongoPipelineExecutor executor, @Nonnull String path) {
    }

    private final UUID uid;

    private final String database;
    private final String collection;

    private final String key;
    private final Object value;

    private final Document document = new Document();
    private final HashMap<Object, String> paths = new HashMap<>();
    private final HashMap<Object, Consumer<Index>> consumers = new HashMap<>();

    /**
     * Creates Mongo pipeline object.
     *
     * @param database   Database.
     * @param collection Collection.
     * @param key        Key.
     * @param value      Value.
     */
    public MongoPipeline(@Nonnull String database, @Nonnull String collection, @Nonnull String key, @Nonnull Object value) {
        this.uid = UUID.randomUUID();
        this.database = Objects.requireNonNull(database, "database cannot be null!");
        this.collection = Objects.requireNonNull(collection, "collection cannot be null!");
        this.key = Objects.requireNonNull(key, "key cannot be null!");
        this.value = Objects.requireNonNull(value, "value cannot be null!");
    }

    /**
     * Gets UID.
     *
     * @return UID.
     */
    @Nonnull
    public UUID getUID() {
        return this.uid;
    }

    /**
     * Gets database.
     *
     * @return Database.
     */
    @Nonnull
    public String getDatabase() {
        return this.database;
    }

    /**
     * Gets collection.
     *
     * @return Collection.
     */
    @Nonnull
    public String getCollection() {
        return this.collection;
    }

    /**
     * Gets key.
     *
     * @return Key.
     */
    @Nonnull
    public String getKey() {
        return this.key;
    }

    /**
     * Gets value.
     *
     * @return Value.
     */
    @Nonnull
    public Object getValue() {
        return this.value;
    }

    /**
     * Gets document.
     *
     * @return Document.
     */
    @Nonnull
    public Document getDocument() {
        return this.document;
    }

    /**
     * Gets paths.
     *
     * @return Paths.
     */
    @Nonnull
    public HashMap<Object, String> getPaths() {
        return this.paths;
    }

    /**
     * Gets path.
     *
     * @param key Key.
     * @return Path value.
     */
    @Nonnull
    public String getPath(@Nonnull Object key) {
        return this.paths.get(Objects.requireNonNull(key, "key cannot be null!"));
    }

    /**
     * Gets path.
     *
     * @param key   Key.
     * @param value Path value.
     * @return Mongo pipeline builder.
     */
    @Nonnull
    public MongoPipeline setPath(@Nonnull Object key, @Nonnull String value) {
        this.paths.putIfAbsent(Objects.requireNonNull(key, "key cannot be null!"), Objects.requireNonNull(value, "value cannot be null!"));
        return this;
    }

    /**
     * Gets consumers.
     *
     * @return Consumers.
     */
    @Nonnull
    public HashMap<Object, Consumer<Index>> getConsumers() {
        return this.consumers;
    }

    /**
     * Gets consumer.
     *
     * @param key Key.
     * @return Consumer.
     */
    @Nonnull
    public Consumer<Index> getConsumer(@Nonnull Object key) {
        return this.consumers.get(Objects.requireNonNull(key, "key cannot be null!"));
    }

    /**
     * Sets consumer.
     *
     * @param key      Key.
     * @param consumer Consumer.
     * @return Mongo pipeline builder.
     */
    @Nonnull
    public MongoPipeline setConsumer(@Nonnull Object key, @Nonnull Consumer<Index> consumer) {
        this.consumers.putIfAbsent(key, consumer);
        return this;
    }

    /**
     * Configure pipeline index.
     *
     * @param key      Key.
     * @param path     Path.
     * @param consumer Consumer.
     * @return Mongo pipeline builder.
     */
    @Nonnull
    public MongoPipeline configure(@Nonnull Object key, String path, @Nonnull Consumer<Index> consumer) {
        this.setPath(Objects.requireNonNull(key, "key cannot be null!"), Objects.requireNonNull(path, "path cannot be null!"));
        this.setConsumer(key, Objects.requireNonNull(consumer, "consumer cannot be null!"));
        return this;
    }

    /**
     * Creates new pipeline executor object.
     *
     * @return Mongo Pipeline Executor.
     */
    @Nonnull
    public MongoPipelineExecutor createExecutor() {
        return new MongoPipelineExecutor(this);
    }
}
