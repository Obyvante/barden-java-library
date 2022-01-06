package com.barden.library.file;

import com.barden.library.BardenJavaLibrary;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlParser;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;

/**
 * Toml file loader class.
 */
public final class TomlFileLoader {

    /**
     * Gets file.
     *
     * @return Optional TOML commented config.
     */
    @Nonnull
    public static Optional<CommentedConfig> getConfig(@Nonnull String name, boolean useDefault) {
        //Gets file.
        File file = new File(Objects.requireNonNull(name, "name cannot be null!") + ".toml");

        //If file is not exists.
        if (!file.exists()) {
            //If no use for default file, no need to continue.
            if (!useDefault)
                return Optional.empty();

            //Handles exception.
            try (InputStream input = TomlFileLoader.class.getResourceAsStream("/" + name + ".toml")) {
                //Handles input.
                if (input != null)
                    Files.copy(input, file.toPath());
                else
                    file.createNewFile();
                return Optional.of(new TomlParser().parse(new FileInputStream(file)));
            } catch (Exception exception) {
                BardenJavaLibrary.getLogger().error("Couldn't default load TOML file(" + name + ")!", exception);
            }

            //Returns empty.
            return Optional.empty();
        }

        //Handles exception.
        try {
            return Optional.of(new TomlParser().parse(new FileInputStream(file)));
        } catch (Exception exception) {
            BardenJavaLibrary.getLogger().error("Couldn't load TOML file(" + name + ")!", exception);
        }

        //Returns empty.
        return Optional.empty();
    }


}
