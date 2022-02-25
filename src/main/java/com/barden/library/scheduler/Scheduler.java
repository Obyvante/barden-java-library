package com.barden.library.scheduler;

import com.barden.library.scheduler.task.Task;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Scheduler class.
 */
public final class Scheduler {

    private Consumer<Task> consumer;
    private long delay;
    private long repeat;
    private boolean block;

    /**
     * Gets consumer.
     *
     * @return Consumer.
     */
    @Nonnull
    public Consumer<Task> getConsumer() {
        return this.consumer;
    }

    /**
     * Gets delay.
     *
     * @return Delay.
     */
    public long getDelay() {
        return this.delay;
    }

    /**
     * Gets repeat.
     *
     * @return Repeat.
     */
    public long getRepeat() {
        return this.repeat;
    }

    /**
     * Gets if scheduler blocks main thread or not.
     *
     * @return If scheduler blocks main thread or not.
     */
    public boolean isBlock() {
        return this.block;
    }

    /**
     * Sets delay.
     *
     * @param time Time.
     * @param unit Time unit.
     * @return Scheduler.
     */
    @Nonnull
    public Scheduler after(long time, @Nonnull TimeUnit unit) {
        this.delay = Objects.requireNonNull(unit, "time unit cannot be null!").toMillis(time);
        return this;
    }

    /**
     * Sets repeat.
     *
     * @param time Time.
     * @param unit Time unit.
     * @return Scheduler.
     */
    @Nonnull
    public Scheduler every(long time, @Nonnull TimeUnit unit) {
        this.repeat = Objects.requireNonNull(unit, "time unit cannot be null!").toMillis(time);
        return this;
    }

    /**
     * Blocks inside thread.
     *
     * @return Scheduler.
     */
    @Nonnull
    public Scheduler block() {
        this.block = !this.block;
        return this;
    }

    /**
     * Schedules task.
     *
     * @return Task.
     */
    @Nonnull
    public Task schedule(@Nonnull Consumer<Task> task) {
        this.consumer = Objects.requireNonNull(task, "task cannot be null!");
        return new Task(this);
    }
}
