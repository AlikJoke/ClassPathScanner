package ru.joke.classpath.scanner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClassPathScannerEnginesTest {

    @Test
    void testDefaultEngine() {
        final var defaultEngine = ClassPathScannerEngines.getDefaultEngine();

        assertNotNull(defaultEngine, "Default engine must be not null");
        assertEquals(ClassPathScannerEngineConfiguration.defaultConfig(), defaultEngine.configuration(), "Default engine config must be equal");
        assertEquals(defaultEngine, ClassPathScannerEngines.getDefaultEngine(), "Default engine instance must be singleton");
    }

    @Test
    void testFindEngineById() {
        final var engineWrapper = ClassPathScannerEngines.getEngine("1");
        assertNotNull(engineWrapper, "Engine wrapper must be not null");
        assertTrue(engineWrapper.isEmpty(), "Engine wrapper must be empty");

        assertThrows(InvalidApiUsageException.class, () -> ClassPathScannerEngines.getEngine(""));
    }

    @Test
    void testDestroyAbsentEngineById() {
        assertFalse(ClassPathScannerEngines.destroyEngine("1"), "Absent engine must not be destroyed");
    }

    @Test
    void testFailedEngineCreation() {
        assertThrows(
                InvalidApiUsageException.class,
                () -> ClassPathScannerEngines.createEngine("", ClassPathScannerEngineConfiguration.defaultConfig())
        );
        assertThrows(
                InvalidApiUsageException.class,
                () -> ClassPathScannerEngines.createEngine("0", null)
        );
    }

    @Test
    void testStatelessEngineLifecycle() {
        testEngineLifecycle("2", ClassPathScannerEngineConfiguration.builder().stateless().build());
    }

    @Test
    void testStatefulEngineLifecycle() {
        testEngineLifecycle("3", ClassPathScannerEngineConfiguration.builder().stateful().build());
    }

    private void testEngineLifecycle(
            final String id,
            final ClassPathScannerEngineConfiguration config
    ) {
        final var engine = ClassPathScannerEngines.createEngine(id, config);

        assertNotNull(engine, "Engine must be not null");
        assertEquals(config, engine.configuration(), "Engine config must be not null");

        final var engineWrapper = ClassPathScannerEngines.getEngine(id);
        assertNotNull(engineWrapper, "Engine wrapper must be not null");
        assertTrue(engineWrapper.isPresent(), "Engine wrapper must be not empty");
        assertEquals(engine, engineWrapper.get(), "Engine must be equal");

        assertSame(engine, ClassPathScannerEngines.createEngine(id, config), "Engine instance must be same");
        assertTrue(ClassPathScannerEngines.destroyEngine(id), "Engine must be destroyed");
    }
}
