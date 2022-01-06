package com.barden.library.cooldown;

import com.barden.library.scheduler.Task;
import com.barden.library.scheduler.SchedulerRepository;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Pretty basic cool down map integration with extended HashMap.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public final class CoolDownMap<K, V> extends HashMap<K, V> {

    /**
     * Cool down map status.
     * If it is expired, then it will be 'false'.
     */
    private boolean status = true;
    /**
     * Bukkit task id.
     */
    private final Task task;

    private Consumer<CoolDownMap<K, V>> expireHandler;

    /**
     * Creates hash map with declared duration and time unit.
     *
     * @param duration Duration.
     * @param unit     Time unit.
     */
    public CoolDownMap(int duration, @Nonnull TimeUnit unit) {
        Objects.requireNonNull(unit, "time unit cannot be null!");
        //Creates bukkit task with delay of duration.
        this.task = SchedulerRepository.create().after(duration, unit).schedule(task -> {
            //Expires handler.
            if (this.expireHandler != null)
                this.expireHandler.accept(this);
            //Change the status of the map
            this.status = false;
            //Clears the map
            this.clear();
        });
    }

    /**
     * Sets expire handler.
     *
     * @param expireHandler Expire handler.
     */
    @Nonnull
    public CoolDownMap<K, V> setExpireHandler(@Nonnull Consumer<CoolDownMap<K, V>> expireHandler) {
        this.expireHandler = Objects.requireNonNull(expireHandler, "expire handler cannot be null!");
        return this;
    }

    @Override
    public V put(K key, V value) {
        if (this.hasExpired())
            return null;
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (this.hasExpired())
            return;
        super.putAll(m);
    }

    @Override
    public V remove(Object key) {
        if (this.hasExpired())
            return null;
        return super.remove(key);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        if (this.hasExpired())
            return null;
        return super.getOrDefault(key, defaultValue);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        if (this.hasExpired())
            return null;
        return super.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        if (this.hasExpired())
            return false;
        return super.remove(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        if (this.hasExpired())
            return false;
        return super.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        if (this.hasExpired())
            return null;
        return super.replace(key, value);
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        if (this.hasExpired())
            return null;
        return super.merge(key, value, remappingFunction);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        if (this.hasExpired())
            return;
        super.replaceAll(function);
    }

    /**
     * Gets if the cool down map has expired or not.
     *
     * @return If the cool down map has expired or not.
     */
    public boolean hasExpired() {
        return !this.status;
    }

    /**
     * Cancels cool down map.
     */
    public void cancel() {
        //If the cool down map has already expired, we do not want to touch it again.
        if (!this.status)
            return;
        //Sets status false if developer wants to check if it is expired or not.
        this.status = false;
        //Cancels existed bukkit task.
        if (this.task != null)
            this.task.cancel();
    }
}
