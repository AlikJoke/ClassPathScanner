package ru.joke.classpath.services;

import java.util.Objects;
import java.util.Set;

/**
 * An abstraction of the location of the index of classpath resources.
 *
 * @author Alik
 * @see ClassPathResourcesService
 */
public interface IndexedClassPathLocation {

    /**
     * Relative path to the directory containing configuration data and the index.
     */
    String CONFIGURATION_DIR = "META-INF/classpath-indexing/";
    /**
     * Relative path to the index file.
     */
    String INDEXED_RESOURCES_FILE = CONFIGURATION_DIR + "indexed-resources.index";

    /**
     * Returns the index file location as a string.
     *
     * @return file location; cannot be {@code null}.
     */
    String getLocation();

    /**
     * Returns the set of class loaders used to assemble all indexes (from all JAR files included in the distribution).
     *
     * @return class loaders; cannot be {@code null}.
     */
    default Set<ClassLoader> getTargetClassLoaders() {
        return Set.of(getClass().getClassLoader());
    }

    /**
     * Returns a relative reference to the index location for the specified set of class loaders.
     *
     * @param targetClassLoaders set of class loaders; cannot be {@code null} or empty.
     * @return reference to the index location; cannot be {@code null}.
     */
    static IndexedClassPathLocation relativeLocation(Set<ClassLoader> targetClassLoaders) {
        return new IndexedClassPathLocation() {
            @Override
            public String getLocation() {
                return INDEXED_RESOURCES_FILE;
            }

            @Override
            public Set<ClassLoader> getTargetClassLoaders() {
                return targetClassLoaders;
            }

            @Override
            public int hashCode() {
                return Objects.hash(getLocation(), getTargetClassLoaders());
            }

            @Override
            public boolean equals(Object obj) {
                return obj == this
                        || obj instanceof IndexedClassPathLocation l
                                && Objects.equals(l.getLocation(), getLocation())
                                && Objects.equals(l.getTargetClassLoaders(), getTargetClassLoaders());
            }
        };
    }
}
