package com.barden.library.scheduler.task;

import com.barden.library.BardenJavaLibrary;
import com.barden.library.metadata.MetadataEntity;
import com.barden.library.scheduler.Scheduler;
import com.barden.library.scheduler.SchedulerProvider;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Task class.
 */
public final class Task extends MetadataEntity implements Runnable {

    private final UUID id = UUID.randomUUID();

    private final Consumer<Task> consumer;
    private final long delay;
    private final long repeat;
    private final boolean block;

    private final ScheduledFuture<?> future;
    private volatile Thread thread;

    /**
     * Creates task object.
     *
     * @param scheduler Scheduler.
     */
    public Task(@Nonnull Scheduler scheduler) {
        //Objects null check.
        Objects.requireNonNull(scheduler, "scheduler cannot be null!");

        this.consumer = scheduler.getConsumer();
        this.delay = scheduler.getDelay();
        this.repeat = scheduler.getRepeat();
        this.block = scheduler.isBlock();
        this.future = this.repeat == 0 ?
                BardenJavaLibrary.getScheduler().getTimerService().schedule(this, this.delay, TimeUnit.MILLISECONDS) :
                BardenJavaLibrary.getScheduler().getTimerService().scheduleAtFixedRate(this, this.delay, this.repeat, TimeUnit.MILLISECONDS);

        //Adds task to the list.
        BardenJavaLibrary.getScheduler().addTask(this);

        //Blocks current thread until the task completion.
        if (this.block)
            while (true) {
                if (!this.future.isDone() && !this.future.isCancelled())
                    continue;
                break;
            }
    }

    /**
     * Gets id.
     *
     * @return Task id.
     */
    @Nonnull
    public UUID getId() {
        return this.id;
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
     * Gets if task is blocking or not.
     *
     * @return If task is blocking or not.
     */
    public boolean isBlock() {
        return this.block;
    }

    /**
     * Gets status.
     *
     * @return Task status.
     */
    @Nonnull
    public TaskStatus getStatus() {
        if (this.future == null)
            return TaskStatus.SCHEDULED;
        else if (this.future.isCancelled())
            return TaskStatus.CANCELLED;
        else if (this.future.isDone())
            return TaskStatus.FINISHED;
        return TaskStatus.SCHEDULED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        //Gets scheduled executor service.
        ScheduledExecutorService service = BardenJavaLibrary.getScheduler().getTimerService();

        //Executes service.
        service.execute(() -> {
            //Gets current thread.
            this.thread = Thread.currentThread();

            //Handles errors.
            try {
                this.consumer.accept(this);
            } catch (Exception exception) {
                //Logs error.
                SchedulerProvider.getLogger().error("Couldn't run task(" + this.getId() + ")!", exception);
            } finally {
                //Removes task from the list.
                if (this.repeat == 0)
                    BardenJavaLibrary.getScheduler().removeTask(this);

                //Sets thread null.
                this.thread = null;
            }
        });
    }

    /**
     * Cancels task.
     */
    public void cancel() {
        //If there is no future task, no need to continue.
        if (this.future == null)
            return;

        //Cancels future.
        this.future.cancel(false);

        //If thread is set, terminates it.
        if (this.thread != null)
            this.thread.interrupt();

        //Removes task from the list.
        BardenJavaLibrary.getScheduler().removeTask(this);
    }
}
