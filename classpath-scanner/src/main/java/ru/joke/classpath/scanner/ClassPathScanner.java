package ru.joke.classpath.scanner;

import ru.joke.classpath.ClassPathResources;

/**
 * Classpath resource scanner. Allows scanning previously indexed classpath resources and searching
 * them for resources that match the scanning criteria specified by the API user.<br>
 * Example of API usage:
 * <pre>
 *     ClassPathScanner.builder()
 *                          .begin()
 *                              .includeClassKind(ClassResource.Kind.INTERFACE)
 *                              .and()
 *                              .withAlias("test")
 *                          .end()
 *                          .and()
 *                              .not()
 *                                  .hasModifier(ClassPathResource.Modifier.PUBLIC)
 *                          .end()
 *                     .build()
 *                     .scan(engine);
 * </pre>
 *
 * @author Alik
 * @see ClassPathScannerEngine
 * @see ClassPathScannerEngines
 */
public interface ClassPathScanner {

    /**
     * Scans the classpath using the default engine.
     *
     * @return resources that match criteria; cannot be {@code null}.
     * @see ClassPathResources
     * @see ClassPathScannerEngines#getDefaultEngine()
     */
    default ClassPathResources scan() {
        return scan(ClassPathScannerEngines.getDefaultEngine());
    }

    /**
     * Scans the classpath using the provided engine.
     *
     * @param engine provided engine to scan resources; cannot be {@code null}.
     * @return resources that match criteria; cannot be {@code null}.
     * @see ClassPathResources
     * @see ClassPathScannerEngine
     */
    default ClassPathResources scan(ClassPathScannerEngine engine) {
        if (engine == null) {
            throw new InvalidApiUsageException("Scanner engine must be provided");
        }

        return engine.scan(this);
    }

    /**
     * Whether the scanner should override the default scope set by the engine (if one is defined).
     * If the engine configuration forbids overriding the default scope, this setting will have no effect.
     *
     * @return {@code true} if the scanner should override the default scope of the engine; {@code false} otherwise.
     * @see ClassPathScannerEngineConfiguration#disableDefaultScopeOverride()
     * @see ClassPathScannerEngineConfiguration#defaultScopeFilter()
     */
    boolean overrideDefaultEngineScope();

    /**
     * Returns a builder instance for constructing a scanner object.<br>
     * The returned object is not thread-safe.
     *
     * @return builder instance; cannot be {@code null}.
     * @see ClassPathScannerBuilder
     */
    static ClassPathScannerBuilder builder() {
        return ClassPathScannerBuilder.create();
    }
}
