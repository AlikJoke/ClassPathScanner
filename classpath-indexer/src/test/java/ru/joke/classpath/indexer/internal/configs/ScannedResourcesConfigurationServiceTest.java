package ru.joke.classpath.indexer.internal.configs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ScannedResourcesConfigurationServiceTest {

    private File outputConfigDir;
    private Messager messager;
    private ScannedResourcesConfigurationService configurationService;

    @BeforeEach
    void setUp() throws IOException {
        this.messager = mock(Messager.class);
        this.outputConfigDir = Files.createTempDirectory("cfg_").toFile();
        this.configurationService = new ScannedResourcesConfigurationService(this.outputConfigDir, this.messager);
    }

    @AfterEach
    void tearDown() {
        this.outputConfigDir.delete();
    }

    @Test
    void testWhenNoAvailableConfigs() {
        final var result = this.configurationService.deserializeAllAvailable();

        assertNotNull(result, "Result must be not null always");
        assertTrue(result.isEmpty(), "Collection of configs must be empty");
    }

    @Test
    void testWhenOneOfConfigsIsCorrupted() throws IOException {
        assertEquals(0, getDirFiles().length, "Config files in dir must be empty");

        final var resources1 = createScannedResources(1);
        this.configurationService.serialize(resources1);

        final var resources2 = createScannedResources(2);
        this.configurationService.serialize(resources2);
        
        // force corrupt one of config files
        Files.writeString(getDirFiles()[0].toPath(), "fff");

        final var result = this.configurationService.deserializeAllAvailable();

        assertNotNull(result, "Result must be not null always");
        assertEquals(1, result.size(), "Collection of configs must contain single element");

        verify(this.messager, only()).printMessage(eq(Diagnostic.Kind.ERROR), anyString());
    }

    @Test
    void testConversion() {
        assertEquals(0, getDirFiles().length, "Config files in dir must be empty");

        final var resources1 = createScannedResources(1);
        this.configurationService.serialize(resources1);

        final var resources2 = createScannedResources(2);
        this.configurationService.serialize(resources2);

        assertEquals(2, getDirFiles().length, "Config files count must be equal");

        final var configs = this.configurationService.deserializeAllAvailable();
        assertNotNull(configs, "Configs must be not null");
        assertEquals(2, configs.size(), "Configs count must be equal");

        final var configsList = new ArrayList<>(configs);
        if (configsList.get(0).annotations().equals(resources1.annotations())) {
            checkEquality(resources1, configsList.get(0));
            checkEquality(resources2, configsList.get(1));
        } else {
            checkEquality(resources1, configsList.get(1));
            checkEquality(resources2, configsList.get(0));
        }
    }

    private void checkEquality(final ScannedResources expected, final ScannedResources actual) {
        assertEquals(expected.annotations(), actual.annotations(), "Annotations must be equal");
        assertEquals(expected.interfaces(), actual.interfaces(), "Interfaces must be equal");
        assertEquals(expected.classes(), actual.classes(), "Classes must be equal");
        assertEquals(expected.aliases(), actual.aliases(), "Aliases must be equal");
    }
    
    private File[] getDirFiles() {
        return this.outputConfigDir.listFiles();
    }

    private ScannedResources createScannedResources(int suffix) {
        final var resources = new ScannedResources();
        resources.aliases().put("ru.joke.Test1" + suffix, Set.of("a" + suffix, "b" + suffix));
        resources.aliases().put("ru.joke.Test2" + suffix, Set.of("a" + suffix));
        resources.classes().add("ru.joke.TestClass" + suffix);
        resources.interfaces().add("ru.joke.TestInterface1" + suffix);
        resources.interfaces().add("ru.joke.TestInterface2" + suffix);
        resources.annotations().add("ru.joke.TestAnnotation1" + suffix);
        resources.annotations().add("ru.joke.TestAnnotation2" + suffix);

        return resources;
    }
}