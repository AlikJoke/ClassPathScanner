package ru.joke.classpath.scanner;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public record ClassPathScannerEngineConfiguration(
        boolean stateful,
        Optional<ClassPathScanner> defaultScopeFilter,
        boolean disableDefaultScopeOverride,
        boolean enableEagerStatefulEngineInitialization,
        Set<ClassLoader> targetClassLoaders
) {

    public static ClassPathScannerEngineConfiguration defaultConfig() {
        return new ClassPathScannerEngineConfiguration(
                false,
                Optional.empty(),
                false,
                false,
                Collections.singleton(ClassPathScannerEngineConfiguration.class.getClassLoader())
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private boolean stateful;
        private ClassPathScanner defaultScopeFilter;
        private boolean disableDefaultScopeOverride;
        private boolean enableEagerStatefulEngineInitialization;
        private Set<ClassLoader> targetClassLoaders = Set.of(getClass().getClassLoader());

        public Builder stateful() {
            this.stateful = true;
            return this;
        }

        public Builder stateless() {
            this.stateful = false;
            return this;
        }

        public Builder defaultScopeFilter(ClassPathScanner defaultScopeFilter) {
            this.defaultScopeFilter = defaultScopeFilter;
            return this;
        }

        public Builder disableDefaultScopeOverride() {
            this.disableDefaultScopeOverride = true;
            return this;
        }

        public Builder enableDefaultScopeOverride() {
            this.disableDefaultScopeOverride = false;
            return this;
        }

        public Builder enableEagerStatefulEngineInitialization() {
            this.enableEagerStatefulEngineInitialization = true;
            return this;
        }

        public Builder disableEagerStatefulEngineInitialization() {
            this.enableEagerStatefulEngineInitialization = false;
            return this;
        }

        public Builder withClassLoaders(Set<ClassLoader> loaders) {
            if (loaders.isEmpty()) {
                throw new IllegalArgumentException("Target class loaders can't be empty");
            }

            this.targetClassLoaders = Set.copyOf(loaders);
            return this;
        }

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
