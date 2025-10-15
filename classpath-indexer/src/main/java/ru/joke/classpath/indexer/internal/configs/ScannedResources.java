package ru.joke.classpath.indexer.internal.configs;

import ru.joke.classpath.ClassPathResource;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A holder of scanned resources.<br>
 * Scanned resources refer to all annotations, interfaces, and classes whose descendants (subclasses or implementations)
 * are to be included in the index. It also allows for specifying additional resources (with identifiers formatted
 * according to {@link ClassPathResource#id()}) along with their aliases, which must also be included in the index.
 *
 * @author Alik
 * @see ScannedResourcesConfigurationService
 */
public final class ScannedResources implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Set<String> annotations = new HashSet<>();
    private final Set<String> interfaces = new HashSet<>();
    private final Set<String> classes = new HashSet<>();
    private final Map<String, Set<String>> aliases = new HashMap<>();

    /**
     * Returns all scanned annotations.
     *
     * @return annotations; cannot be {@code null}.
     */
    public Set<String> annotations() {
        return annotations;
    }

    /**
     * Returns all scanned interfaces.
     *
     * @return interfaces; cannot be {@code null}.
     */
    public Set<String> interfaces() {
        return interfaces;
    }

    /**
     * Returns all scanned classes.
     *
     * @return classes; cannot be {@code null}.
     */
    public Set<String> classes() {
        return classes;
    }

    /**
     * Returns all scanned aliased resources.
     *
     * @return aliases resources; cannot be {@code null}.
     */
    public Map<String, Set<String>> aliases() {
        return aliases;
    }

    /**
     * Checks that scanned resources is empty.
     *
     * @return {@code true} if no scanned resources collected; {@code false} otherwise.
     */
    public boolean isEmpty() {
        return this.annotations.isEmpty() && this.interfaces.isEmpty() && this.classes.isEmpty() && this.aliases.isEmpty();
    }

    /**
     * Fills holder from the user-provided indexing configuration (via configuration file).
     *
     * @param indexingConfiguration user-provided indexing configuration; cannot be {@code null}.
     * @see ClassPathIndexingConfiguration
     */
    public void fillFrom(final ClassPathIndexingConfiguration indexingConfiguration) {
        this.annotations.addAll(indexingConfiguration.annotations());
        this.interfaces.addAll(indexingConfiguration.interfaces());
        this.classes.addAll(indexingConfiguration.classes());
        this.aliases.putAll(indexingConfiguration.aliases());
    }
}
