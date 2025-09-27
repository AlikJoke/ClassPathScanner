package ru.joke.classpath.scanner.impl.engines;

import ru.joke.classpath.ClassPathResourcesService;
import ru.joke.classpath.scanner.ClassPathScannerEngineConfiguration;

public final class StatefulClassPathScannerEngine extends AbsClassPathScannerEngine {

    private final ClassPathResourcesService resourcesService;

    public StatefulClassPathScannerEngine(final ClassPathScannerEngineConfiguration configuration) {
        super(configuration);
        this.resourcesService = ClassPathResourcesService.getInstance();
    }

    @Override
    public ClassPathResourcesService resourcesService() {
        return this.resourcesService;
    }
}
