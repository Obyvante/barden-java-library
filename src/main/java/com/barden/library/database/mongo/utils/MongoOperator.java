package com.barden.library.database.mongo.utils;

import org.bson.Document;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Mongo document mapper.
 */
public final class MongoOperator {

    /**
     * Sets key and value then return as a document.
     *
     * @param key   Key.
     * @param value Value.
     * @return Document.
     */
    @Nonnull
    public static Document set(@Nonnull String key, @Nonnull Object value) {
        //Objects null check.
        Objects.requireNonNull(key, "key cannot be null!");
        Objects.requireNonNull(value, "value cannot be null!");

        return new Document("$set", new Document(key, value));
    }

    /**
     * Sets key and value then return as a document.
     *
     * @param document Document.
     * @param key      Key.
     * @param value    Value.
     * @return Document.
     */
    @Nonnull
    public static Document set(@Nonnull Document document, @Nonnull String key, @Nonnull Object value) {
        //Objects null check.
        Objects.requireNonNull(document, "document cannot be null!");
        Objects.requireNonNull(key, "key cannot be null!");
        Objects.requireNonNull(value, "value cannot be null!");

        if (document.containsKey("$set"))
            return document.get("$set", Document.class).append(key, value);
        return document.append("$set", new Document(key, value));
    }

    /**
     * Unsets key and value then return as a document.
     *
     * @param key Key.
     * @return Document.
     */
    @Nonnull
    public static Document unset(@Nonnull String key) {
        //Objects null check.
        Objects.requireNonNull(key, "key cannot be null!");

        return new Document("$unset", new Document(key, ""));
    }

    /**
     * Unsets key and value then return as a document.
     *
     * @param document Document.
     * @param key      Key.
     * @return Document.
     */
    @Nonnull
    public static Document unset(@Nonnull Document document, @Nonnull String key) {
        //Objects null check.
        Objects.requireNonNull(document, "document cannot be null!");
        Objects.requireNonNull(key, "key cannot be null!");

        if (document.containsKey("$unset"))
            return document.get("$unset", Document.class).append(key, "");
        return document.append("$unset", new Document(key, ""));
    }

}
