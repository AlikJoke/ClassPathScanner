package ru.joke.classpath.scanner;

import ru.joke.classpath.ClassPathResources;

public interface ClassPathScanner {

    default ClassPathResources scan() {
        return scan(ClassPathScannerEngines.getDefaultEngine());
    }

    default ClassPathResources scan(ClassPathScannerEngine engine) {
        return engine.scan(this);
    }

    boolean overrideDefaultEngineScope();

    static ClassPathScannerBuilder builder() {
        return ClassPathScannerBuilder.create();
    }
}
