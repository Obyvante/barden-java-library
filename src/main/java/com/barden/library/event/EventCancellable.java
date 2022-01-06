package com.barden.library.event;

/**
 * Event cancellable class.
 */
public abstract class EventCancellable extends Event {

    protected boolean cancelled;

    /**
     * Creates event cancellable object.
     */
    public EventCancellable() {
    }

    /**
     * Gets if event is cancelled or not.
     *
     * @return If event is cancelled or not.
     */
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Sets event cancel status.
     *
     * @param cancelled Event cancel status.
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
