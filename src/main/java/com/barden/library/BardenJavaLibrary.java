package com.barden.library;

import com.barden.library.database.DatabaseProvider;
import com.barden.library.scheduler.SchedulerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * Barden java library class.
 */
public final class BardenJavaLibrary {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerProvider.class);
    private static final SchedulerProvider scheduler = new SchedulerProvider();

    /**
     * Initializes barden java library.
     */
    public static void initialize() {
        //Initializes database repository.
        DatabaseProvider.initialize();
    }

    /**
     * Terminates barden java library.
     */
    public static void terminate() {
        try {
            BardenJavaLibrary.scheduler.shutdown();
        } catch (Exception exception) {
            SchedulerProvider.getLogger().error("Couldn't shutdown scheduler!", exception);
        }
    }

    /**
     * Gets path.
     *
     * @return Path.
     */
    @Nonnull
    public static String getPath() {
        try {
            return new File(BardenJavaLibrary.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
        } catch (Exception exception) {
            return "";
        }
    }

    /**
     * Gets logger.
     *
     * @return Logger.
     */
    @Nonnull
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Gets scheduler.
     *
     * @return Barden scheduler.
     */
    @NonNull
    public static SchedulerProvider getScheduler() {
        return scheduler;
    }
}
