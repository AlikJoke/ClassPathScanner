package ru.joke.classpath;

import java.util.HashSet;
import java.util.Optional;

/**
 * Default implementation of the {@link ClassPathResource} based on the {@link HashSet}.
 *
 * @author Alik
 * @see ClassPathResource
 * @see ClassPathResources
 * @see HashSet
 */
public final class IndexedClassPathResources extends HashSet<ClassPathResource> implements ClassPathResources {
    @Override
    public Optional<ClassPathResource> any() {
        return isEmpty() ? Optional.empty() : Optional.of(iterator().next());
    }
}
