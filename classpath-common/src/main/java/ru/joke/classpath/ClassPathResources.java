package ru.joke.classpath;

import java.util.Collection;
import java.util.Optional;

/**
 * A collection container of classpath resources that have been previously indexed or are to be indexed.<br>
 * The order of elements in the collection depends on the implementation.
 *
 * @author Alik
 * @see ClassPathResource
 * @see IndexedClassPathResources
 */
public interface ClassPathResources extends Collection<ClassPathResource> {

    /**
     * Returns an arbitrary element from the collection (order is nondeterministic and based on the implementation).
     *
     * @return element from the collection wrapped in {@link Optional}; if the collection is empty,
     * {@link Optional#empty()} will be returned.
     */
    Optional<ClassPathResource> any();
}
