package ru.joke.classpath.scanner.impl;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ClassPathResources;
import ru.joke.classpath.ClassPathResourcesService;
import ru.joke.classpath.IndexedClassPathLocation;
import ru.joke.classpath.scanner.ClassPathQuery;

import java.util.function.Predicate;

final class PredicateBasedClassPathQuery implements ClassPathQuery {

    private final Predicate<ClassPathResource> predicate;

    PredicateBasedClassPathQuery(Predicate<ClassPathResource> predicate) {
        this.predicate = predicate;
    }

    @Override
    public ClassPathResources search() {
        return ClassPathResourcesService.getInstance().read(IndexedClassPathLocation.relativeLocation(), this.predicate);
    }
}
