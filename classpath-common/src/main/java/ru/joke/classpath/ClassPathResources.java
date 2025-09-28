package ru.joke.classpath;

import java.util.Collection;
import java.util.Optional;

public interface ClassPathResources extends Collection<ClassPathResource> {

    Optional<ClassPathResource> first();
}
