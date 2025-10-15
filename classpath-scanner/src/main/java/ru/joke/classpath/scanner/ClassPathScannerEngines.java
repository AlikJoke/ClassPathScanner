package ru.joke.classpath.scanner;

import ru.joke.classpath.scanner.internal.engines.StatefulClassPathScannerEngine;
import ru.joke.classpath.scanner.internal.engines.StatelessClassPathScannerEngine;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entry point for working with the Scanner API.<br>
 * It allows managing scan engines, from creating and retrieving them by id to destroying them.<br>
 * A singleton default engine ({@link ClassPathScannerEngines#getDefaultEngine()} is available,
 * independently of creating new ones. This engine is stateless, meaning it does not store indexed data in memory,
 * which consequently reduces scanning speed.<br>
 * This class is thread-safe.
 *
 * @author Alik
 * @see ClassPathScannerEngine
 * @see ClassPathScannerEngineConfiguration
 */
public abstract class ClassPathScannerEngines {

    private static final ClassPathScannerEngine defaultEngine = new StatelessClassPathScannerEngine(ClassPathScannerEngineConfiguration.defaultConfig());
    private static final Map<String, ClassPathScannerEngine> engines = new ConcurrentHashMap<>();

    /**
     * Returns the default singleton scanner engine.<br>
     * This engine is stateless.
     *
     * @return default singleton engine; cannot be {@code null}.
     * @see ClassPathScannerEngineConfiguration#defaultConfig()
     */
    public static ClassPathScannerEngine getDefaultEngine() {
        return defaultEngine;
    }

    /**
     * Returns the engine by its identifier.<br>
     * If no engine with the given identifier exists, {@link Optional#empty()} is returned.
     *
     * @param id identifier of the engine; must be a non-empty string.
     * @return wrapped engine; cannot be {@code null}.
     *
     * @see #createEngine(String, ClassPathScannerEngineConfiguration)
     * @see #destroyEngine(String)
     */
    public static Optional<ClassPathScannerEngine> getEngine(final String id) {
        return Optional.ofNullable(engines.get(checkEngineId(id)));
    }

    /**
     *  Creates an engine with the specified identifier and configuration, and returns it.
     *  If an engine with the given identifier already exists, the method will return that existing engine.
     *
     * @param id            identifier of the engine; must be a non-empty string.
     * @param configuration engine configuration; cannot be {@code null}.
     * @return created engine; cannot be {@code null}.
     *
     * @see ClassPathScannerEngineConfiguration
     */
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

    /**
     * Destroys the engine with the specified identifier, if it exists.
     *
     * @param id identifier of the engine; must be a non-empty string.
     * @return {@code true} if the engine existed and was destroyed; {@code false} otherwise.
     */
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
