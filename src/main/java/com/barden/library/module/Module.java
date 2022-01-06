package com.barden.library.module;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Module library.
 */
@SuppressWarnings("unchecked")
public final class Module {

    /**
     * Module option.
     */
    public enum Option {
        NOT_OVERRIDE
    }

    private final ConcurrentHashMap<Object, Object> modules = new ConcurrentHashMap<>();

    /**
     * Gets modules.
     *
     * @param <T> Module type.
     * @return Modules.
     */
    public <T> Collection<T> get() {
        return (Collection<T>) modules.values();
    }

    /**
     * Gets modules.
     *
     * @param moduleType Module type class.
     * @param <T>        Module type.
     * @return Modules.
     */
    public <T> Collection<T> get(@Nonnull Class<T> moduleType) {
        Objects.requireNonNull(moduleType, "module type cannot be null!");
        return (Collection<T>) modules.values();
    }

    /**
     * Gets module.
     *
     * @param key Key.
     * @param <T> Value type.
     * @return Value.
     */
    public <T> T get(@Nonnull Object key) {
        return (T) this.modules.get(Objects.requireNonNull(key, "key cannot be null!"));
    }

    /**
     * Gets module.
     *
     * @param key  Key.
     * @param type Value type class.
     * @param <T>  Value type.
     * @return Value.
     */
    public <T> T get(@Nonnull Object key, @Nullable Class<T> type) {
        Objects.requireNonNull(type, "type cannot be null!");
        return (T) this.modules.get(Objects.requireNonNull(key, "key cannot be null!"));
    }

    /**
     * Sets module.
     *
     * @param <T>            Module type.
     * @param key            Key.
     * @param value          Value.
     * @param module_options Module options.
     */
    public <T> void set(@Nonnull Object key, @Nullable T value, @Nonnull Option... module_options) {
        //Objects null check.
        Objects.requireNonNull(key, "key cannot be null!");
        Objects.requireNonNull(value, "value cannot be null!");
        Objects.requireNonNull(module_options, "module_options cannot be null!");

        //Declare options as a list.
        List<Option> options = Arrays.stream(module_options).toList();

        //Not override option functionality.
        if (options.contains(Option.NOT_OVERRIDE)) {
            if (this.modules.containsKey(key))
                return;
        }

        //Saves module to the map.
        this.modules.put(key, value);
    }

    /**
     * Removes module.
     *
     * @param key Key.
     */
    public void remove(@Nonnull Object key) {
        this.modules.remove(Objects.requireNonNull(key, "key cannot be null!"));
    }
}
