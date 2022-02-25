package com.barden.library.metadata;

import com.barden.library.scheduler.SchedulerRepository;
import com.barden.library.scheduler.task.Task;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Metadata object.
 */
@SuppressWarnings("unchecked")
public final class Metadata {
    private final HashMap<Object, Object> metadata = new HashMap<>();

    /**
     * Gets if metadata registered or not.
     *
     * @param key Key.
     * @return If metadata registered or not.
     */
    public boolean has(@Nonnull Object key) {
        return this.metadata.get(Objects.requireNonNull(key, "key cannot be null!")) != null;
    }

    /**
     * Gets metadata.
     *
     * @param <T> Value type.
     * @param key Key.
     * @return Value.
     */
    @Nullable
    public <T> T get(@Nonnull Object key) {
        return (T) this.get(key, Object.class);
    }

    /**
     * Gets metadata.
     *
     * @param <T>        Value type.
     * @param key        Key.
     * @param valueClass Value class.
     * @return Value.
     */
    @Nullable
    public <T> T get(@Nonnull Object key, @Nonnull Class<T> valueClass) {
        Objects.requireNonNull(valueClass, "value class cannot be null!");
        return (T) this.metadata.get(Objects.requireNonNull(key, "key cannot be null!"));
    }

    /**
     * Gets metadata nonnull.
     *
     * @param <T> Value type.
     * @param key Key.
     * @return Value.
     */
    @Nonnull
    public <T> T getNonNull(@Nonnull Object key) {
        return (T) this.getNonNull(key, Object.class);
    }

    /**
     * Gets metadata nonnull.
     *
     * @param <T>        Value type.
     * @param key        Key.
     * @param valueClass Value class.
     * @return Value.
     */
    @Nonnull
    public <T> T getNonNull(@Nonnull Object key, @Nonnull Class<T> valueClass) {
        Objects.requireNonNull(valueClass, "value class cannot be null!");
        return (T) this.metadata.get(Objects.requireNonNull(key, "key cannot be null!"));
    }

    /**
     * Gets metadata.
     *
     * @param <T>          Value type.
     * @param key          Key.
     * @param defaultValue Default value.
     * @return Value.
     */
    @Nonnull
    public <T> T get(@Nonnull Object key, T defaultValue) {
        return (T) this.metadata.getOrDefault(Objects.requireNonNull(key, "key cannot be null!"),
                Objects.requireNonNull(defaultValue, "default value cannot be null!"));
    }

    /**
     * Sets metadata.
     *
     * @param key   Key.
     * @param value Value.
     * @return Metadata.
     */
    @Nonnull
    public Metadata set(@Nonnull Object key, @Nonnull Object value) {
        //Objects null check.
        Objects.requireNonNull(key, "key cannot be null!");
        Objects.requireNonNull(value, "value cannot be null!");

        //Checks expire.
        this.checkExpire(key);

        //Sets metadata.
        this.metadata.put(key, value);
        return this;
    }

    /**
     * Sets metadata.
     *
     * @param key      Key.
     * @param value    Value.
     * @param unit     Time unit.
     * @param duration Duration.
     * @return Metadata.
     */
    @Nonnull
    public Metadata set(@Nonnull Object key, @Nonnull Object value, @Nonnull TimeUnit unit, int duration) {
        return this.set(key, value, unit, duration, null);
    }

    /**
     * Sets metadata.
     *
     * @param key           Key.
     * @param value         Value.
     * @param unit          Time unit.
     * @param duration      Duration.
     * @param expireHandler Expire handler. (Optional)
     * @return Metadata.
     */
    @Nonnull
    public Metadata set(@Nonnull Object key, @Nonnull Object value, @Nonnull TimeUnit unit, int duration, @Nullable Consumer<Metadata> expireHandler) {
        //Objects null check.
        Objects.requireNonNull(key, "key cannot be null!");
        Objects.requireNonNull(value, "value cannot be null!");
        Objects.requireNonNull(unit, "unit cannot be null!");

        //Checks expire.
        this.checkExpire(key);

        //Sets metadata.
        this.metadata.put(key, value);
        this.metadata.put(key + ":expire", SchedulerRepository.create().after(duration, unit).schedule(task -> {
            this.metadata.remove(key);
            this.metadata.remove(key + ":expire");
            if (expireHandler != null)
                expireHandler.accept(this);
        }));
        return this;
    }

    /**
     * Adds metadata key.
     *
     * @param key Key.
     * @return Metadata.
     */
    @Nonnull
    public Metadata add(@Nonnull Object key) {
        //Objects null check.
        Objects.requireNonNull(key, "key cannot be null!");

        //Checks expire.
        this.checkExpire(key);

        //Adds metadata.
        this.metadata.put(key, 0);
        return this;
    }

    /**
     * Adds metadata.
     *
     * @param key      Key.
     * @param unit     Time unit.
     * @param duration Duration.
     * @return Metadata.
     */
    @Nonnull
    public Metadata add(@Nonnull Object key, @Nonnull TimeUnit unit, int duration) {
        return this.add(key, unit, duration, null);
    }

    /**
     * Adds metadata.
     *
     * @param key           Key.
     * @param unit          Time unit.
     * @param duration      Duration.
     * @param expireHandler Expire handler. (Optional)
     * @return Metadata.
     */
    @Nonnull
    public Metadata add(@Nonnull Object key, @Nonnull TimeUnit unit, int duration, @Nullable Consumer<Metadata> expireHandler) {
        return this.set(key, 0, unit, duration, expireHandler);
    }

    /**
     * Removes metadata.
     *
     * @param key Key.
     * @return Metadata.
     */
    @Nonnull
    public Metadata remove(@Nonnull Object key) {
        //Objects null check.
        Objects.requireNonNull(key, "key cannot be null!");
        //Checks expire.
        if (!this.checkExpire(key))
            this.metadata.remove(key);
        return this;
    }


    /*
    MISC
     */

    /**
     * Checks expire.
     *
     * @param key Key.
     */
    private boolean checkExpire(@Nonnull Object key) {
        //Objects null check.
        Objects.requireNonNull(key, "key cannot be null!");

        //If key expire is not set, no need to continue.
        if (this.get(key + ":expire") == null)
            return false;

        //Cancels task.
        this.getNonNull(key + ":expire", Task.class).cancel();

        //Removes metadata.
        this.metadata.remove(key);
        this.metadata.remove(key + ":expire");
        return true;
    }

    /**
     * Resets metadata.
     */
    public void reset() {
        //Expires metadata keys.
        this.metadata.keySet().stream()
                .filter(key -> key.getClass() == String.class && ((String) key).endsWith(":expire"))
                .forEach(key -> this.getNonNull(key, Task.class).cancel());

        //Clears metadata.
        this.metadata.clear();
    }
}
