package ru.joke.classpath.scanner.internal.engines;

import ru.joke.classpath.ClassPathResources;
import ru.joke.classpath.scanner.InvalidApiUsageException;
import ru.joke.classpath.services.ClassPathResourcesService;
import ru.joke.classpath.services.IndexedClassPathLocation;
import ru.joke.classpath.scanner.ClassPathScanner;
import ru.joke.classpath.scanner.ClassPathScannerEngineConfiguration;

/**
 * Stateless scan engine implementation.<br>
 * The engine does not store any data in memory.
 *
 * @author Alik
 * @see ru.joke.classpath.scanner.ClassPathScannerEngine
 */
public final class StatelessClassPathScannerEngine extends AbsClassPathScannerEngine {

    /**
     * Constructs stateless scan engine with provided configuration.
     *
     * @param configuration engine configuration; cannot be {@code null}.
     * @see ClassPathScannerEngineConfiguration
     */
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
