package ru.joke.classpath;

import java.util.HashSet;
import java.util.Optional;

public final class IndexedClassPathResources extends HashSet<ClassPathResource> implements ClassPathResources {
    @Override
    public Optional<ClassPathResource> first() {
        return isEmpty() ? Optional.empty() : Optional.of(iterator().next());
    }
}
