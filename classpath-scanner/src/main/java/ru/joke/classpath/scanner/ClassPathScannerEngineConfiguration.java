package ru.joke.classpath.scanner;

import java.util.Optional;

public record ClassPathScannerEngineConfiguration(
        boolean stateful,
        Optional<ClassPathScanner> defaultScopeFilter,
        boolean disableDefaultScopeOverride
) {

    public static ClassPathScannerEngineConfiguration defaultConfig() {
        return new ClassPathScannerEngineConfiguration(false, Optional.empty(), false);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private boolean stateful;
        private ClassPathScanner defaultScopeFilter;
        private boolean disableDefaultScopeOverride;

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

        public ClassPathScannerEngineConfiguration build() {
            return new ClassPathScannerEngineConfiguration(
                    this.stateful,
                    Optional.ofNullable(this.defaultScopeFilter),
                    this.disableDefaultScopeOverride
            );
        }
    }
}
