package ru.joke.classpath.scanner;

import ru.joke.classpath.ClassPathResources;

public interface ClassPathScannerEngine {

    ClassPathResources scan(ClassPathScanner scanner);

    ClassPathScannerEngineConfiguration configuration();

    void reload();
}
