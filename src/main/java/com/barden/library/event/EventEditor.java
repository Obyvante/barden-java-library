package com.barden.library.event;

import com.barden.library.BardenJavaLibrary;
import com.barden.library.metadata.Metadata;
import com.barden.library.metadata.MetadataEntity;
import com.barden.library.scheduler.SchedulerRepository;
import com.barden.library.scheduler.task.Task;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Event editor class.
 */
@SuppressWarnings("unchecked")
public final class EventEditor<T extends Event> extends MetadataEntity implements EventExecutor {

    //Root event
    private final Class<T> commonClass;
    private final Collection<String> names;
    private EventOrder order;
    private Consumer<T> consumer;

    //Filters
    private final Collection<Function<T, Boolean>> functionFilters = new ArrayList<>();

    //Limit
    private int usageLimit;
    private int usage;

    //Expire
    private int expire;
    private TimeUnit expireUnit;
    private Task expireTask;
    private Consumer<EventEditor<T>> expireHandler;

    //Checks
    private boolean registered;
    private boolean unregistered;

    /**
     * Creates event editor object.
     *
     * @param events Events.
     */
    public EventEditor(@Nonnull Class<?>... events) {
        this(List.of(Objects.requireNonNull(events, "events cannot be null!")));
    }

    /**
     * Creates event editor object.
     *
     * @param events Events.
     */
    public EventEditor(@Nonnull Collection<Class<?>> events) {
        this(events, EventOrder.NORMAL);
    }

    /**
     * Creates event editor object.
     *
     * @param events Events.
     * @param order  Event order.
     */
    public EventEditor(@Nonnull Collection<Class<?>> events, @Nonnull EventOrder order) {
        this((Class<T>) Event.class, events, order);
    }

    /**
     * Creates event editor object.
     *
     * @param commonClass Common class. (BASE)
     * @param events      Events.
     * @param order       Event order.
     */
    public EventEditor(@Nonnull Class<T> commonClass, @Nonnull Collection<Class<?>> events, @Nonnull EventOrder order) {
        this.commonClass = Objects.requireNonNull(commonClass, "common class cannot be null!");
        this.names = Objects.requireNonNull(events, "events cannot be null!").stream().map(Class::getSimpleName).collect(Collectors.toList());
        this.order = Objects.requireNonNull(order, "order cannot be null!");
        //If there are multiple events, creates name based metadata.
        if (this.names.size() > 1)
            this.names.forEach(name -> this.metadata.set(name, new Metadata()));
    }

    /**
     * Gets metadata by name.
     *
     * @param name Event name.
     * @return Metadata by name.
     */
    @Nonnull
    public Metadata metadata(@Nonnull String name) {
        //If event name is not exist, throws null pointer.
        if (!this.names.contains(name) || this.names.size() > 1)
            throw new NullPointerException("name doesn't exist!");
        //Returns metadata by event name.
        return this.metadata.getNonNull(Objects.requireNonNull(name, "name cannot be null!"));
    }

    /**
     * Gets common class.
     *
     * @return Common class.
     */
    @Nonnull
    public Class<T> getCommonClass() {
        return this.commonClass;
    }

    /**
     * Gets names.
     *
     * @return Event names.
     */
    @Nonnull
    public Collection<String> getNames() {
        return this.names;
    }

    /**
     * Gets event order.
     *
     * @return Event order.
     */
    @Nonnull
    public EventOrder getOrder() {
        return this.order;
    }

    /**
     * Gets if it is registered or not.
     *
     * @return If it is registered or not.
     */
    public boolean isRegistered() {
        return this.registered;
    }

    /**
     * Gets if it is unregistered or not.
     *
     * @return If it is unregistered or not.
     */
    public boolean isUnregistered() {
        return this.unregistered;
    }

    /**
     * Registers event editor.
     */
    public void register() {
        //If it is unregistered, no need to continue.
        if (this.unregistered)
            return;

        //If event editor is registered, no need to continue.
        if (this.registered)
            return;
        this.registered = true;

        //Expire handler.
        if (this.expireUnit != null)
            this.expireTask = SchedulerRepository.create().after(this.expire, this.expireUnit).schedule(task -> {
                //If event editor is not valid, no need to continue.
                if (!this.isRegistered() || this.isUnregistered())
                    return;

                //Handles unregistration.
                try {
                    //If expire handler is set, runs handler.
                    if (this.expireHandler != null)
                        this.expireHandler.accept(this);
                } catch (Exception exception) {
                    //Logs error.
                    BardenJavaLibrary.getLogger().error("Couldn't run expire handler!", exception);
                } finally {
                    //Unregisters event editor.
                    this.unregister();
                }
            });
        //Returns event editor.
    }

