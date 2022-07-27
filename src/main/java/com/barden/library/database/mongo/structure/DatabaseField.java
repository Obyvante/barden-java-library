package com.barden.library.database.mongo.structure;

import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

/**
 * Database field interface to handle required methods.
 *
 * @param <P> the type of the field
 */
@SuppressWarnings({"unused", "unchecked"})
public interface DatabaseField<P> {

    /**
     * Gets field path.
     *
     * @return {@link String}
     */
    @NotNull
    String getPath();

    /**
     * Checks if enum field is query field.
     *
     * @return if enum field is query field {@param true}, otherwise {@param false}.
     */
    boolean isQuery();

    /**
     * Gets bson value of current field.
     *
     * @param parent {@link P}
     * @return {@link BsonValue}
     */
    @NotNull
    BsonValue toBsonValue(@NotNull P parent);

    /**
     * Gets {@link BsonValue} from given path.
     *
     * @param bsonDocument {@link BsonDocument} to get {@link BsonValue} from.
     * @param path         {@link String} path to get {@link BsonValue} from.
     * @param clazz        {@link Class} type of {@link BsonValue} to get.
     * @param <T>          the type of the {@link BsonValue}
     * @return {@link BsonValue}
     * @throws NullPointerException If any of object parameters is null.
     */
    @NotNull
    static <T extends BsonValue> T getValueByPath(@NotNull BsonDocument bsonDocument, @NotNull String path, @NotNull Class<T> clazz) {
        //If path is empty, returns empty optional.
        if (path.isEmpty()) throw new IllegalArgumentException("path is empty");

        //If path doesn't contain any dot notation, no need to continue.
        if (!path.contains(".")) return Objects.requireNonNull((T) bsonDocument.get(path));

        //Splits paths by 'dot'.
        String[] paths = path.split("\\.");
        String path_end = paths[paths.length - 1];

        //Creates empty document to handle dot notation.
        @NotNull BsonDocument previous_document;
        //Handles document content.
        if (bsonDocument.containsKey(paths[0])) previous_document = bsonDocument.getDocument(paths[0]);
        else throw new NullPointerException("path value couldn't be found");

        //Crops path size since we use first one.
        paths = Arrays.copyOfRange(paths, 1, paths.length - 1);

        //Loops through path parts.
        for (String path_part : paths) {
            //Handles document creation.
            if (previous_document.containsKey(path_part)) previous_document = previous_document.getDocument(path_part);
            else throw new NullPointerException("path value couldn't be found");
        }

        //Returns value.
        return Objects.requireNonNull((T) previous_document.get(path_end));
    }
}