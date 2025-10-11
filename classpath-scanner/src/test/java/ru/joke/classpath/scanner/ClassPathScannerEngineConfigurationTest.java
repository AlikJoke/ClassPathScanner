package ru.joke.classpath.scanner;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ClassPathScannerEngineConfigurationTest {

    @Test
    void testDefaultConfig() {
        final var config = ClassPathScannerEngineConfiguration.defaultConfig();

        makeChecks(
                config,
                false,
                false,
                false,
                Set.of(getClass().getClassLoader())
        );
        assertTrue(config.defaultScopeFilter().isEmpty(), "Scope filter must be not set");
    }

    @Test
    void testDirectConfigCreation() {
        final var defaultScope = ClassPathScanner.builder().begin().all().end().build();
        final var expectedClassLoaders = Set.of(getClass().getClassLoader(), ClassLoader.getPlatformClassLoader());
        final var config = new ClassPathScannerEngineConfiguration(
                true,
                Optional.of(defaultScope),
                true,
                true,
                expectedClassLoaders
        );

        makeChecks(
                config,
                true,
                true,
                true,
                expectedClassLoaders
        );

        assertTrue(config.defaultScopeFilter().isPresent(), "Default scope filter must be set");
        assertEquals(defaultScope, config.defaultScopeFilter().get(), "Default scope filter must be equal");
    }

    @Test
    void testFailedConfigCreationByBuilder() {
        assertThrows(InvalidApiUsageException.class, () ->
            ClassPathScannerEngineConfiguration.builder()
                                                    .stateful()
                                                    .enableEagerStatefulEngineInitialization()
                                                    .enableDefaultScopeOverride()
                                                    .withClassLoaders(Set.of())
                                               .build()
        );
    }

    @Test
    void testConfigCreationByBuilder() {
        final var expectedClassLoaders = Set.of(getClass().getClassLoader(), ClassLoader.getPlatformClassLoader());
        final var defaultScope = ClassPathScanner.builder().begin().all().end().build();
        final var config =
                ClassPathScannerEngineConfiguration.builder()
                                                        .defaultScopeFilter(defaultScope)
                                                        .disableDefaultScopeOverride()
                                                        .disableEagerStatefulEngineInitialization()
                                                        .stateless()
                                                        .withClassLoaders(expectedClassLoaders)
                                                    .build();

        makeChecks(
                config,
                false,
                true,
                false,
                expectedClassLoaders
        );

        assertTrue(config.defaultScopeFilter().isPresent(), "Default scope filter must be set");
        assertEquals(defaultScope, config.defaultScopeFilter().get(), "Default scope filter must be equal");
    }

    private void makeChecks(
            final ClassPathScannerEngineConfiguration config,
            final boolean shouldBeStateful,
            final boolean shouldBeDisabledDefaultScopeOverride,
            final boolean shouldBeEnabledEagerStatefulEngineInit,
            final Set<ClassLoader> expectedClassLoaders
    ) {
        assertNotNull(config, "Config must be not null");
        assertEquals(shouldBeStateful, config.stateful(), "Engine state must be equal");
        assertEquals(shouldBeDisabledDefaultScopeOverride, config.disableDefaultScopeOverride(), "Overriding of the default scope must be equal");
        assertNotNull(config.defaultScopeFilter(), "Scope filter wrapper must be not null always");
        assertEquals(shouldBeEnabledEagerStatefulEngineInit, config.enableEagerStatefulEngineInitialization(), "Eager engine init must be equal");
        assertNotNull(config.targetClassLoaders(), "Target class loaders must be not null");
        assertEquals(expectedClassLoaders, config.targetClassLoaders(), "Classloaders must be equal");
    }
}
