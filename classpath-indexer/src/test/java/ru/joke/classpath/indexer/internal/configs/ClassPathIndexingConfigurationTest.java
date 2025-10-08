package ru.joke.classpath.indexer.internal.configs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassPathIndexingConfigurationTest {

    private final Set<String> expectedAnnotations = Set.of("A1");
    private final Set<String> expectedInterfaces = Set.of("I1", "I2");
    private final Set<String> expectedClasses = Set.of("C1", "C2");
    private final Map<String, Set<String>> expectedAliases = Map.of("K1", Set.of("a1", "a2"));

    @Test
    void testConstructorCreation() {
        final var config = new ClassPathIndexingConfiguration(
                expectedAnnotations,
                expectedInterfaces,
                expectedAliases,
                expectedClasses
        );

        makeChecks(config);
    }

    @Test
    void testFromConfigFileCreation() throws IOException {
        try (final var configStream = getClass().getResourceAsStream("/META-INF/classpath-indexing/scanning-resources.conf")) {
            final var config = ClassPathIndexingConfiguration.parse(configStream);
            makeChecks(config);
        }
    }

    private void makeChecks(final ClassPathIndexingConfiguration config) {
        assertEquals(expectedAnnotations, config.annotations(), "Annotations must be equal");
        assertEquals(expectedInterfaces, config.interfaces(), "Interfaces must be equal");
        assertEquals(expectedClasses, config.classes(), "Classes must be equal");
        assertEquals(expectedAliases, config.aliases(), "Aliases must be equal");
    }
}
