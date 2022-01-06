package com.barden.library.database.mongo.pipeline;

import com.barden.library.BardenJavaLibrary;
import com.barden.library.database.DatabaseRepository;
import com.barden.library.scheduler.SchedulerRepository;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Mongo pipeline executor class.
 */
public final class MongoPipelineExecutor {

    private final MongoPipeline pipeline;
    private final Document document = new Document();

    /**
     * Creates mongo pipeline executor object.
     *
     * @param pipeline Mongo pipeline.
     */
    public MongoPipelineExecutor(@Nonnull MongoPipeline pipeline) {
        this.pipeline = Objects.requireNonNull(pipeline, "pipeline cannot be null!");
    }

    /**
     * Gets pipeline.
     *
     * @return Pipeline.
     */
    @Nonnull
    public MongoPipeline getPipeline() {
        return this.pipeline;
    }

    /**
     * Gets document.
     *
     * @return Document.
     */
    @Nonnull
    public Document getDocument() {
        return this.document;
    }

    /**
     * Adds pipeline action.
     *
     * @param keys Keys.
     * @return Mongo pipeline executor builder.
     */
    @Nonnull
    public MongoPipelineExecutor add(@Nonnull Object... keys) {
        //Objects null check.
        Objects.requireNonNull(keys, "key cannot be null!");

        //Loop through keys.
        for (Object key : keys) {
            //If there is no consumer, no need to continue.
            if (!this.pipeline.getConsumers().containsKey(Objects.requireNonNull(key, "key cannot be null!")))
                return this;
            //Triggers consumer.
            this.pipeline.getConsumer(key).accept(new MongoPipeline.Index(this, this.pipeline.getPath(key)));
        }
        return this;
    }

    /**
     * [SET OPERATION]
     *
     * @param path  Path.
     * @param value Value.
     * @return Mongo pipeline executor builder.
     */
    @Nonnull
    public MongoPipelineExecutor setByPath(@Nonnull String path, @Nonnull Object value) {
        this.set().put(Objects.requireNonNull(path, "path cannot be null!"), Objects.requireNonNull(value, "value cannot be null!"));
        return this;
    }

    /**
     * [SET OPERATION]
     *
     * @param key   Key.
     * @param value Value.
     * @return Mongo pipeline executor builder.
     */
    @Nonnull
    public MongoPipelineExecutor setByKey(@Nonnull Object key, @Nonnull Object value) {
        this.set().put(Objects.requireNonNull(this.pipeline.getPath(key), "path cannot be null!"), Objects.requireNonNull(value, "value cannot be null!"));
        return this;
    }

    /**
     * [UNSET OPERATION]
     *
     * @param path Path.
     * @return Mongo pipeline executor builder.
     */
    @Nonnull
    public MongoPipelineExecutor unsetByPath(@Nonnull String path) {
        this.unset().put(Objects.requireNonNull(path, "path cannot be null!"), "");
        return this;
    }

    /**
     * [UNSET OPERATION]
     *
     * @param key Key.
     * @return Mongo pipeline executor builder.
     */
    @Nonnull
    public MongoPipelineExecutor unsetByKey(@Nonnull Object key) {
        this.unset().put(Objects.requireNonNull(this.pipeline.getPath(key), "path cannot be null!"), "");
        return this;
    }

    /**
     * [INCREASE OPERATION]
     *
     * @param path  Path.
     * @param value Value.
     * @return Mongo pipeline executor builder.
     */
    @Nonnull
    public MongoPipelineExecutor increaseByPath(@Nonnull String path, @Nonnull Number value) {
        this.increase().put(Objects.requireNonNull(path, "path cannot be null!"), Objects.requireNonNull(value, "value cannot be null!"));
        return this;
    }

    /**
     * [INCREASE OPERATION]
     *
     * @param key   Key.
     * @param value Value.
     * @return Mongo pipeline executor builder.
     */
    @Nonnull
    public MongoPipelineExecutor increaseByKey(@Nonnull Object key, @Nonnull Number value) {
        this.increase().put(Objects.requireNonNull(this.pipeline.getPath(key), "path cannot be null!"), Objects.requireNonNull(value, "value cannot be null!"));
        return this;
    }

