package ru.joke.classpath.indexer.internal.configs;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ScannedResourcesTest {

    @Test
    void testAddingResources() {
        final var resources = new ScannedResources();
        final var expectedAnnotations = Set.of("A1", "A2");
        final var expectedInterfaces = Set.of("I1", "I2");
        final var expectedClasses = Set.of("C1", "C2");

        final var expectedAliases = Map.of(
                "K1", Set.of("a1", "a2"),
                "K2", Set.of("a3")
        );

        resources.annotations().addAll(expectedAnnotations);
        resources.interfaces().addAll(expectedInterfaces);
        resources.classes().addAll(expectedClasses);
        resources.aliases().putAll(expectedAliases);

        assertEquals(expectedAnnotations, resources.annotations());
        assertNotSame(expectedAnnotations, resources.annotations());

        assertEquals(expectedInterfaces, resources.interfaces());
        assertNotSame(expectedInterfaces, resources.interfaces());

        assertEquals(expectedClasses, resources.classes());
        assertNotSame(expectedClasses, resources.classes());

        assertEquals(expectedAliases, resources.aliases());
        assertNotSame(expectedAliases, resources.aliases());
    }

    @Test
    void testEmptiness() {
        final var resources = new ScannedResources();
        assertTrue(resources.isEmpty(), "Resources must be empty");

        resources.annotations().add("A1");
        assertFalse(resources.isEmpty(), "Resources must not be empty");

        resources.annotations().clear();
        assertTrue(resources.isEmpty(), "Resources must be empty");

        resources.interfaces().add("I1");
        assertFalse(resources.isEmpty(), "Resources must not be empty");

        resources.interfaces().clear();
        assertTrue(resources.isEmpty(), "Resources must be empty");

        resources.classes().add("C1");
        assertFalse(resources.isEmpty(), "Resources must not be empty");

        resources.classes().clear();
        assertTrue(resources.isEmpty(), "Resources must be empty");

        resources.aliases().put("K1", Set.of("a1"));
        assertFalse(resources.isEmpty(), "Resources must not be empty");

        resources.aliases().clear();
        assertTrue(resources.isEmpty(), "Resources must be empty");
    }

    @Test
    void testFillFromIndexingConfiguration() {
        final var resources = new ScannedResources();
        final var config = new ClassPathIndexingConfiguration(
                Set.of("A1"),
                Set.of("I1", "I2"),
                Map.of("K1", Set.of("a1", "a2")),
                Set.of("C1", "C2")
        );

        resources.fillFrom(config);

        assertEquals(config.annotations(), resources.annotations(), "Annotations must be equal");
        assertEquals(config.interfaces(), resources.interfaces(), "Interfaces must be equal");
        assertEquals(config.classes(), resources.classes(), "Classes must be equal");
        assertEquals(config.aliases(), resources.aliases(), "Aliases must be equal");
    }
}
