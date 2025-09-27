package ru.joke.classpath;

public interface ConcreteClassPathResourceConverter<T extends ClassPathResource> extends ClassPathResourceConverter<T> {

    ClassPathResource.Type supportedType();
}
