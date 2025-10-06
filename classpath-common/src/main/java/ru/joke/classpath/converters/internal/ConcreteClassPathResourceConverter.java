package ru.joke.classpath.converters.internal;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.converters.ClassPathResourceConverter;

public interface ConcreteClassPathResourceConverter<T extends ClassPathResource> extends ClassPathResourceConverter<T> {

    ClassPathResource.Type supportedType();
}
