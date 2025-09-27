package ru.joke.classpath;

import java.util.Optional;

public interface ClassPathResourceConverter<T extends ClassPathResource> {

    String toString(T resource);

    Optional<T> fromString(String resource);
}
