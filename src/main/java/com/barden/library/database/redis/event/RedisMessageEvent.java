package com.barden.library.database.redis.event;

import com.barden.library.event.Event;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Redis message event.
 */
public class RedisMessageEvent extends Event {

    private final String channel;
    private final String message;

    /**
     * Crates redis message event object.
     *
     * @param channel Channel.
     * @param message Message.
     */
    public RedisMessageEvent(@Nonnull String channel, @Nonnull String message) {
        this.channel = Objects.requireNonNull(channel, "channel cannot be null!");
        this.message = Objects.requireNonNull(message, "message cannot be null!");
    }

    /**
     * Gets channel
     *
     * @return Channel.
     */
    @Nonnull
    public String getChannel() {
        return channel;
    }

    /**
     * Gets received message.
     *
     * @return Received message.
     */
    @Nonnull
    public String getMessage() {
        return this.message;
    }

    /**
     * Gets message as json.
     *
     * @return Message as json.
     */
    @Nonnull
    public JsonObject getMessageAsJson() {
        return new Gson().fromJson(this.message, JsonObject.class);
    }
}
