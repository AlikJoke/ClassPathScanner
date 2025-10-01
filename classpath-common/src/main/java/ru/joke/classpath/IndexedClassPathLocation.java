package ru.joke.classpath;

public interface IndexedClassPathLocation {

    String CONFIGURATION_DIR = "META-INF/classpath-indexing/";
    String INDEXED_RESOURCES_FILE = CONFIGURATION_DIR + "indexed-resources.index";

    String getLocation();

    static IndexedClassPathLocation relativeLocation() {
        return () -> INDEXED_RESOURCES_FILE;
    }
}
