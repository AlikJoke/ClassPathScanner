package ru.joke.classpath.scanner;

import ru.joke.classpath.scanner.impl.engines.StatefulClassPathScannerEngine;
import ru.joke.classpath.scanner.impl.engines.StatelessClassPathScannerEngine;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ClassPathScannerEngines {

    private static final ClassPathScannerEngine defaultEngine = new StatelessClassPathScannerEngine(ClassPathScannerEngineConfiguration.defaultConfig());
    private static final Map<String, ClassPathScannerEngine> engines = new ConcurrentHashMap<>();

    public static ClassPathScannerEngine getDefaultEngine() {
        return defaultEngine;
    }

    public static Optional<ClassPathScannerEngine> getEngine(final String id) {
        return Optional.ofNullable(engines.get(id));
    }

    public static ClassPathScannerEngine createEngine(
            final String id,
            final ClassPathScannerEngineConfiguration configuration
    ) {
        return engines.computeIfAbsent(
                id,
                k -> configuration.stateful()
                        ? new StatefulClassPathScannerEngine(configuration)
                        : new StatelessClassPathScannerEngine(configuration)
                );
    }

    public static boolean destroyEngine(final String id) {
        return engines.remove(id) != null;
    }

    private ClassPathScannerEngines() {}
}
