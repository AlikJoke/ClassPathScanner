package ru.joke.classpath.converters;

import ru.joke.classpath.ClassPathResource;

import java.util.Optional;

public interface ClassPathResourceConverter<T extends ClassPathResource> {

    String toString(T resource);

    Optional<T> fromString(String resource);
}
