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
        return Optional.ofNullable(engines.get(checkEngineId(id)));
    }

    public static ClassPathScannerEngine createEngine(
            final String id,
            final ClassPathScannerEngineConfiguration configuration
    ) {
        if (configuration == null) {
            throw new InvalidApiUsageException("Configuration of engine must be not null");
        }

        return engines.computeIfAbsent(
                checkEngineId(id),
                k -> configuration.stateful()
                        ? new StatefulClassPathScannerEngine(configuration)
                        : new StatelessClassPathScannerEngine(configuration)
                );
    }

    public static boolean destroyEngine(final String id) {
        return engines.remove(checkEngineId(id)) != null;
    }

    private static String checkEngineId(final String id) {
        if (id == null || id.isEmpty()) {
            throw new InvalidApiUsageException("Id of engine must be not empty");
        }

        return id;
    }

    private ClassPathScannerEngines() {}
}
