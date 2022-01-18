package com.barden.library.database.influx;

import com.barden.library.BardenJavaLibrary;
import com.influxdb.client.*;

import javax.annotation.Nonnull;
import java.util.Objects;

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

        //Creates client connection.
        this.client = InfluxDBClientFactory.create("http://" + this.host + ":" + this.port, token.toCharArray(), organization, bucket);
        //Configures client.
        this.client.enableGzip();

        //Makes write api.
        this.writeApi = this.client.makeWriteApi(WriteOptions.builder().build());

        //Pings to the database server to make sure it is working.
        if (!this.client.ping())
            return;

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
}
