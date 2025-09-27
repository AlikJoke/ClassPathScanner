package ru.joke.classpath.scanner;

import ru.joke.classpath.ClassPathResources;

public interface ClassPathScanner {

    ClassPathResources scan();

    static ClassPathScannerBuilder builder() {
        return ClassPathScannerBuilder.create();
    }
}
