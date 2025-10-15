package ru.joke.classpath.scanner.internal.engines;

import org.junit.jupiter.api.Test;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.IndexedClassPathResources;
import ru.joke.classpath.scanner.ClassPathScanner;
import ru.joke.classpath.scanner.ClassPathScannerEngine;
import ru.joke.classpath.scanner.ClassPathScannerEngineConfiguration;
import ru.joke.classpath.scanner.internal.PredicateBasedClassPathScanner;
import ru.joke.classpath.services.ClassPathResourcesService;
import ru.joke.classpath.services.IndexedClassPathLocation;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class StatelessClassPathScannerEngineTest extends AbsClassPathScannerEngineTest {

    @Test
    void testScanWhenNoDefaultScopeProvided() {
        executeTest(
                ClassPathScannerEngineConfiguration.builder(),
                false
        );
    }

    @Test
    void testScanWhenDefaultScopeProvidedAndOverridingDisabled() {
        final var defaultScope = createDefaultEngineScope();

        executeTest(
                ClassPathScannerEngineConfiguration.builder()
                                                        .defaultScopeFilter(defaultScope)
                                                        .disableDefaultScopeOverride(),
                false
        );
    }

    @Test
    void testScanWhenDefaultScopeProvidedAndOverridingEnabledInConfigAndScannerOverridesDefaultEngine() {
        final var defaultScope = createDefaultEngineScope();

        executeTest(
                ClassPathScannerEngineConfiguration.builder()
                                                        .defaultScopeFilter(defaultScope)
                                                        .enableDefaultScopeOverride(),
                true
        );
    }

    @Test
    void testScanWhenDefaultScopeProvidedAndOverridingEnabledInConfigAndScannerDoesNotOverridesDefaultEngine() {
        final var defaultScope = createDefaultEngineScope();

        executeTest(
                ClassPathScannerEngineConfiguration.builder()
                                                        .defaultScopeFilter(defaultScope)
                                                        .enableDefaultScopeOverride(),
                false
        );
    }

    @Override
    protected ClassPathScannerEngine createEngine(ClassPathScannerEngineConfiguration.Builder configurationBuilder) {
        return new StatelessClassPathScannerEngine(configurationBuilder.stateless().build());
    }

    private PredicateBasedClassPathScanner createDefaultEngineScope() {
        return (PredicateBasedClassPathScanner) ClassPathScanner.builder()
                                                                    .begin()
                                                                        .excludeResourceType(ClassPathResource.Type.CLASS)
                                                                    .end()
                                                                 .build();
    }

    private void executeTest(
            ClassPathScannerEngineConfiguration.Builder configBuilder,
            boolean overrideDefaultEngineScopeInScanner
    ) {
        final var targetClassLoaders = Set.of(getClass().getClassLoader());
        final var engine = createEngine(
                configBuilder.withClassLoaders(targetClassLoaders)
        );

        try (final var mockedStatic = mockStatic(ClassPathResourcesService.class, UUID.randomUUID().toString())) {
            final var mockResourcesService = mock(ClassPathResourcesService.class);
            mockedStatic.when(ClassPathResourcesService::getInstance).thenReturn(mockResourcesService);

            final var scanner =
                    (PredicateBasedClassPathScanner) ClassPathScanner.builder()
                            .begin(overrideDefaultEngineScopeInScanner)
                            .all()
                            .end()
                            .build();
            final var wrappedScanner = new WrappedScanner(scanner);

            final var expectedLocation = IndexedClassPathLocation.relativeLocation(targetClassLoaders);
            final var expectedResources = new IndexedClassPathResources();
            when(mockResourcesService.read(eq(expectedLocation), any())).thenReturn(expectedResources);

            final var result = engine.scan(wrappedScanner);

            assertSame(expectedResources, result, "Resources must be same");
            verify(mockResourcesService).read(expectedLocation, wrappedScanner.finalScanner);
        }
    }
}
