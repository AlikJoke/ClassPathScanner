package ru.joke.classpath.scanner;

import ru.joke.classpath.ClassPathResources;

/**
 * A representation of a scan engine.<br>
 * It provides the scanning API and access to the engine's configuration.<br>
 * The {@link ClassPathScannerEngines} class exists for creating and managing the engine's lifecycle.<br>
 * All implementations of this interface must be thread-safe.
 *
 * @author Alik
 * @see ClassPathScannerEngines
 * @see ClassPathScannerEngine
 * @see ClassPathScannerEngineConfiguration
 * @see ClassPathResources
 */
public interface ClassPathScannerEngine {

    /**
     * Scans classpath resources based on criteria passed via a scanner object.
     *
     * @param scanner scanner; cannot be {@code null}.
     * @return classpath resources that match the scanner's query; cannot be {@code null}.
     * @see ClassPathResources
     * @see ClassPathScanner
     */
    ClassPathResources scan(ClassPathScanner scanner);

    /**
     * Returns this engine configuration.
     *
     * @return engine configuration; cannot be {@code null}.
     * @see ClassPathScannerEngineConfiguration
     */
    ClassPathScannerEngineConfiguration configuration();

    /**
     * Reloads the engine state (does nothing for stateless engines).
     */
    void reload();
}
