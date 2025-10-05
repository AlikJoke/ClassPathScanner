package ru.joke.classpath.services;

import java.util.Set;

public interface IndexedClassPathLocation {

    String CONFIGURATION_DIR = "META-INF/classpath-indexing/";
    String INDEXED_RESOURCES_FILE = CONFIGURATION_DIR + "indexed-resources.index";

    String getLocation();

    default Set<ClassLoader> getTargetClassLoaders() {
        return Set.of(getClass().getClassLoader());
    }

    static IndexedClassPathLocation relativeLocation(Set<ClassLoader> targetClassLoaders) {
        return new IndexedClassPathLocation() {
            @Override
            public String getLocation() {
                return INDEXED_RESOURCES_FILE;
            }

            @Override
            public Set<ClassLoader> getTargetClassLoaders() {
                return targetClassLoaders;
            }
        };
    }
}
