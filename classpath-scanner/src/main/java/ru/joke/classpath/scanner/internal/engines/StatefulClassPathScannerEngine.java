package ru.joke.classpath.scanner.internal.engines;

import ru.joke.classpath.ClassPathResources;
import ru.joke.classpath.IndexedClassPathResources;
import ru.joke.classpath.scanner.ClassPathScanner;
import ru.joke.classpath.scanner.ClassPathScannerEngineConfiguration;
import ru.joke.classpath.scanner.InvalidApiUsageException;
import ru.joke.classpath.services.ClassPathResourcesService;
import ru.joke.classpath.services.IndexedClassPathLocation;

import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Stateful scan engine implementation.<br>
 * The engine stores the resource index in memory after the first search to accelerate subsequent queries.
 * If the engine is configured for eager initialization, the index will be scanned upon engine creation,
 * rather than during the first search.
 *
 * @author Alik
 * @see ru.joke.classpath.scanner.ClassPathScannerEngine
 */
public final class StatefulClassPathScannerEngine extends AbsClassPathScannerEngine {

    private final DefaultEngineScopeLoader scannedResourcesAccessor;

    /**
     * Constructs stateful scan engine with provided configuration.
     *
     * @param configuration engine configuration; cannot be {@code null}.
     * @see ClassPathScannerEngineConfiguration
     */
    public StatefulClassPathScannerEngine(final ClassPathScannerEngineConfiguration configuration) {
        super(configuration);
        this.scannedResourcesAccessor = new DefaultEngineScopeLoader();
        if (configuration.enableEagerStatefulEngineInitialization()) {
            this.scannedResourcesAccessor.get();
        }
    }

    @Override
    public ClassPathResources scan(ClassPathScanner scanner) {
        if (scanner == null) {
            throw new InvalidApiUsageException("Scanner must be not null");
        }

        final var filter = buildFinalFilter(scanner);
        final var resources = this.scannedResourcesAccessor.get();
        return resources
                .stream()
                .filter(filter)
                .collect(Collectors.toCollection(IndexedClassPathResources::new));
    }

    @Override
    public void reload() {
        synchronized (this.scannedResourcesAccessor) {
            this.scannedResourcesAccessor.resources = null;
        }
        if (this.configuration.enableEagerStatefulEngineInitialization()) {
            this.scannedResourcesAccessor.get();
        }
    }

    private class DefaultEngineScopeLoader implements Supplier<ClassPathResources> {

        private volatile ClassPathResources resources;

        @Override
        public ClassPathResources get() {
            ClassPathResources result;
            if ((result = this.resources) == null) {
                synchronized (this) {
                    if ((result = this.resources) == null) {
                        this.resources = result = findResourcesInDefaultScope();
                    }
                }
            }

            return result;
        }

        private ClassPathResources findResourcesInDefaultScope() {
            final var resourcesService = ClassPathResourcesService.getInstance();
            final var initScopeFilter =
                    configuration.defaultScopeFilter()
                            .filter(f -> configuration.disableDefaultScopeOverride())
                            .orElseGet(this::buildScannerForAllResources);

            return resourcesService.read(
                    IndexedClassPathLocation.relativeLocation(configuration.targetClassLoaders()),
                    checkScanner(initScopeFilter)
            );
        }

        private ClassPathScanner buildScannerForAllResources() {
            return ClassPathScanner.builder()
                                        .begin()
                                            .all()
                                        .end()
                                    .build();
        }
    }
}
