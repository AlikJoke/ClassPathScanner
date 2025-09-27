package ru.joke.classpath.scanner.impl;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ClassPathResources;
import ru.joke.classpath.IndexedClassPathLocation;
import ru.joke.classpath.scanner.ClassPathScanner;
import ru.joke.classpath.scanner.impl.engines.ExtendedClassPathScannerEngine;

import java.util.function.Predicate;

final class PredicateBasedClassPathScanner implements ClassPathScanner {

    private final Predicate<ClassPathResource> predicate;
    private final ExtendedClassPathScannerEngine engine;

    PredicateBasedClassPathScanner(
            final Predicate<ClassPathResource> predicate,
            final ExtendedClassPathScannerEngine engine
    ) {
        this.engine = engine;
        this.predicate = predicate;
    }

    @Override
    public ClassPathResources scan() {
        return engine.resourcesService().read(IndexedClassPathLocation.relativeLocation(), this.predicate);
    }
}
