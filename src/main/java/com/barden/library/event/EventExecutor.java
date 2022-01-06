package com.barden.library.event;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Event executor.
 */
public interface EventExecutor {

    /**
     * Gets event names.
     *
     * @return Event names.
     */
    @Nonnull
    Collection<String> getNames();

    /**
     * Gets order.
     * @return Event order.
     */
    @Nonnull
    EventOrder getOrder();

    /**
     * Be triggered when event executes.
     *
     * @param event Event.
     */
    void onExecute(@Nonnull Event event);
}
