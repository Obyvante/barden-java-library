package com.barden.library.database;

import com.barden.library.database.influx.InfluxProvider;
import com.barden.library.database.mongo.MongoProvider;
import com.barden.library.database.redis.RedisProvider;
import com.barden.library.file.TomlFileLoader;
import com.electronwill.nightconfig.core.CommentedConfig;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Database repository class.
 */
public final class DatabaseProvider {

    private static CommentedConfig config;

    private static MongoProvider mongoProvider;
    private static RedisProvider redisProvider;
    private static InfluxProvider influxProvider;

    /**
     * Creates database object.
     */
    public static void initialize() {
        //If config is loaded, no need to continue.
        if (config != null)
            return;

        //Initializes -> [CONFIG]
        config = TomlFileLoader.getConfig("database", true).orElseThrow();

        //Initializes -> [MONGO]
        if (config.getInt("mongo.enabled") == 1)
            mongoProvider = new MongoProvider(
                    config.getOrElse("mongo.host", "localhost"),
                    config.getOrElse("mongo.port", 27017),
                    config.getOrElse("mongo.username", ""),
                    config.getOrElse("mongo.password", ""),
                    config.getOrElse("mongo.auth", "admin"));

        //Initializes -> [REDIS]
        if (config.getInt("redis.enabled") == 1)
            redisProvider = new RedisProvider(
                    config.getOrElse("redis.host", "localhost"),
                    config.getOrElse("redis.port", 6379),
                    config.getOrElse("redis.password", ""));

        //Initializes -> [INFLUX]
        if (config.getInt("influx.enabled") == 1)
            influxProvider = new InfluxProvider(
                    config.getOrElse("influx.host", "localhost"),
                    config.getOrElse("influx.port", 8086),
                    config.getOrElse("influx.token", ""),
                    config.getOrElse("influx.organization", ""),
                    config.getOrElse("influx.bucket", "default"));
    }

    /**
     * Gets config.
     *
     * @return Commented config.
     */
    @Nonnull
    public static CommentedConfig getConfig() {
        return config;
    }

    /**
     * Gets mongo provider.
     *
     * @return Mongo provider.
     */
    @Nonnull
    public static MongoProvider mongo() {
        return mongoProvider;
    }

    /**
     * Gets mongo provider safe.
     *
     * @return Optional mongo provider.
     */
    @Nonnull
    public static Optional<MongoProvider> safeMongo() {
        return Optional.ofNullable(mongoProvider);
    }

    /**
     * Gets redis provider.
     *
     * @return Redis provider.
     */
    @Nonnull
    public static RedisProvider redis() {
        return redisProvider;
    }

    /**
     * Gets redis provider safe.
     *
     * @return Optional redis provider.
     */
    @Nonnull
    public static Optional<RedisProvider> safeRedis() {
        return Optional.ofNullable(redisProvider);
    }

    /**
     * Gets influx provider.
     *
     * @return Influx provider.
     */
    @Nonnull
    public static InfluxProvider influx() {
        return influxProvider;
    }

    /**
     * Gets influx provider safe.
     *
     * @return Optional influx provider.
     */
    @Nonnull
    public static Optional<InfluxProvider> safeInflux() {
        return Optional.ofNullable(influxProvider);
    }
}