    /**
     * Merges pipeline.
     *
     * @param pipeline_executor Pipeline executor.
     * @return Mongo pipeline document.
     */
    @Nonnull
    public MongoPipelineExecutor merge(@Nonnull MongoPipelineExecutor pipeline_executor) {
        //Objects null check.
        Objects.requireNonNull(pipeline, "pipeline cannot be null!");

        //If pipeline is same, no need to continue.
        if (this.pipeline.getUID().equals(pipeline_executor.getPipeline().getUID())
                || !this.pipeline.getDatabase().equals(pipeline_executor.getPipeline().getDatabase())
                || !this.pipeline.getCollection().equals(pipeline_executor.getPipeline().getCollection())
                || !this.pipeline.getKey().equals(pipeline_executor.getPipeline().getKey())
                || !this.pipeline.getValue().equals(pipeline_executor.getPipeline().getValue()))
            return this;

        //Merges documents. [SET]
        if (pipeline_executor.getDocument().containsKey("$set")) {
            pipeline_executor.getDocument().get("$set", Document.class).forEach((key, value) -> {
                //Inform system that there is a same key for merged document.
                if (this.set().containsKey(key))
                    BardenJavaLibrary.getLogger().warn("Found same key value for key(" + key + ")[SET]!");
                //Sets key and value.
                this.set().put(key, value);
            });
        }

        //Merges documents. [INCREASE]
        if (pipeline_executor.getDocument().containsKey("inc")) {
            pipeline_executor.getDocument().get("$inc", Document.class).forEach((key, value) -> {
                //Inform system that there is a same key for merged document.
                if (this.increase().containsKey(key))
                    BardenJavaLibrary.getLogger().warn("Found same key value for key(" + key + ")[INCREASE]!");
                //Sets key and value.
                this.increase().put(key, value);
            });
        }
        return this;
    }

    /**
     * Executes mongo pipeline.
     *
     * @param async Async status.
     */
    public void execute(boolean async) {
        if (async)
            SchedulerRepository.schedule(task -> this.execute());
        else
            this.execute();
    }

    /**
     * Executes database pipeline.
     */
    public void execute() {
        try {
            //Gets mongo collection.
            MongoCollection<Document> collection = DatabaseRepository.mongo()
                    .getCollection(this.pipeline.getDatabase(), this.pipeline.getCollection());
            //If collection is null, no need to continue
            if (collection == null)
                return;

            //Declare base variables
            Document index_id = new Document(this.pipeline.getKey(), this.pipeline.getValue());

            //Handles updates.
            collection.updateOne(index_id, this.document);
        } catch (Exception exception) {
            BardenJavaLibrary.getLogger().error("Couldn't update mongo pipeline!", exception);
            BardenJavaLibrary.getLogger().error("Latest mongo pipeline error details: " + this);
        }
    }


    /*
    MONGO OPERATIONS
     */

    /**
     * Gets set operation.
     *
     * @return Set operation.
     */
    @Nonnull
    public Document set() {
        //If document contains set, return it.
        if (this.document.containsKey("$set"))
            return this.document.get("$set", Document.class);
        //Adds "set" update type.
        this.document.put("$set", new Document());
        //Gets "set" update type from the document.
        return this.document.get("$set", Document.class);
    }

    /**
     * Gets unset operation.
     *
     * @return Unset operation.
     */
    @Nonnull
    public Document unset() {
        //If document contains set, return it.
        if (this.document.containsKey("$unset"))
            return this.document.get("$unset", Document.class);
        //Adds "unset" update type.
        this.document.put("$unset", new Document());
        //Gets "unset" update type from the document.
        return this.document.get("$unset", Document.class);
    }

    /**
     * Gets increase operation.
     *
     * @return Increase operation.
     */
    @Nonnull
    public Document increase() {
        //If document contains set, return it.
        if (this.document.containsKey("$inc"))
            return this.document.get("$inc", Document.class);
        //Adds "inc" update type.
        this.document.put("$inc", new Document());
        //Gets "inc" update type from the document.
        return this.document.get("$inc", Document.class);
    }


    /*
    MISC
    */

    /**
     * Gets mongo pipeline as a string.
     *
     * @return Mongo pipeline string.
     */
    @Nonnull
    @Override
    public String toString() {
        return "Mongo Pipeline Executor{" +
                "document=" + this.document +
                '}';
    }
}
