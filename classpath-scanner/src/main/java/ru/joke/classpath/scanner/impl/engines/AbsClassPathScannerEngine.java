package ru.joke.classpath.scanner.impl.engines;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.scanner.ClassPathScanner;
import ru.joke.classpath.scanner.ClassPathScannerEngine;
import ru.joke.classpath.scanner.ClassPathScannerEngineConfiguration;
import ru.joke.classpath.scanner.impl.PredicateBasedClassPathScanner;

import java.util.function.Predicate;

abstract sealed class AbsClassPathScannerEngine implements ClassPathScannerEngine
        permits StatefulClassPathScannerEngine, StatelessClassPathScannerEngine {

    protected final ClassPathScannerEngineConfiguration configuration;

    protected AbsClassPathScannerEngine(final ClassPathScannerEngineConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public ClassPathScannerEngineConfiguration configuration() {
        return this.configuration;
    }

    protected Predicate<ClassPathResource> buildFinalFilter(ClassPathScanner scanner) {
        return this.configuration.defaultScopeFilter()
                                    .filter(Predicate.not(ClassPathScanner::overrideDefaultEngineScope))
                                    .map(this::checkScanner)
                                    .map(checkScanner(scanner)::and)
                                    .orElse(checkScanner(scanner));
    }

    private PredicateBasedClassPathScanner checkScanner(ClassPathScanner scanner) {
        if (!(scanner instanceof PredicateBasedClassPathScanner ps)) {
            throw new ClassCastException();
        }

        return ps;
    }
}
