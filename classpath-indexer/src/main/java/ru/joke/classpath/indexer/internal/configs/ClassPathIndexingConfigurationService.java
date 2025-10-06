package ru.joke.classpath.indexer.internal.configs;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

public final class ClassPathIndexingConfigurationService {

    private static final String CONFIG_LOCATION = "META-INF/classpath-indexing/scanning-resources.conf";

    private final Filer filer;
    private final Messager messager;

    public ClassPathIndexingConfigurationService(
            final Filer filer,
            final Messager messager
    ) {
        this.filer = filer;
        this.messager = messager;
    }

    public Optional<ClassPathIndexingConfiguration> find() {
        try {
            final var fileObject = this.filer.getResource(StandardLocation.CLASS_OUTPUT, "", CONFIG_LOCATION);
            if (fileObject == null || !new File(fileObject.getName()).exists()) {
                this.messager.printMessage(Diagnostic.Kind.NOTE, "Indexer configuration file not found in " + CONFIG_LOCATION);
                return Optional.empty();
            }

            try (final var configStream = fileObject.openInputStream()) {
                return Optional.of(ClassPathIndexingConfiguration.parse(configStream));
            }
        } catch (FileNotFoundException e) {
            return Optional.empty();
        } catch (IOException e) {
            this.messager.printMessage(Diagnostic.Kind.ERROR, "Can't access indexer configuration file: " + CONFIG_LOCATION);
            return Optional.empty();
        }
    }

}
