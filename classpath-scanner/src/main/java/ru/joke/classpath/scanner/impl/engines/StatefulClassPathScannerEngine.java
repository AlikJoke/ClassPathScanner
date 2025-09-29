package ru.joke.classpath.scanner.impl.engines;

import ru.joke.classpath.ClassPathResources;
import ru.joke.classpath.ClassPathResourcesService;
import ru.joke.classpath.IndexedClassPathLocation;
import ru.joke.classpath.scanner.ClassPathScanner;
import ru.joke.classpath.scanner.ClassPathScannerEngineConfiguration;

public final class StatefulClassPathScannerEngine extends AbsClassPathScannerEngine {

    private final ClassPathResourcesService resourcesService;

    public StatefulClassPathScannerEngine(final ClassPathScannerEngineConfiguration configuration) {
        super(configuration);
        this.resourcesService = ClassPathResourcesService.getInstance();
    }

    @Override
    public ClassPathResources scan(ClassPathScanner scanner) {
        final var filter = buildFinalFilter(scanner);
        return this.resourcesService.read(IndexedClassPathLocation.relativeLocation(), filter);
    }
}
