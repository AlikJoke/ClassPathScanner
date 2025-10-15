package ru.joke.classpath.indexer.internal.configs;

import ru.joke.classpath.IndexedClassPathException;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A service for serializing/deserializing scanned resources collected across different annotation processor rounds.
 *
 * @author Alik
 * @see ScannedResources
 */
public final class ScannedResourcesConfigurationService {

    private static final String CONFIG_TEMP_EXT = ".sr";

    private final Messager messager;
    private final File targetOutputConfigDir;

    /**
     * Constructs service with specified parameters.
     *
     * @param targetOutputConfigDir directory to store serialized data; cannot be {@code null}.
     * @param messager              logger; cannot be {@code null}.
     */
    public ScannedResourcesConfigurationService(
            final File targetOutputConfigDir,
            final Messager messager
    ) {
        this.targetOutputConfigDir = Objects.requireNonNull(targetOutputConfigDir, "targetOutputConfigDir");
        this.messager = Objects.requireNonNull(messager, "messager");
    }

    /**
     * Serializes the given object containing resources and writes it to disk.
     *
     * @param scannedResources resources to serialize; cannot be {@code null}.
     * @see ScannedResources
     */
    public void serialize(final ScannedResources scannedResources) {
        final var config = new File(this.targetOutputConfigDir, UUID.randomUUID() + CONFIG_TEMP_EXT);
        try (final var fileOutputStream = new FileOutputStream(config);
             final var objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            objectOutputStream.writeObject(scannedResources);
        } catch (IOException ex) {
            throw new IndexedClassPathException(ex);
        }
    }

    /**
     * Deserializes existing resources in the specified directory into {@link ScannedResources} objects.
     *
     * @return deserialized resources; cannot be {@code null}.
     * @see ScannedResources
     */
    public Set<ScannedResources> deserializeAllAvailable() {

        final var scannedResourcesFiles = this.targetOutputConfigDir.listFiles(f -> f.getName().endsWith(CONFIG_TEMP_EXT));
        if (scannedResourcesFiles != null) {
            return Arrays.stream(scannedResourcesFiles)
                            .map(this::deserialize)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
        }

        return Collections.emptySet();
    }

    private ScannedResources deserialize(final File file) {
        try (final var fileInputStream = new FileInputStream(file)) {
             final var objectInputStream = new ObjectInputStream(fileInputStream);
            return (ScannedResources) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            this.messager.printMessage(Diagnostic.Kind.ERROR, "Unable to deserialize data from file %s: %s".formatted(file.getAbsolutePath(), getExceptionString(e)));
            return null;
        }
    }

    private String getExceptionString(final Exception ex) {
        final var writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
