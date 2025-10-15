package ru.joke.classpath.indexer.internal.configs;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

/**
 * A service for managing resource indexing configuration, provided by the library consumer by creating a file
 * in the consumer module at {@link ClassPathIndexingConfigurationService#CONFIG_LOCATION}, containing information
 * about resources whose representations and derived resources (subclasses, implementations, etc.)
 * are to be included in the index.<br>
 * The configuration file structure should be as follows:
 * <pre>
 * #annotations
 * ru.joke.test.TestAnnotation1
 * ru.joke.test.TestAnnotation2
 *
 * #classes
 * ru.joke.test.TestAbstractClass
 * #interfaces
 * ru.joke.test.TestInterface
 *
 * #aliases
 * ru.joke.test/ru.joke.test.TestInterface#field:fieldA;fieldB
 * </pre>
 * Four tags are reserved:
 * <ul>
     * <li>#annotations</li>
     * <li>#classes</li>
     * <li>#interfaces</li>
     * <li>#aliases</li>
 * </ul>
 * Each tag must be followed by a list of resources of the corresponding type.<br>
 * <ul>
 *     <li>For annotations, both the annotations themselves and all resources annotated by them are indexed
 *      (this rule also applies recursively to annotations that are themselves annotated by the specified annotation).</li>
 *      <li>For classes, both the classes themselves and their subclasses are indexed (the rule applies recursively to subclasses).</li>
 *      <li>For interfaces, the interfaces themselves are indexed, as well as extending interfaces and all implementing
 *      classes (the same recursion rule applies).</li>
 *      <li>For aliases, only the resources themselves listed under this tag are indexed.</li>
 * </ul>
 *
 * @author Alik
 * @see ClassPathIndexingConfiguration
 */
public final class ClassPathIndexingConfigurationService {

    static final String CONFIG_LOCATION = "META-INF/classpath-indexing/scanning-resources.conf";

    private final Filer filer;
    private final Messager messager;

    /**
     * Constructs the configuration service.
     *
     * @param filer    filer to provide access to configuration files; cannot be {@code null}.
     * @param messager logger; cannot be {@code null}.
     */
    public ClassPathIndexingConfigurationService(
            final Filer filer,
            final Messager messager
    ) {
        this.filer = filer;
        this.messager = messager;
    }

    /**
     * Returns the indexing configuration settings from the configuration file, if one exists.
     *
     * @return indexing configuration; cannot be {@code null}.
     * @see ClassPathIndexingConfiguration
     */
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
