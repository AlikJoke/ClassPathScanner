package ru.joke.classpath.scanner.impl.engines;

import ru.joke.classpath.ClassPathResources;
import ru.joke.classpath.scanner.InvalidApiUsageException;
import ru.joke.classpath.services.ClassPathResourcesService;
import ru.joke.classpath.services.IndexedClassPathLocation;
import ru.joke.classpath.scanner.ClassPathScanner;
import ru.joke.classpath.scanner.ClassPathScannerEngineConfiguration;

public final class StatelessClassPathScannerEngine extends AbsClassPathScannerEngine {

    public StatelessClassPathScannerEngine(final ClassPathScannerEngineConfiguration configuration) {
        super(configuration);
    }

    @Override
    public ClassPathResources scan(ClassPathScanner scanner) {
        if (scanner == null) {
            throw new InvalidApiUsageException("Scanner must be not null");
        }

        final var filter = buildFinalFilter(scanner);
        return ClassPathResourcesService.getInstance().read(
                IndexedClassPathLocation.relativeLocation(configuration.targetClassLoaders()),
                filter
        );
    }
}
