package com.barden.library.database.mongo.utils;

import alexh.weak.Dynamic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Mongo mapper class.
 */
@SuppressWarnings("unchecked")
public final class MongoMapper {

    /**
     * Gets destination/path from the map.
     *
     * @param map           Map.
     * @param path          Path or dot notation.
     * @param type          Class type for expected result.
     * @param default_value Default value.
     * @param <T>           Excepted class type for return value.
     * @return Map value.
     */
    @Nonnull
    public static <T> T get(@Nonnull Map<String, Object> map, @Nonnull String path, Class<T> type, @Nonnull T default_value) {
        //Objects null check.
        Objects.requireNonNull(map, "map cannot be null!");
        Objects.requireNonNull(path, "path cannot be null!");
        Objects.requireNonNull(default_value, "default_value cannot be null!");

        //Gets map path with dynamic.
        Dynamic dynamic = Dynamic.from(map).dget(path);
        //If it is present, no need to continue.
        if (dynamic.isPresent())
            return dynamic.as(type);

        //Returns default value if it is not present.
        return default_value;
    }

    /**
     * Gets destination/path from the map.
     *
     * @param map  Map.
     * @param path Path or dot notation.
     * @param type Class type for expected result.
     * @param <T>  Excepted class type for return value.
     * @return Map value.
     */
    @Nonnull
    public static <T> T getNonNull(@Nonnull Map<String, Object> map, @Nonnull String path, @Nonnull Class<T> type) {
        return Objects.requireNonNull(get(map, path, type), "object cannot be null!");
    }

    /**
     * Gets destination/path from the map.
     *
     * @param map  Map.
     * @param path Path or dot notation.
     * @param <T>  Excepted class type for return value.
     * @return Map value.
     */
    @Nullable
    public static <T> T get(@Nonnull Map<String, Object> map, @Nonnull String path) {
        return (T) get(map, path, Object.class);
    }

    /**
     * Gets destination/path from the map.
     *
     * @param map  Map.
     * @param path Path or dot notation.
     * @param <T>  Excepted class type for return value.
     * @return Map value.
     */
    @Nonnull
    public static <T> T getNonNull(@Nonnull Map<String, Object> map, @Nonnull String path) {
        return (T) Objects.requireNonNull(get(map, path, Object.class), "object cannot be null!");
    }

    /**
     * Gets destination/path from the map.
     *
     * @param map  Map.
     * @param path Path or dot notation.
     * @param type Class type for expected result.
     * @param <T>  Excepted class type for return value.
     * @return Map value.
     */
    @Nullable
    public static <T> T get(@Nonnull Map<String, Object> map, @Nonnull String path, @Nonnull Class<T> type) {
        //Objects null check.
        Objects.requireNonNull(map, "map cannot be null!");
        Objects.requireNonNull(path, "path cannot be null!");

        //Gets map path with dynamic.
        Dynamic dynamic = Dynamic.from(map).dget(path);
        //If it is present, no need to continue.
        if (dynamic.isPresent())
            return dynamic.as(type);

        //Returns null if it is not present.
        return null;
    }

    /**
     * Sets/Adds declared destination/path in the map.
     *
     * @param map   Map.
     * @param path  Path or dot notation.
     * @param value Value.
     * @param <T>   Excepted class type for value.
     * @return Map.
     */
    @Nonnull
    public static <T extends Map<String, Object>> T set(@Nonnull Map<String, Object> map, @Nonnull String path, @Nonnull Object value) {
        //Objects null check.
        Objects.requireNonNull(map, "map cannot be null!");
        Objects.requireNonNull(path, "path cannot be null!");
        Objects.requireNonNull(value, "value cannot be null!");

        //If path doesn't have dot notation, no need to continue.
        if (!path.contains(".")) {
            map.put(path, value);
            return (T) map;
        }

        //Declares dotted path.
        String[] dotted_path = path.split("\\.");

        //Creates dynamic map editor.
        Dynamic dynamic = Dynamic.from(map);

        //Loop through the paths.
        for (int i = 0; i < dotted_path.length - 1; i++) {
            //If parent is not exist, create one.
            if (!dynamic.get(dotted_path[i]).isPresent())
                dynamic.asMap().put(dotted_path[i], new LinkedHashMap<String, Object>());

            //Sets new dynamic.
            dynamic = dynamic.get(dotted_path[i]);
        }

        //Puts value to declared path.
        dynamic.asMap().put(dotted_path[dotted_path.length - 1], value);

        //Returns edited map.
        return (T) map;
    }

    /**
     * Removes declared destination/path from the map.
     *
     * @param map  Map.
     * @param path Path or dot notation.
     * @param <T>  Excepted class type for value.
     * @return Map.
     */
    @Nonnull
    public static <T extends Map<String, Object>> T remove(@Nonnull Map<String, Object> map, @Nonnull String path) {
        //Objects null check.
        Objects.requireNonNull(map, "map cannot be null!");
        Objects.requireNonNull(path, "path cannot be null!");

        //If path doesn't have dot notation, no need to continue.
        if (!path.contains(".")) {
            map.remove(path);
            return (T) map;
        }

        //Declares dotted path.
        String[] dotted_path = path.split("\\.");

        //Creates string builder to find exact path as dot notation.
        StringBuilder targeted_path = new StringBuilder();

        //Loop through the paths.
        for (int i = 0; i < dotted_path.length - 1; i++) {
            if (targeted_path.length() == 0)
                targeted_path.append(dotted_path[i]);
            else
                targeted_path.append(".").append(dotted_path[i]);
        }

        //Removes declared path.
        Dynamic.from(map).dget(targeted_path.toString()).maybe().ifPresent(dynamic_object -> dynamic_object.asMap().remove(dotted_path[dotted_path.length - 1]));

        //Returns edited map.
        return (T) map;
    }
}
