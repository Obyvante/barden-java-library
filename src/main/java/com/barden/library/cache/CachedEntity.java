package com.barden.library.cache;

import com.barden.library.scheduler.SchedulerProvider;
import com.barden.library.scheduler.task.Task;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Cached entity abstract class.
 */
public abstract class CachedEntity {

    private final long time;
    private final TimeUnit unit;
    private final Consumer<Task> action;
    private Task task;

    private boolean done;

    /**
     * Creates cached entity object.
     *
     * @param time   Time.
     * @param unit   Time unit.
     * @param action Task consumer.
     */
    public CachedEntity(long time, @Nonnull TimeUnit unit, @Nonnull Consumer<Task> action) {
        this.unit = Objects.requireNonNull(unit, "time unit cannot be null!");
        this.time = time;
        this.action = Objects.requireNonNull(action, "action cannot be null!");
        this.task = SchedulerProvider.create().after(time, unit).schedule(f_task -> {
            //Accepts action with current task.
            this.action.accept(f_task);
            //Makes cached entity be done.
            this.done = true;
            //Makes task null since we no longer need.
            this.task = null;
        });
    }

    /**
     * Resets cache time.
     */
    protected void resetCacheTime() {
        //Makes done false.
        this.done = false;

        //Cancels previous task if there are any.
        if (this.task != null)
            this.task.cancel();

        //Creates new task.
        this.task = SchedulerProvider.create().after(time, unit).schedule(f_task -> {
            //Accepts action with current task.
            this.action.accept(f_task);
            //Makes cached entity be done.
            this.done = true;
            //Makes task null since we no longer need.
            this.task = null;
        });
    }

    /**
     * Gets if cache time expired or not.
     *
     * @return If cache time expired or not.
     */
    protected boolean isCacheTimeExpired() {
        return this.done;
    }
}
