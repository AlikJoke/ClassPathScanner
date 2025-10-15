package ru.joke.classpath.scanner;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Scan engine configuration. Specifies settings for creating the engine and controlling its behavior.<br>
 * For convenient creation of a configuration object, use the builder {@link Builder}
 * via the {@link ClassPathScannerEngineConfiguration#builder()}.
 *
 * @param stateful                                Whether the engine should retain state; if the engine is stateful,
 *                                                subsequent requests to the engine will be faster.
 * @param defaultScopeFilter                      A filter for the engine's default scanning scope; allows restricting
 *                                                the search area for the engine, which will then be used for subsequent
 *                                                scan queries. This filter will be implicitly applied to scan requests
 *                                                executed through this engine. Cannot be {@code null}.
 * @param disableDefaultScopeOverride             Whether overriding the engine's default scope should be disabled;
 *                                                if overriding is disabled, this immediately limits the set of
 *                                                resources that will be scanned.
 * @param enableEagerStatefulEngineInitialization Whether eager initialization of a stateful engine is enabled. If eager
 *                                                initialization is enabled, the set of resources for future scanning
 *                                                will be loaded into memory when the engine is created, reducing the
 *                                                time of the first scan request. Otherwise, the data will be loaded
 *                                                into memory only on the first scan request through this engine.
 * @param targetClassLoaders                      The set of class loaders used to load resources from index files
 *                                                in various JARs; cannot be {@code null}.
 *
 * @author Alik
 * @see ClassPathScannerEngine
 * @see Builder
 */
public record ClassPathScannerEngineConfiguration(
        boolean stateful,
        Optional<ClassPathScanner> defaultScopeFilter,
        boolean disableDefaultScopeOverride,
        boolean enableEagerStatefulEngineInitialization,
        Set<ClassLoader> targetClassLoaders
) {

    /**
     * Constructs the configuration object with provided parameters.
     *
     * @param stateful                                whether the engine should retain state; if the engine is stateful,
     *                                                subsequent requests to the engine will be faster.
     * @param defaultScopeFilter                      a filter for the engine's default scanning scope; allows restricting
     *                                                the search area for the engine, which will then be used for subsequent
     *                                                scan queries. This filter will be implicitly applied to scan requests
     *                                                executed through this engine. Cannot be {@code null}.
     * @param disableDefaultScopeOverride             whether overriding the engine's default scope should be disabled;
     *                                                if overriding is disabled, this immediately limits the set of
     *                                                resources that will be scanned.
     * @param enableEagerStatefulEngineInitialization whether eager initialization of a stateful engine is enabled. If eager
     *                                                initialization is enabled, the set of resources for future scanning
     *                                                will be loaded into memory when the engine is created, reducing the
     *                                                time of the first scan request. Otherwise, the data will be loaded
     *                                                into memory only on the first scan request through this engine.
     * @param targetClassLoaders                      the set of class loaders used to load resources from index files
     *                                                in various JARs; cannot be {@code null}.
     */
    public ClassPathScannerEngineConfiguration {
        Objects.requireNonNull(defaultScopeFilter);
        if (targetClassLoaders == null || targetClassLoaders.isEmpty()) {
            throw new InvalidApiUsageException("Class loaders must be not empty");
        }
    }

    /**
     * A static factory method for creating the default configuration.
     *
     * @return default configuration; cannot be {@code null}.
     */
    public static ClassPathScannerEngineConfiguration defaultConfig() {
        return new ClassPathScannerEngineConfiguration(
                false,
                Optional.empty(),
                false,
                false,
                Collections.singleton(ClassPathScannerEngineConfiguration.class.getClassLoader())
        );
    }

    /**
     * Returns a builder instance for constructing a configuration object.<br>
     * The returned object is not thread-safe.
     *
     * @return builder instance; cannot be {@code null}.
     * @see Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for the engine configuration object.
     *
     * @author Alik
     */
    public static class Builder {

        private boolean stateful;
        private ClassPathScanner defaultScopeFilter;
        private boolean disableDefaultScopeOverride;
        private boolean enableEagerStatefulEngineInitialization;
        private Set<ClassLoader> targetClassLoaders = Set.of(getClass().getClassLoader());

        /**
         * Sets the flag that the engine should be stateful.<br>
         * By default, the engine will be stateless.
         *
         * @return this builder instance for further construction; cannot be {@code null}.
         * @see ClassPathScannerEngineConfiguration#stateful()
         */
        public Builder stateful() {
            this.stateful = true;
            return this;
        }

        /**
         * Sets the flag indicating that the engine is stateless.
         *
         * @return this builder instance for further construction; cannot be {@code null}.
         * @see ClassPathScannerEngineConfiguration#stateful()
         */
        public Builder stateless() {
            this.stateful = false;
            return this;
        }

        /**
         * Sets the default scan scope for the engine.
         *
         * @param defaultScopeFilter the default scan scope; cannot be {@code null}.
         * @return this builder instance for further construction; cannot be {@code null}.
         * @see ClassPathScannerEngineConfiguration#defaultScopeFilter()
         */
        public Builder defaultScopeFilter(ClassPathScanner defaultScopeFilter) {
            this.defaultScopeFilter = defaultScopeFilter;
            return this;
        }

        /**
         * Sets a flag that prevents the engine's default scan scope from being overridden in scan requests.
         *
         * @return this builder instance for further construction; cannot be {@code null}.
         * @see ClassPathScannerEngineConfiguration#disableDefaultScopeOverride()
         */
        public Builder disableDefaultScopeOverride() {
            this.disableDefaultScopeOverride = true;
            return this;
        }

        /**
         * Sets a flag that allows the engine's default scan scope to be overridden in scan requests.
         *
         * @return this builder instance for further construction; cannot be {@code null}.
         * @see ClassPathScannerEngineConfiguration#disableDefaultScopeOverride()
         */
        public Builder enableDefaultScopeOverride() {
            this.disableDefaultScopeOverride = false;
            return this;
        }

        /**
         * Sets a flag to use eager initialization of the engine's default search scope when the engine is created.
         *
         * @return this builder instance for further construction; cannot be {@code null}.
         * @see ClassPathScannerEngineConfiguration#enableEagerStatefulEngineInitialization()
         */
        public Builder enableEagerStatefulEngineInitialization() {
            this.enableEagerStatefulEngineInitialization = true;
            return this;
        }

        /**
         * Sets a flag to use lazy initialization of the engine's default search scope.
         *
         * @return this builder instance for further construction; cannot be {@code null}.
         * @see ClassPathScannerEngineConfiguration#enableEagerStatefulEngineInitialization()
         */
        public Builder disableEagerStatefulEngineInitialization() {
            this.enableEagerStatefulEngineInitialization = false;
            return this;
        }

        /**
         * Sets the set of class loaders for loading resources from index files located in various JARs.
         *
         * @param loaders class loaders for loading resources from index files; cannot be {@code null}.
         * @return this builder instance for further construction; cannot be {@code null}.
         * @see ClassPathScannerEngineConfiguration#targetClassLoaders()
         */
        public Builder withClassLoaders(Set<ClassLoader> loaders) {
            if (loaders.isEmpty()) {
                throw new InvalidApiUsageException("Target class loaders can't be empty");
            }

            this.targetClassLoaders = Set.copyOf(loaders);
            return this;
        }

        /**
         * Builds the engine configuration object based on the parameters set in the builder.
         *
         * @return engine configuration based on the builder parameters; cannot be {@code null}.
         */
        public ClassPathScannerEngineConfiguration build() {
            return new ClassPathScannerEngineConfiguration(
                    this.stateful,
                    Optional.ofNullable(this.defaultScopeFilter),
                    this.disableDefaultScopeOverride,
                    this.enableEagerStatefulEngineInitialization,
                    this.targetClassLoaders
            );
        }
    }
}
