package com.barden.library.event;

import com.barden.library.scheduler.SchedulerRepository;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Event repository class.
 */
public final class EventRepository {

    private static final List<EventExecutor> executors = new ArrayList<>();

    /**
     * Creates event editor object.
     *
     * @param event Event.
     * @return Event editor.
     */
    @Nonnull
    public static <T extends Event> EventEditor<T> of(@Nonnull Class<T> event) {
        //Creates event editor.
        EventEditor<T> editor = new EventEditor<>(event, event);
        //Adds event editor(executor to the list.)
        EventRepository.add(editor);
        //Returns created editor.
        return editor;
    }

    /**
     * Creates event editor object.
     *
     * @param events Events.
     * @return Event editor.
     */
    @SafeVarargs
    @Nonnull
    public static EventEditor<Event> of(@Nonnull Class<? extends Event>... events) {
        //Creates event editor.
        EventEditor<Event> editor = new EventEditor<>(events);
        //Adds event editor(executor to the list.)
        EventRepository.add(editor);
        //Returns created editor.
        return editor;
    }

    /**
     * Creates event editor object.
     *
     * @param common Common event class.
     * @param events Events.
     * @return Event editor.
     */
    @SafeVarargs
    @Nonnull
    public static <T extends Event> EventEditor<T> from(@Nonnull Class<T> common, @Nonnull Class<? extends Event>... events) {
        //Creates event editor.
        EventEditor<T> editor = new EventEditor<>(common, List.of(Objects.requireNonNull(events, "events cannot be null!")), EventOrder.NORMAL);
        //Adds event editor(executor to the list.)
        EventRepository.add(editor);
        //Returns created editor.
        return editor;
    }

    /**
     * Adds executor.
     *
     * @param executor Event executor.
     */
    public static void add(@Nonnull EventExecutor executor) {
        executors.add(Objects.requireNonNull(executor, "executor cannot be null!"));
    }

    /**
     * Removes executor.
     *
     * @param executor Event executor.
     */
    public static void remove(@Nonnull EventExecutor executor) {
        executors.remove(Objects.requireNonNull(executor, "executor cannot be null!"));
    }

    /**
     * Executes events.
     *
     * @param events Events.
     */
    public static void execute(@Nonnull Event... events) {
        //Objects null check.
        Objects.requireNonNull(events, "events cannot be null!");

        //Loop through events.
        for (@Nonnull Event event : events) {
            List<EventExecutor> _executors = Lists.newArrayList(Collections2.filter(executors, input -> input.getNames().contains(event.getName())));
            _executors.sort(Comparator.comparing(EventExecutor::getOrder));
            _executors.forEach(executor -> {
                //Handles event thread.
                if (event.isAsynchronous())
                    SchedulerRepository.schedule(task -> executor.onExecute(event));
                else
                    executor.onExecute(event);
            });
        }
    }
}
