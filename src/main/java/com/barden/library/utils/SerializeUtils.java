package com.barden.library.utils;

import com.barden.library.BardenJavaLibrary;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

/**
 * Serializer library.
 */
@SuppressWarnings("unchecked")
public final class SerializeUtils {

    /**
     * Serializes object.
     *
     * @param object Object.
     * @return Optional serialized.
     */
    @Nonnull
    public synchronized static Optional<String> serialize(@Nonnull Object object) {
        //Objects null check.
        Objects.requireNonNull(object, "object cannot be null!");

        //Tries to encode object with Base64.
        try {
            //Declare output streams.
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(io);

            //Process object with streams.
            os.writeObject(object);
            os.flush();

            //Return encoded object string.
            return Optional.of(Base64.getEncoder().encodeToString(io.toByteArray()));
        } catch (Exception exception) {
            BardenJavaLibrary.getLogger().error("Couldn't serialize object!", exception);
        }

        //If there is an error, return empty.
        return Optional.empty();
    }

    /**
     * Deserializes object.
     *
     * @param encoded_object Encoded object.
     * @param object_type    Object type class.
     * @param <T>            Object type.
     * @return Optional deserialized object.
     */
    @Nonnull
    public synchronized static <T> Optional<T> deserialize(@Nonnull String encoded_object, @Nonnull Class<T> object_type) {
        //Objects null check.
        Objects.requireNonNull(encoded_object, "encoded object cannot be null!");
        Objects.requireNonNull(object_type, "object type cannot be null!");

        //Tries to decode object with Base64.
        try {
            //Decode encoded object.
            byte[] decoded_object = Base64.getDecoder().decode(encoded_object);

            //Declare input streams.
            ByteArrayInputStream in = new ByteArrayInputStream(decoded_object);
            ObjectInputStream is = new ObjectInputStream(in);

            //Return decoded object.
            return Optional.ofNullable((T) is.readObject());
        } catch (Exception exception) {
            BardenJavaLibrary.getLogger().error("Couldn't deserialize object!", exception);
        }

        //If there is an error, return empty.
        return Optional.empty();
    }

}
