package ru.joke.classpath.scanner.impl.engines;

import ru.joke.classpath.ClassPathResources;
import ru.joke.classpath.ClassPathResourcesService;
import ru.joke.classpath.IndexedClassPathLocation;
import ru.joke.classpath.IndexedClassPathResources;
import ru.joke.classpath.scanner.ClassPathScanner;
import ru.joke.classpath.scanner.ClassPathScannerEngineConfiguration;

import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class StatefulClassPathScannerEngine extends AbsClassPathScannerEngine {

    private final Supplier<ClassPathResources> scannedResourcesAccessor;

    public StatefulClassPathScannerEngine(final ClassPathScannerEngineConfiguration configuration) {
        super(configuration);
        this.scannedResourcesAccessor = new DefaultEngineScopeLoader();
    }

    @Override
    public ClassPathResources scan(ClassPathScanner scanner) {
        final var filter = buildFinalFilter(scanner);
        final var resources = this.scannedResourcesAccessor.get();
        return resources
                .stream()
                .filter(filter)
                .collect(Collectors.toCollection(IndexedClassPathResources::new));
    }

    private class DefaultEngineScopeLoader implements Supplier<ClassPathResources> {

        private volatile ClassPathResources resources;

        @Override
        public ClassPathResources get() {
            if (this.resources == null) {
                synchronized (this) {
                    if (this.resources == null) {
                        this.resources = findResourcesInDefaultScope();
                    }
                }
            }

            return resources;
        }

        private ClassPathResources findResourcesInDefaultScope() {
            final var resourcesService = ClassPathResourcesService.getInstance();
            final var initScopeFilter =
                    configuration.defaultScopeFilter()
                            .filter(f -> configuration.disableDefaultScopeOverride())
                            .orElseGet(this::buildScannerForAllResources);

            return resourcesService.read(IndexedClassPathLocation.relativeLocation(), checkScanner(initScopeFilter));
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
