package ru.joke.classpath;

import java.util.ArrayList;
import java.util.Optional;

public final class IndexedClassPathResources extends ArrayList<ClassPathResource> implements ClassPathResources {
    @Override
    public Optional<ClassPathResource> first() {
        return isEmpty() ? Optional.empty() : Optional.of(get(0));
    }
}
