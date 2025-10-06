package ru.joke.classpath.scanner;

import ru.joke.classpath.ClassPathResources;

public interface ClassPathScanner {

    default ClassPathResources scan() {
        return scan(ClassPathScannerEngines.getDefaultEngine());
    }

    default ClassPathResources scan(ClassPathScannerEngine engine) {
        if (engine == null) {
            throw new InvalidApiUsageException("Scanner engine must be provided");
        }

        return engine.scan(this);
    }

    boolean overrideDefaultEngineScope();

    static ClassPathScannerBuilder builder() {
        return ClassPathScannerBuilder.create();
    }
}
