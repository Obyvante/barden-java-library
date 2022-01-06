package com.barden.library.database.redis;

import com.barden.library.BardenJavaLibrary;
import com.barden.library.database.redis.event.RedisMessageEvent;
import com.barden.library.event.EventRepository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Redis provider class.
 */
public final class RedisProvider {

    private boolean initialized = false;

    private final String host;
    private final int port;

    private final String password;

    //Jedis
    private final JedisPool client;
    private final JedisPoolConfig clientSettings = new JedisPoolConfig();

    //Publish and subscribe
    private Jedis subscribeClient;
    private JedisPubSub subscribe;

    private final Set<String> channels = new LinkedHashSet<>();
    private Set<String> channelsQueue = new LinkedHashSet<>();

    private boolean subscribeStatus = false;

    private Thread channelThread;

    /**
     * Create Redis connection and provider class with Jedis
     *
     * @param host     Host or IP address of Redis server.
     * @param port     Port address of Redis server.
     * @param password (Optional) Redis password.
     */
    public RedisProvider(String host, int port, String password) {
        this.host = host;
        this.port = port;
        this.password = password;

        //Client settings
        this.clientSettings.setMaxTotal(32);
        this.clientSettings.setMaxIdle(32);
        this.clientSettings.setMinIdle(8);

        this.clientSettings.setMaxWaitMillis(10000);

        this.clientSettings.setJmxEnabled(true);

        this.clientSettings.setBlockWhenExhausted(true);

        this.clientSettings.setTestWhileIdle(true);
        this.clientSettings.setTestOnBorrow(true);

        //Handles authorization.
        if (!this.password.isEmpty()) {
            this.client = new JedisPool(this.clientSettings, this.host, this.port, 0, this.password);
        } else {
            this.client = new JedisPool(this.clientSettings, this.host, this.port, 0);
            BardenJavaLibrary.getLogger().warn("You are not using any username and password for Redis server.");
        }

        //Pings to the database server to make sure it is working.
        if (!this.ping())
            return;

        //Initializes channel.
        this.initializeChannels();

        //Logging.
        BardenJavaLibrary.getLogger().info("Successfully connected to Redis! (" + this.host + ":" + this.port + ")");

        //Initialization.
        initialized = true;
    }

    /**
     * Connection test.
     */
    private boolean ping() {
        try (Jedis resource = this.client.getResource()) {
            if (resource.isConnected())
                return true;
            throw new Exception("Couldn't get resource from Redis!");
        } catch (Exception exception) {
            BardenJavaLibrary.getLogger().error("Couldn't connect to Redis! §4(" + this.host + ":" + this.port + ")", exception);
        }
        return false;
    }

    /**
     * Gets if Redis initialized or not.
     *
     * @return If Redis initialized or not.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Gets Redis host.
     *
     * @return Redis host.
     */
    @Nonnull
    public String getHost() {
        return host;
    }

    /**
     * Gets Redis port.
     *
     * @return Redis port.
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets Redis password.
     *
     * @return Redis password.
     */
    @Nonnull
    public String getPassword() {
        return password;
    }

    /**
     * Gets Redis connected client.
     *
     * @return Redis client.
     */
    @Nonnull
    public JedisPool getClient() {
        return client;
    }

    /**
     * Gets Redis client settings.
     *
     * @return Redis client settings.
     */
    @Nonnull
    public JedisPoolConfig getClientSettings() {
        return clientSettings;
    }

    /**
     * Subscribes channels.
     *
     * @param channels Channels.
     * @return If subscribe is successfully or not.
     */
    public boolean subscribe(@Nonnull String... channels) {
        //Objects null check.
        Objects.requireNonNull(channels, "channels cannot be null!");

        //If it is not ready, add channels to the queue.
        if (!this.subscribeStatus) {
            this.channelsQueue.addAll(Arrays.asList(channels));
            return false;
        }

        //If Jedis hasn't subscribed yet, no need to continue.
        if (this.subscribe == null)
            return false;

        //Tries to subscribe channels
        try {
            this.subscribe.subscribe(channels);
        } catch (Exception exception) {
            BardenJavaLibrary.getLogger().error("Couldn't subscribe Jedis channel(s)!", exception);
            BardenJavaLibrary.getLogger().error("Channels; " + Arrays.toString(channels));
            return false;
        }

        return true;
    }

    /**
     * Unsubscribes channels.
     *
     * @param channels Channels.
     * @return If remove unsubscribe is successfully or not.
     */
    public boolean unsubscribe(@Nonnull String... channels) {
        Objects.requireNonNull(channels, "channels cannot be null!");

        //If Jedis hasn't subscribed yet, no need to continue.
        if (!this.subscribeStatus || this.subscribe == null)
            return false;

        //Tries to unsubscribe channels
        try {
            this.subscribe.unsubscribe(channels);
        } catch (Exception exception) {
            BardenJavaLibrary.getLogger().error("Couldn't unsubscribe Jedis channel(s)!", exception);
            BardenJavaLibrary.getLogger().error("Channels; " + Arrays.toString(channels));
            return false;
        }

        return true;
    }

    /**
     * Initializes channels.
     */
    private void initializeChannels() {
        //Sets subscribe field.
        this.subscribe = new JedisPubSub() {
            @Override
            public void onMessage(@Nonnull String channel, @Nonnull String message) {
                EventRepository.execute(new RedisMessageEvent(channel, message));
            }

            @Override
            public void onSubscribe(@Nonnull String channel, int subscribedChannels) {
                channels.add(channel);
                if (channel.equals("barden-channels"))
                    registerQueue();
            }

            @Override
            public void onUnsubscribe(@Nonnull String channel, int subscribedChannels) {
                channels.remove(channel);
            }
        };

        //Registers channel.
        this.registerChannel();
    }

    /**
     * Registers channel in task.
     */
    private void registerChannel() {
        this.channelThread = new Thread(() -> {
            try {
                this.subscribeClient = new Jedis(this.host);
                if (!this.password.isEmpty())
                    this.subscribeClient.auth(this.password);
                this.subscribeClient.subscribe(this.subscribe, "barden-channels");
                this.subscribeClient.close();
            } catch (Exception exception) {
                BardenJavaLibrary.getLogger().error("Couldn't connect to Jedis pub/sub!", exception);
            }
        });

        this.channelThread.start();
    }

    /**
     * Register queued channels.
     */
    private void registerQueue() {
        if (!this.channelsQueue.isEmpty())
            this.subscribe.subscribe(this.channelsQueue.toArray(new String[0]));
        this.channelsQueue = new LinkedHashSet<>();
        this.subscribeStatus = true;
    }

    /**
     * Closes Redis connection.
     */
    public void close() {
        if (this.client == null || this.client.isClosed())
            return;

        try {
            if (this.channelThread != null)
                this.channelThread.interrupt();
            this.client.destroy();
            BardenJavaLibrary.getLogger().info("Redis connection closed.");
        } catch (Exception exception) {
            BardenJavaLibrary.getLogger().error("Couldn't close Redis!", exception);
        }
    }
}
