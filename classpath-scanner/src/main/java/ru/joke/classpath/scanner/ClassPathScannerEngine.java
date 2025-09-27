package ru.joke.classpath.scanner;

public interface ClassPathScannerEngine {

    ClassPathScannerBuilder createScanner();

    ClassPathScannerEngineConfiguration configuration();
}
