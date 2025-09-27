package ru.joke.classpath.scanner.impl.engines;

import ru.joke.classpath.ClassPathResourcesService;
import ru.joke.classpath.scanner.ClassPathScannerEngine;

public interface ExtendedClassPathScannerEngine extends ClassPathScannerEngine {

    ClassPathResourcesService resourcesService();
}
