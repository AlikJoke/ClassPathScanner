package ru.joke.classpath.scanner.impl.engines;

import ru.joke.classpath.scanner.ClassPathScannerBuilder;
import ru.joke.classpath.scanner.ClassPathScannerEngineConfiguration;

abstract sealed class AbsClassPathScannerEngine implements ExtendedClassPathScannerEngine
        permits StatefulClassPathScannerEngine, StatelessClassPathScannerEngine {

    protected final ClassPathScannerEngineConfiguration configuration;

    protected AbsClassPathScannerEngine(final ClassPathScannerEngineConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public ClassPathScannerBuilder createScanner() {
        return ClassPathScannerBuilder.create(this);
    }

    @Override
    public ClassPathScannerEngineConfiguration configuration() {
        return null;
    }
}
