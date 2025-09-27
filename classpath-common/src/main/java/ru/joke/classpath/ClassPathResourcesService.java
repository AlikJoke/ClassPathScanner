package ru.joke.classpath;

import java.util.ServiceLoader;
import java.util.function.Predicate;

public interface ClassPathResourcesService {

    void write(IndexedClassPathLocation targetLocation, ClassPathResources resources);

    ClassPathResources read(IndexedClassPathLocation sourceLocation, Predicate<ClassPathResource> filter);

    static ClassPathResourcesService getInstance() {
        return ServiceLoader.load(ClassPathResourcesService.class).findFirst().orElseThrow();
    }
}
