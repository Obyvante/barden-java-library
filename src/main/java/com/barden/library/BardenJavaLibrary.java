package com.barden.library;

import com.barden.library.database.DatabaseRepository;
import com.barden.library.scheduler.SchedulerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * Barden java library class.
 */
public final class BardenJavaLibrary {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerRepository.class);
    private static final SchedulerRepository scheduler = new SchedulerRepository();

    /**
     * Initializes barden java library.
     */
    public static void initialize() {
        //Initializes database repository.
        DatabaseRepository.initialize();
    }

    /**
     * Terminates barden java library.
     */
    public static void terminate() {
        try {
            BardenJavaLibrary.scheduler.shutdown();
        } catch (Exception exception) {
            SchedulerRepository.getLogger().error("Couldn't shutdown scheduler!", exception);
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
    public static SchedulerRepository getScheduler() {
        return scheduler;
    }
}
