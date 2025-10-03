package ru.joke.classpath.scanner.impl.engines;

import ru.joke.classpath.ClassPathResources;
import ru.joke.classpath.ClassPathResourcesService;
import ru.joke.classpath.IndexedClassPathLocation;
import ru.joke.classpath.scanner.ClassPathScanner;
import ru.joke.classpath.scanner.ClassPathScannerEngineConfiguration;

public final class StatelessClassPathScannerEngine extends AbsClassPathScannerEngine {

    public StatelessClassPathScannerEngine(final ClassPathScannerEngineConfiguration configuration) {
        super(configuration);
    }

    @Override
    public ClassPathResources scan(ClassPathScanner scanner) {
        final var filter = buildFinalFilter(scanner);
        return ClassPathResourcesService.getInstance().read(
                IndexedClassPathLocation.relativeLocation(configuration.targetClassLoaders()),
                filter
        );
    }
}
