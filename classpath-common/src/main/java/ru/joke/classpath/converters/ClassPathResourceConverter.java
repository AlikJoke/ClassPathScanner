package ru.joke.classpath.converters;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.converters.internal.DelegateClassPathResourceConverter;

import java.util.Optional;

/**
 * A converter for a classpath resource to and from its string representation.<br>
 * It allows converting a resource into a string for writing to the index, and converting
 * a resource string from the index back into a resource object.
 *
 * @param <T> concrete type of the classpath resource
 *
 * @author Alik
 *
 * @see ClassPathResource
 * @see Dictionary
 */
public interface ClassPathResourceConverter<T extends ClassPathResource> {

    /**
     * Converts the given resource into a string. During conversion, a lookup table
     * is used, which allows assigning aliases to different parts of the resource
     * to reduce the index size.
     *
     * @param resource resource to convert; cannot be {@code null}.
     * @param dictionary lookup table (dictionary); cannot be {@code null}.
     * @return string representation of the given resource; cannot be {@code null} or empty.
     */
    String toString(T resource, Dictionary dictionary);

    /**
     * Converts a string into a resource object according to the specified lookup table.
     *
     * @param resource string representation of the resource; cannot be {@code null}.
     * @param dictionary lookup table (dictionary); cannot be {@code null}.
     * @return wrapped classpath resource object; cannot be {@code null}.
     */
    Optional<T> fromString(String resource, Dictionary dictionary);

    /**
     * Returns an instance of the converter implementation.
     *
     * @return instance of the converter; cannot be {@code null}.
     */
    static ClassPathResourceConverter<ClassPathResource> getInstance() {
        return new DelegateClassPathResourceConverter();
    }
}
