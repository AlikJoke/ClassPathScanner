package ru.joke.classpath.services;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ClassPathResources;
import ru.joke.classpath.services.internal.DefaultClassPathResourcesService;

import java.util.function.Predicate;

/**
 * A service for working with indexable classpath resources. It provides an API for writing to and reading from the index.
 *
 * @author Alik
 * @see IndexedClassPathLocation
 * @see ClassPathResource
 * @see ClassPathResources
 */
public interface ClassPathResourcesService {

    /**
     * Writes classpath resources to the index at the specified location.
     *
     * @param targetLocation specified location of the index; cannot be {@code null}.
     * @param resources resources to write; cannot be {@code null}.
     */
    void write(IndexedClassPathLocation targetLocation, ClassPathResources resources);

    /**
     * Reads classpath resources from the index at the specified location.
     *
     * @param sourceLocation specified location; cannot be {@code null}.
     * @param filter filter of the required resources; cannot be {@code null}.
     * @return resources from the index matched to the filter; cannot be {@code null}.
     */
    ClassPathResources read(IndexedClassPathLocation sourceLocation, Predicate<ClassPathResource> filter);

    /**
     * Returns an instance of the service implementation.
     *
     * @return instance of the service implementation; cannot be {@code null}.
     */
    static ClassPathResourcesService getInstance() {
        return new DefaultClassPathResourcesService();
    }
}
