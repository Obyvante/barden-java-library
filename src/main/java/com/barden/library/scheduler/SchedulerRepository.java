package com.barden.library.scheduler;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Barden scheduler class. (Library)
 */
public final class SchedulerRepository {

    /*
    STATICS
     */

    private static final Logger logger = LoggerFactory.getLogger(SchedulerRepository.class);

    /**
     * Gets logger.
     *
     * @return Logger.
     */
    @Nonnull
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Creates new scheduler.
     *
     * @return Scheduler.
     */
    @Nonnull
    public static Scheduler create() {
        return new Scheduler();
    }

    /**
     * Schedules a task.
     *
     * @param task Task.
     * @return Task.
     */
    @Nonnull
    public static Task schedule(@Nonnull Consumer<Task> task) {
        return new Scheduler().schedule(task);
    }


    /*
    ROOT
     */

    //[EXECUTOR SERVICE]
    private final ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("Scheduler - #%d")
            .build());
    //[EXECUTOR TIMER SERVICE]
    private final ScheduledExecutorService executorTimerService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("Scheduler Timer")
            .build());

    //[SYNCHRONIZED HASH SET]
    private final Set<Task> tasks = Collections.synchronizedSet(new HashSet<>());

    /**
     * Gets executor service.
     *
     * @return Executor service.
     */
    @Nonnull
    public ExecutorService getService() {
        return this.executorService;
    }

    /**
     * Gets executor timer service.
     *
     * @return Executor timer service.
     */
    @Nonnull
    public ScheduledExecutorService getTimerService() {
        return this.executorTimerService;
    }

    /**
     * Gets barden schedulers.
     *
     * @return Barden schedulers.
     */
    @Nonnull
    public Set<Task> getTasks() {
        return this.tasks;
    }

    /**
     * Finds task by its id.
     *
     * @param id Task id.
     * @return Optional task.
     */
    @Nonnull
    public Optional<Task> findTask(@Nonnull UUID id) {
        return this.tasks.stream().filter(task -> task.getId().equals(Objects.requireNonNull(id, "id cannot be null"))).findFirst();
    }

    /**
     * Gets task by its id.
     *
     * @param id Task id.
     * @return Task.
     */
    public Task getTask(@Nonnull UUID id) {
        return this.findTask(id).orElseThrow(() -> new NullPointerException("task cannot be null!"));
    }

    /**
     * Adds task.
     *
     * @param task Task.
     */
    public void addTask(@Nonnull Task task) {
        this.tasks.add(Objects.requireNonNull(task, "task cannot be null!"));
    }

    /**
     * Removes task.
     *
     * @param task Task.
     */
    public void removeTask(@Nonnull Task task) {
        this.tasks.remove(Objects.requireNonNull(task, "task cannot be null!"));
    }

    /**
     * Shutdowns schedulers.
     *
     * @return {@code true} if all tasks finished, {@code false} otherwise.
     * @throws InterruptedException If the current thread was interrupted.
     */
    public boolean shutdown() throws InterruptedException {
        //Creates terminating tasks.
        Collection<Task> terminating_tasks;

        //Loops synchronized tasks.
        synchronized (this.tasks) {
            terminating_tasks = ImmutableList.copyOf(this.tasks);
        }

        //Cancels all tasks.
        for (Task task : terminating_tasks)
            task.cancel();

        //Shutdowns executors.
        this.executorTimerService.shutdown();
        this.executorService.shutdown();

        //Awaits termination.
        return this.executorService.awaitTermination(10, TimeUnit.SECONDS);
    }
}
