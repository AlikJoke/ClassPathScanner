package ru.joke.classpath.services;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ClassPathResources;
import ru.joke.classpath.services.internal.DefaultClassPathResourcesService;

import java.util.function.Predicate;

public interface ClassPathResourcesService {

    void write(IndexedClassPathLocation targetLocation, ClassPathResources resources);

    ClassPathResources read(IndexedClassPathLocation sourceLocation, Predicate<ClassPathResource> filter);

    static ClassPathResourcesService getInstance() {
        return new DefaultClassPathResourcesService();
    }
}
