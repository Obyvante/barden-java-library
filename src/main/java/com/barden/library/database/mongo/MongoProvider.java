package com.barden.library.database.mongo;

import com.barden.library.BardenJavaLibrary;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Objects;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * Mongo provider class.
 */
public final class MongoProvider {

    private boolean initialized = false;

    private final String host;
    private final int port;

    private final String username;
    private final String password;

    private final String auth;

    //MongoDB
    private final MongoClient client;
    private final MongoClientSettings clientSettings;

    /**
     * Create MongoDB connection and provider class with sync-driver
     *
     * @param host     Host or IP address of MongoDB server.
     * @param port     Port address of MongoDB server.
     * @param username (Optional) Username password.
     * @param password (Optional) MongoDB password.
     * @param auth     (Optional) Auth type, it's mostly "admin".
     */
    public MongoProvider(@Nonnull String host, int port, @Nonnull String username, @Nonnull String password, @Nonnull String auth) {
        this.host = Objects.requireNonNull(host, "host cannot be null");
        this.port = port;
        this.username = Objects.requireNonNull(username, "username cannot be null!");
        this.password = Objects.requireNonNull(password, "password cannot be null!");
        this.auth = Objects.requireNonNull(auth, "auth cannot be null!");

        //Configures codec registry.
        CodecRegistry pojo_codec_provider = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codec_registry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojo_codec_provider);

        //Creates client settings builder.
        MongoClientSettings.Builder settings_builder = MongoClientSettings.builder()
                .applyToClusterSettings(builder -> builder.hosts(Collections.singletonList(new ServerAddress(this.host, this.port))))
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .codecRegistry(codec_registry);

        //Handles authorization.
        if (!this.username.isEmpty())
            settings_builder.credential(MongoCredential.createCredential(this.username, this.auth, this.password.toCharArray()));
        else
            BardenJavaLibrary.getLogger().warn("You are not using any username and password for MongoDB server.");

        //Builds client settings.
        this.clientSettings = settings_builder.build();

        //Creates mongo client with declared settings.
        this.client = MongoClients.create(this.clientSettings);

        //Pings to the database server to make sure it is working.
        if (!this.ping())
            return;

        //Logging.
        BardenJavaLibrary.getLogger().info("Successfully connected to MongoDB! (" + this.host + ":" + this.port + ")");

        //Initialization.
        this.initialized = true;
    }

    /**
     * Connection test.
     * Pings MongoDB to fetch it is working or not.
     */
    private boolean ping() {
        try {
            this.client.getDatabase("admin").runCommand(new Document("ping", 1));
        } catch (Exception exception) {
            BardenJavaLibrary.getLogger().error("Couldn't ping MongoDB!", exception);
            //Initialization.
            this.initialized = false;
            return false;
        }
        return true;
    }

    /**
     * Gets if MongoDB initialized or not.
     *
     * @return If MongoDB initialized or not.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Gets host.
     *
     * @return Host.
     */
    @Nonnull
    public String getHost() {
        return host;
    }

    /**
     * Gets port.
     *
     * @return Port.
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets username.
     *
     * @return Username.
     */
    @Nonnull
    public String getUsername() {
        return username;
    }

    /**
     * Gets password.
     *
     * @return Password.
     */
    @Nonnull
    public String getPassword() {
        return password;
    }

    /**
     * Gets MongoDB auth type.
     * By default, it is "admin"
     *
     * @return Auth type.
     */
    @Nonnull
    public String getAuth() {
        return auth;
    }

    /**
     * Gets MongoDB connected client.
     *
     * @return Client.
     */
    @Nonnull
    public MongoClient getClient() {
        return client;
    }

    /**
     * Gets MongoDB client settings.
     *
     * @return Client settings.
     */
    @Nonnull
    public MongoClientSettings getClientSettings() {
        return clientSettings;
    }

    /**
     * Creates index for collection.
     *
     * @param database   Database name.
     * @param collection Collection name.
     * @param index      Index content.
     * @param options    Index options as IndexOptions class.
     */
    public void createIndex(@Nonnull String database, @Nonnull String collection, @Nonnull Bson index, @Nonnull IndexOptions options) {
        //Objects null check.
        Objects.requireNonNull(database, "database cannot be null!");
        Objects.requireNonNull(collection, "collection cannot be null!");
        Objects.requireNonNull(index, "index cannot be null!");
        Objects.requireNonNull(options, "options cannot be null!");

        //Collection mongo.
        MongoCollection<Document> mongo_collection = getCollection(database, collection);
        if (mongo_collection == null)
            return;

        //Create index.
        mongo_collection.createIndex(index, options);
    }

    /**
     * Gets database.
     *
     * @param database Database name.
     * @return Returns selected database as MongoDatabase.
     */
    @Nullable
    public MongoDatabase getDatabase(@Nonnull String database) {
        Objects.requireNonNull(database, "database cannot be null!");

        //If client is null, no need to continue.
        if (this.client == null)
            return null;

        //Tries to get database.
        try {
            return this.client.getDatabase(database);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * @param database   Database name.
     * @param collection Collection name.
     * @return Returns selected collection as MongoCollection.
     */
    @Nullable
    public MongoCollection<Document> getCollection(@Nonnull String database, @Nonnull String collection) {
        //Objects null check.
        Objects.requireNonNull(database, "database cannot be null!");
        Objects.requireNonNull(collection, "collection cannot be null!");

        //Database check.
        MongoDatabase controlled_database = this.getDatabase(database);
        if (controlled_database == null)
            return null;

        //Tries to return collection.
        try {
            return controlled_database.getCollection(collection);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Gets collection.
     *
     * @param <T>            Collection object type.
     * @param database       Database name.
     * @param collection     Collection name.
     * @param collectionType Collection type.
     * @return Returns selected collection as MongoCollection.
     */
    @Nullable
    public <T> MongoCollection<T> getCollection(@Nonnull String database, @Nonnull String collection, @Nonnull Class<T> collectionType) {
        //Objects null check.
        Objects.requireNonNull(database, "database cannot be null!");
        Objects.requireNonNull(collection, "collection cannot be null!");
        Objects.requireNonNull(collectionType, "collection type cannot be null!");

        //Database check.
        MongoDatabase controlled_database = this.getDatabase(database);
        if (controlled_database == null)
            return null;

        //Tries to return collection.
        try {
            return controlled_database.getCollection(collection, collectionType);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
