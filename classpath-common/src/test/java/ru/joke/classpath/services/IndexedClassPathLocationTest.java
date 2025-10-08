package ru.joke.classpath.services;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class IndexedClassPathLocationTest {

    @Test
    void testSimpleLocationCreation() {
        final var targetLocation = "test-location";
        final IndexedClassPathLocation location = () -> targetLocation;

        assertEquals(targetLocation, location.getLocation(), "Location must be equal");
        assertEquals(1, location.getTargetClassLoaders().size(), "Loaders count must be equal");
        assertEquals(IndexedClassPathLocation.class.getClassLoader(), location.getTargetClassLoaders().iterator().next(), "Loader must be equal");
    }

    @Test
    void testRelativeLocationCreation() {
        final var loader1 = mock(ClassLoader.class);
        final var loader2 = mock(ClassLoader.class);
        final var location = IndexedClassPathLocation.relativeLocation(Set.of(loader1, loader2));

        assertEquals(IndexedClassPathLocation.INDEXED_RESOURCES_FILE, location.getLocation(), "Location must be equal");
        assertEquals(2, location.getTargetClassLoaders().size(), "Loaders count must be equal");
        assertTrue(location.getTargetClassLoaders().contains(loader1), "Loader must present in collection of loaders");
        assertTrue(location.getTargetClassLoaders().contains(loader2), "Loader must present in collection of loaders");
    }
}
