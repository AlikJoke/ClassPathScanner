package ru.joke.classpath.scanner;

import org.junit.jupiter.api.Test;
import ru.joke.classpath.ClassPathResources;
import ru.joke.classpath.IndexedClassPathResources;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ClassPathScannerTest {

    @Test
    void testBuilderCreation() {
        final var builder = ClassPathScanner.builder();
        assertNotNull(builder, "Builder must be not null");
    }

    @Test
    void testScan() {
        final var scanner = new FakeClassPathScanner();
        final var mockEngine = mock(ClassPathScannerEngine.class);
        final var expectedResult = new IndexedClassPathResources();
        when(mockEngine.scan(eq(scanner))).thenReturn(expectedResult);

        scanner.scan();

        assertEquals(ClassPathScannerEngines.getDefaultEngine(), scanner.engine, "Engine must be equal to default");
        assertThrows(InvalidApiUsageException.class, () -> scanner.scan(null));

        final var actualResult = scanner.scan(mockEngine);

        assertSame(expectedResult, actualResult, "Result of scanning must be equal");
        verify(mockEngine).scan(scanner);
    }

    private static class FakeClassPathScanner implements ClassPathScanner {

        private ClassPathScannerEngine engine;

        @Override
        public boolean overrideDefaultEngineScope() {
            return false;
        }

        @Override
        public ClassPathResources scan(ClassPathScannerEngine engine) {
            this.engine = engine;
            if (ClassPathScannerEngines.getDefaultEngine().equals(engine)) {
                return new IndexedClassPathResources();
            } else {
                return ClassPathScanner.super.scan(engine);
            }
        }
    }
}
