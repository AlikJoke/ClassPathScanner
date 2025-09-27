package ru.joke.classpath.converters;

import ru.joke.classpath.ClassPathResource;

public interface ConcreteClassPathResourceConverter<T extends ClassPathResource> extends ClassPathResourceConverter<T> {

    ClassPathResource.Type supportedType();
}
