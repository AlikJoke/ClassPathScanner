package ru.joke.classpath.scanner.internal.engines;

import org.junit.jupiter.api.Test;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.scanner.ClassPathScanner;
import ru.joke.classpath.scanner.ClassPathScannerEngine;
import ru.joke.classpath.scanner.ClassPathScannerEngineConfiguration;
import ru.joke.classpath.scanner.InvalidApiUsageException;
import ru.joke.classpath.scanner.internal.PredicateBasedClassPathScanner;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertThrows;

abstract class AbsClassPathScannerEngineTest {

    @Test
    void testWhenNullScannerProvided() {

        final var configBuilder = ClassPathScannerEngineConfiguration.builder();
        final var engine = createEngine(configBuilder);

        assertThrows(InvalidApiUsageException.class, () -> engine.scan(null));
    }

    @Test
    void testWhenUnsupportedTypeScannerProvided() {
        final var scanner = new ClassPathScanner() {

            @Override
            public boolean overrideDefaultEngineScope() {
                return false;
            }
        };

        final var configBuilder = ClassPathScannerEngineConfiguration.builder();
        final var engine = createEngine(configBuilder);

        assertThrows(ClassCastException.class, () -> engine.scan(scanner));
    }

    protected abstract ClassPathScannerEngine createEngine(ClassPathScannerEngineConfiguration.Builder configurationBuilder);

    static class WrappedScanner implements PredicateBasedClassPathScanner {

        private final PredicateBasedClassPathScanner scanner;
        Predicate<ClassPathResource> finalScanner;

        WrappedScanner(PredicateBasedClassPathScanner scanner) {
            this.scanner = scanner;
            this.finalScanner = this;
        }

        @Override
        public boolean test(ClassPathResource resource) {
            return scanner.test(resource);
        }

        @Override
        public Predicate<ClassPathResource> and(Predicate<? super ClassPathResource> other) {
            return (this.finalScanner = PredicateBasedClassPathScanner.super.and(other));
        }

        @Override
        public boolean overrideDefaultEngineScope() {
            return scanner.overrideDefaultEngineScope();
        }
    }
}
