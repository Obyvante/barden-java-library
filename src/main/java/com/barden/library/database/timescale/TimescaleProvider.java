package com.barden.library.database.timescale;

import com.barden.library.BardenJavaLibrary;

import javax.annotation.Nonnull;
import java.sql.*;
import java.util.Objects;

/**
 * Timescale provider class.
 */
public final class TimescaleProvider {

    private boolean initialized = false;

    private final String host;
    private final int port;

    private final String username;
    private final String password;

    private final String database;

    private Connection connection;

    /**
     * Create a postgresql connection and provider.
     *
     * @param host     Host or IP address of TimescaleDB server.
     * @param port     Port address of TimescaleDB server.
     * @param username Username.
     * @param password Password.
     */
    public TimescaleProvider(@Nonnull String host, int port, @Nonnull String username, @Nonnull String password, @Nonnull String database) {
        this.host = Objects.requireNonNull(host, "host cannot be null");
        this.port = port;
        this.username = Objects.requireNonNull(username, "username cannot be null!");
        this.password = Objects.requireNonNull(password, "password cannot be null!");
        this.database = Objects.requireNonNull(database, "database cannot be null!");

        //Declares required fields.
        var URL = "jdbc:postgresql://" + this.host + ":" + this.port + "/" + this.database + "?user=" + this.username + "&password=" + this.password;

        try {
            this.connection = DriverManager.getConnection(URL);
        } catch (Exception exception) {
            BardenJavaLibrary.getLogger().error("Couldn't connect to TimescaleDB!", exception);
            return;
        }

        //Logging.
        BardenJavaLibrary.getLogger().info("Successfully connected to TimescaleDB! (" + this.host + ":" + this.port + ")");

        //Initialization.
        this.initialized = true;
    }

    /**
     * Gets if TimescaleDB initialized or not.
     *
     * @return If TimescaleDB initialized or not.
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
     * Gets username.
     *
     * @return Username.
     */
    @Nonnull
    public String getUsername() {
        return this.username;
    }

    /**
     * Gets password.
     *
     * @return Password.
     */
    @Nonnull
    public String getPassword() {
        return this.password;
    }

    /**
     * Gets database.
     *
     * @return Database.
     */
    @Nonnull
    public String getDatabase() {
        return this.database;
    }

    /**
     * Gets connection.
     *
     * @return Connection.
     */
    @Nonnull
    public Connection getConnection() {
        return this.connection;
    }

    /**
     * Creates a statement.
     *
     * @return Statement.
     */
    @Nonnull
    public Statement session() throws SQLException {
        return this.connection.createStatement();
    }

    /**
     * Prepares a statement.
     *
     * @return Statement.
     */
    @Nonnull
    public PreparedStatement prepare(@Nonnull String sql) throws SQLException {
        return this.connection.prepareStatement(sql);
    }
}