    /**
     * Unregisters event editor.
     */
    public void unregister() {
        //If event editor is not registered, no need to continue.
        if (!this.registered)
            return;

        //If it is already unregistered, we do not want to use it, right?
        if (this.unregistered)
            return;
        this.unregistered = true;

        //If it is scheduled, cancels task.
        if (this.expireTask != null)
            this.expireTask.cancel();

        //Unregister event editor from repository.
        EventRepository.remove(this);
    }

    /**
     * Sets order.
     *
     * @param order Event order.
     * @return Event editor.
     */
    @Nonnull
    public EventEditor<T> order(@Nonnull EventOrder order) {
        //If event editor is registered or unregistered, no need to continue.
        if (this.registered || this.unregistered)
            return this;
        this.order = Objects.requireNonNull(order, "order cannot be null!");
        return this;
    }

    /**
     * Adds new functional event filter to the list.
     *
     * @param function_filter Functional event filter.
     * @return Builder.
     */
    @Nonnull
    public EventEditor<T> filter(@Nonnull Function<T, Boolean> function_filter) {
        //If event editor is registered or unregistered, no need to continue.
        if (this.registered || this.unregistered)
            return this;
        //If filter is already exist, no need to continue.
        if (this.functionFilters.contains(Objects.requireNonNull(function_filter, "function filter cannot be null!")))
            return this;
        //Adds filter to the list.
        this.functionFilters.add(function_filter);
        return this;
    }

    /**
     * Sets usage limit.
     *
     * @param usageLimit Usage limit.
     * @return Builder.
     */
    @Nonnull
    public EventEditor<T> limit(int usageLimit) {
        //If event editor is registered or unregistered, no need to continue.
        if (this.registered || this.unregistered)
            return this;
        this.usageLimit = usageLimit;
        return this;
    }

    /**
     * Sets expire duration and time unit.
     *
     * @param expire     Expire duration.
     * @param expireUnit Expire time unit.
     * @return Builder.
     */
    @Nonnull
    public EventEditor<T> expire(int expire, @Nonnull TimeUnit expireUnit) {
        //If event editor is registered or unregistered, no need to continue.
        if (this.registered || this.unregistered)
            return this;
        this.expire = expire;
        this.expireUnit = Objects.requireNonNull(expireUnit, "expire unit cannot be null!");
        return this;
    }

    /**
     * Sets expire duration and time unit.
     *
     * @param expire        Expire duration.
     * @param expireUnit    Expire time unit.
     * @param expireHandler Expire handler.
     * @return Builder.
     */
    @Nonnull
    public EventEditor<T> expire(int expire, @Nonnull TimeUnit expireUnit, @Nonnull Consumer<EventEditor<T>> expireHandler) {
        //If event editor is registered or unregistered, no need to continue.
        if (this.registered || this.unregistered)
            return this;
        this.expire = expire;
        this.expireUnit = Objects.requireNonNull(expireUnit, "expire unit cannot be null!");
        this.expireHandler = Objects.requireNonNull(expireHandler, "expire handler cannot be null!");
        return this;
    }

    /**
     * Sets order.
     *
     * @param consumer Event executor consumer.
     * @return Event editor.
     */
    @Nonnull
    public EventEditor<T> consume(@Nonnull Consumer<T> consumer) {
        //If event editor is registered or unregistered, no need to continue.
        if (this.registered || this.unregistered)
            return this;
        this.consumer = Objects.requireNonNull(consumer, "consumer cannot be null!");
        //Registers event editor.
        this.register();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void onExecute(@Nonnull Event event) {
        //Objects null check.
        Objects.requireNonNull(event, "event cannot be null!");

        //If event editor is not registered, throws error.
        if (!this.registered)
            throw new IllegalStateException("event editor is not registered!");

        //If event editor is unregistered, throws error.
        if (this.unregistered)
            throw new IllegalStateException("event editor is unregistered!");

        //Checks functional filters.
        boolean should_continue = true;
        for (Function<T, Boolean> function_filter : this.functionFilters) {
            //Applies function filter then checks if it is positive.
            if (function_filter.apply((T) event))
                continue;
            //If not, no need to continue.
            should_continue = false;
            break;
        }
        //If any of the functional filters false, no need to continue.
        if (!should_continue)
            return;

        //Checks usage limit.
        if (this.usageLimit != 0 && this.usage++ >= this.usageLimit) {
            //Unregisters event editor.
            this.unregister();
            return;
        }

        //Accepts consumer for declared event.
        this.consumer.accept((T) event);
    }
}