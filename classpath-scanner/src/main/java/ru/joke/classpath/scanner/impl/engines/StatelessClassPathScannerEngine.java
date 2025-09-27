package ru.joke.classpath.scanner.impl.engines;

import ru.joke.classpath.ClassPathResourcesService;
import ru.joke.classpath.scanner.ClassPathScannerEngineConfiguration;

public final class StatelessClassPathScannerEngine extends AbsClassPathScannerEngine {

    public StatelessClassPathScannerEngine(final ClassPathScannerEngineConfiguration configuration) {
        super(configuration);
    }

    @Override
    public ClassPathResourcesService resourcesService() {
        return ClassPathResourcesService.getInstance();
    }
}
