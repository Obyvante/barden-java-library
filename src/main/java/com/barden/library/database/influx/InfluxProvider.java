package com.barden.library.database.influx;

import com.barden.library.BardenJavaLibrary;
import com.influxdb.client.*;
import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.Task;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Influx provider class.
 */
public final class InfluxProvider {

    private boolean initialized = false;

    private final String host;
    private final int port;

    private final String token;
    private final String organization;

    private final String bucket;

    private final InfluxDBClient client;
    private final WriteApi writeApi;

    /**
     * Create influx connection and provider class with sync-driver.
     *
     * @param host         Host or IP address of InfluxDB server.
     * @param port         Port address of InfluxDB server.
     * @param token        Token.
     * @param organization Organization.
     * @param bucket       Bucket.
     */
    public InfluxProvider(@Nonnull String host, int port, @Nonnull String token, @Nonnull String organization, @Nonnull String bucket) {
        this.host = Objects.requireNonNull(host, "host cannot be null");
        this.port = port;
        this.token = Objects.requireNonNull(token, "token cannot be null!");
        this.organization = Objects.requireNonNull(organization, "organization cannot be null!");
        this.bucket = Objects.requireNonNull(bucket, "bucket cannot be null!");

        //Creates influxdb client options.
        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .url("http://" + this.host + ":" + this.port)
                .authenticateToken(token.toCharArray())
                .org(null)
                .bucket(null)
                .okHttpClient(new OkHttpClient.Builder()
                        .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                        .readTimeout(30, TimeUnit.SECONDS)
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(true))
                .build();

        //Creates client connection.
        this.client = InfluxDBClientFactory.create(options);

        //Makes write api.
        this.writeApi = this.client.makeWriteApi(WriteOptions.builder().build());

        //Pings to the database server to make sure it is working.
        if (!this.client.ping()) {
            BardenJavaLibrary.getLogger().error("Couldn't ping influx!");
            return;
        }

        //Logging.
        BardenJavaLibrary.getLogger().info("Successfully connected to InfluxDB! (" + this.host + ":" + this.port + ")");

        //Initialization.
        this.initialized = true;
    }

    /**
     * Gets if InfluxDB initialized or not.
     *
     * @return If InfluxDB initialized or not.
     */
    public boolean isInitialized() {
        return this.initialized;
    }

    /**
     * Gets host.
     *
     * @return Host.
     */
    @Nonnull
    public String getHost() {
        return this.host;
    }

    /**
     * Gets port.
     *
     * @return Port.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Gets token.
     *
     * @return Token.
     */
    @Nonnull
    public String getToken() {
        return this.token;
    }

    /**
     * Gets organization.
     *
     * @return Organization.
     */
    @Nonnull
    public String getOrganization() {
        return this.organization;
    }

    /**
     * Gets organization id.
     *
     * @return Organization id.
     */
    @Nonnull
    public String getOrganizationId() {
        return this.getOrganizationByName(this.organization).getId();
    }

    /**
     * Gets bucket.
     *
     * @return Bucket.
     */
    @Nonnull
    public String getBucket() {
        return this.bucket;
    }

    /**
     * Gets connected client.
     *
     * @return InfluxDB Client.
     */
    @Nonnull
    public InfluxDBClient getClient() {
        return this.client;
    }

    /**
     * Gets write api.
     *
     * @return Write API.
     */
    @Nonnull
    public WriteApi getWriteAPI() {
        return this.writeApi;
    }

    /**
     * Gets write api blocking.
     * Create a new synchronous blocking Write client.
     *
     * @return Write API Blocking.
     */
    @Nonnull
    public WriteApiBlocking getWriteAPIBlocking() {
        return this.client.getWriteApiBlocking();
    }

    /**
     * Finds bucket by its name.
     *
     * @param name Bucket name.
     * @return Optional bucket.
     */
    @Nonnull
    public Optional<Bucket> findBucketByName(@Nonnull String name) {
        return Optional.ofNullable(this.client.getBucketsApi().findBucketByName(Objects.requireNonNull(name, "bucket name cannot be null!")));
    }

    /**
     * Gets bucket by its name.
     *
     * @param name Bucket name.
     * @return Bucket.
     */
    @Nonnull
    public Bucket getBucketByName(@Nonnull String name) {
        return this.findBucketByName(name).orElseThrow(() -> new NullPointerException("bucket(" + name + ") cannot be null!"));
    }

    /**
     * Finds organization by its name.
     *
     * @param name Organization name.
     * @return Optional organization.
     */
    @Nonnull
    public Optional<Organization> findOrganizationByName(@Nonnull String name) {
        return this.client.getOrganizationsApi().findOrganizations().stream()
                .filter(org -> org.getName().equals(Objects.requireNonNull(name, "organization name cannot be null!")))
                .findFirst();
    }

    /**
     * Gets organization by its name.
     *
     * @param name Organization name.
     * @return Organization.
     */
    @Nonnull
    public Organization getOrganizationByName(@Nonnull String name) {
        return this.findOrganizationByName(name).orElseThrow(() -> new NullPointerException("organization(" + name + ") cannot be null!"));
    }

    /**
     * Finds task by its name.
     *
     * @param name Task name.
     * @return Optional task.
     */
    @Nonnull
    public Optional<Task> findTaskByName(@Nonnull String name) {
        return this.client.getTasksApi().findTasks().stream()
                .filter(org -> org.getName().equals(Objects.requireNonNull(name, "task name cannot be null!")))
                .findFirst();
    }

    /**
     * Gets task by its name.
     *
     * @param name Task name.
     * @return Task.
     */
    @Nonnull
    public Task getTaskByName(@Nonnull String name) {
        return this.findTaskByName(name).orElseThrow(() -> new NullPointerException("task(" + name + ") cannot be null!"));
    }
}
