<p align="center">
  <img src="https://user-images.githubusercontent.com/45916622/181322541-230809a1-6997-46f5-9392-b4b6a94e88b9.png" width="256">
</p>

## What is Barden Java Library?

Barden Java Library is library software developed for use by the developers in the team.
It is used by @obyvante to develop Roblox games in the past. Still, it has useful default Java libraries you can use for
any kind of project.

## Getting started

### Building

To get started with Barden Java Library, download the source code and run the following commands:

```bash
./gradlew clean build
```

Then, if there are no errors, you can run the following command to publish the library to Maven Local:

```bash
gradle publishToMavenLocal
```

### Dependency

To depend on the library for Gradle, you can use the following dependency:

```gradle
implementation 'com.barden:barden-java-library:1.0'
```

To depend on the library for Maven, you can use the following dependency:

```maven
<dependency>
  <groupId>com.barden</groupId>
  <artifactId>barden-java-library</artifactId>
  <version>1.0</version>
</dependency>
```

### Initializing

To initialize the library, you must use static method `BardenJavaLibrary.initialize()`:

```
BardenJavaLibrary.initialize();
```

### Unitializing

Unitializing the library is important for async schedulers and tasks.

To unitialize the library, you must use static method `BardenJavaLibrary.uninitialize()`:

```
BardenJavaLibrary.terminate();
```

## Library

### Database

To connect and use databases future, you only have to fill generated file named **database.toml**(you can find in
resources folder).
There are total of four database providers:

#### MongoDB

You can take a look at inside the MongoProvider class to see methods. There is only one special case you
should be familiar with, **MongoDB Database Structure**.

If you have a class object, and you want to save specific properties of the class, you can use the following example:

#### Example of MongoDB Database Structure

```java
public enum BookBsonField implements DatabaseField<Book> {
    UID("uid", true),
    COVER("cover");

    private final String path;
    private final boolean query;

    BookBsonField(@NotNull String path) {
        this(path, false);
    }

    BookBsonField(@NotNull String path, boolean query) {
        this.path = Objects.requireNonNull(path);
        this.query = query;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public String getPath() {
        return this.path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isQuery() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public BsonValue toBsonValue(@NotNull Book book) {
        return switch (this) {
            case UID -> new BsonBinary(book.getUID());
            case COVER -> new BsonInt32(book.getCover());
        };
    }
}
```

```java
public final class Book implements DatabaseObject<Book, BookBsonField> {
    private final UUID uid;
    private final String title;
    private final String author;
    private int cover;
    private final BookDatabase database;

    public Book(@NotNull UUID uid, @NotNull String title, @NotNull String author, int cover) {
        this.uid = Objects.requireNonNull(uid);
        this.title = Objects.requireNonNull(title);
        this.author = Objects.requireNonNull(author);
        this.cover = cover;
        this.database = new BookDatabase(this);
    }

    @NotNull
    public UUID getUID() {
        return this.uid;
    }

    @NotNull
    public String getTitle() {
        return this.title;
    }

    @NotNull
    public String getAuthor() {
        return this.author;
    }

    public int getCover() {
        return this.cover;
    }

    public void setCover(int cover) {
        this.cover = cover;
    }


    /*
    OVERRIDES
     */

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public DatabaseStructure<Book, BookBsonField> getDatabase() {
        return this.database;
    }
}
```

```java
public final class BookDatabase extends DatabaseStructure<Book, BookBsonField> {

    public static final Provider PROVIDER = new Provider();

    public static final class Provider extends DatabaseMongoProvider {
        public Provider() {
            super("my-mongodb-collection", "books");
        }
    }

    public BookDatabase(@NotNull Book book) {
        super(book, BookBsonField.class, BookDatabase.PROVIDER);
    }
}
```

#### Redis, InfluxDB and Timescale

You can take a look at inside their provider classes to see methods.

```
Book book= new Book(UUID.randomUUID(),"The Great Gatsby","F. Scott Fitzgerald",1);
        book.setCover(2);
        book.getDatabase().saveAsync(BookBsonField.COVER); // syncrhonous save exist.
```

### Event

Basically, events are used to notify the developer about certain things. You can think as a subscriber, listener and
publisher pipeline architecture.

### Example of Event

```java
public class TestEvent extends Event {
    private final long id;

    public TestEvent(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }
}
```

```java
//Listens for event. (Subscribes to event.)
EventEditor<TestEvent> eventEditor=EventRepository.of(TestEvent.class)
        .filter(event->event.getId()==1)
        .consume(event->System.out.println(event.getId()));

//Publishes event. (Emits event.)
EventRepository.execute(new TestEvent(1));

//Unsubscribes from event. (Unsubscribes from event.)
eventEditor.unregister();
```

### Metadata

With metadata, you can store information about anything like Maps. The difference is that metadata has unique features
such as expirable keys, consumers, and getters etc.

### Example of Metadata

```java
Metadata metadata=new Metadata();

metadata.set("key","value");

metadata.set(1,(Function<Integer, Integer>)x->x*2);
Function<Integer, Integer> test=metadata.getNonNull(1);
System.out.println(test.apply(2)); //4

metadata.set("my expirable key",0,TimeUnit.SECONDS,5);

metadata.set("important task",true,TimeUnit.MINUTES,1,_metadata->{
    System.out.println("important task expired!");
    Optional.ofNullable(_metadata.get("important task")).ifPresent(System.out::println);
});
```

### Scheduler

Scheduler is developed due to overwhelming Java OS-based executor thread pools. If you even want to create a basic
syntax with it, it'll be complicated in the end of the day. To avoid this, you can use the scheduler. It's a simple
scheduler that can be used to schedule tasks.

### Example of Scheduler

```java
// Asyncrhonous scheduler task.
SchedulerProvider.schedule(task->{
    int counter=task.metadata().get("counter",0);
    task.metadata().set("counter",counter+1);

    if(counter==10){
        task.cancel();
    }
});

// Scheduler task builder.
SchedulerProvider.create()
        .after(5,TimeUnit.SECONDS)
        .every(1,TimeUnit.SECONDS)
        .schedule(task->{
            System.out.println("It works after 5 seconds and repeat for every 1 second!-");
        });

// Blocking asyncronous scheduler task. It will block the current thread until the task is finished.
/SchedulerProvider.create()
        .block()
        .schedule(task->{
            try{
                Thread.sleep(5000);
            }catch(Exception e){
                e.printStackTrace();
            }
        });
```

