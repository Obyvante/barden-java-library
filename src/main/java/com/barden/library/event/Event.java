package com.barden.library.event;

import javax.annotation.Nonnull;

/**
 * Event class.
 */
public abstract class Event {

    protected final String name;
    private final boolean async;

    /**
     * Creates event object.
     */
    public Event() {
        this.name = this.getClass().getSimpleName();
        this.async = false;
    }

    /**
     * Creates event object.
     * If event is asynchronous, common event methods will
     * not synchronize with each other.
     *
     * @param async Async or not.
     */
    public Event(boolean async) {
        this.name = this.getClass().getSimpleName();
        this.async = async;
    }

    /**
     * Gets name.
     *
     * @return Event name.
     */
    @Nonnull
    public final String getName() {
        return this.name;
    }

    /**
     * Gets if event is asynchronous or not.
     *
     * @return If event is asynchronous or not.
     */
    public final boolean isAsynchronous() {
        return this.async;
    }
}
