package ru.joke.classpath;

import java.util.List;
import java.util.Optional;

public interface ClassPathResources extends List<ClassPathResource> {

    Optional<ClassPathResource> first();
}
