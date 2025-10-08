package ru.joke.classpath.indexer.internal.configs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static ru.joke.classpath.indexer.internal.configs.ClassPathIndexingConfigurationService.CONFIG_LOCATION;

class ClassPathIndexingConfigurationServiceTest {

    private Filer filer;
    private Messager messager;
    private ClassPathIndexingConfigurationService service;

    @BeforeEach
    void setUp() {
        this.filer = mock(Filer.class);
        this.messager = mock(Messager.class);
        this.service = new ClassPathIndexingConfigurationService(this.filer, this.messager);
    }

    @Test
    void testWhenNoConfigFileFound() throws IOException {
        when(this.filer.getResource(eq(StandardLocation.CLASS_OUTPUT), eq(""), eq(CONFIG_LOCATION)))
                .thenReturn(null);
        final var result = this.service.find();

        assertNotNull(result, "Result must be not null always");
        assertTrue(result.isEmpty(), "Result must be empty");
        verify(this.messager, only()).printMessage(eq(Diagnostic.Kind.NOTE), anyString());
    }

    @Test
    void testWhenConfigFileFound() throws IOException, URISyntaxException {
        final var configFile = getClass().getResource("/" + CONFIG_LOCATION);
        final var configFileUri = Objects.requireNonNull(configFile).toURI();
        final var fileObject = new TestJavaFileObject(configFileUri);

        when(this.filer.getResource(eq(StandardLocation.CLASS_OUTPUT), eq(""), eq(CONFIG_LOCATION))).thenReturn(fileObject);
        final var result = this.service.find();

        assertNotNull(result, "Result must be not null always");
        assertTrue(result.isPresent(), "Result must be not empty");

        try (final var configStream = getClass().getResourceAsStream("/" + CONFIG_LOCATION)) {
            final var config = ClassPathIndexingConfiguration.parse(configStream);
            makeChecks(config, result.get());
        }
    }

    @Test
    void testWhenFileNotFoundExceptionRaisedWhileConfigFileReading() throws IOException, URISyntaxException {
        final var configFile = createBrokenFileObject(new FileNotFoundException());
        when(this.filer.getResource(eq(StandardLocation.CLASS_OUTPUT), eq(""), eq(CONFIG_LOCATION))).thenReturn(configFile);

        final var result = this.service.find();

        assertNotNull(result, "Result must be not null always");
        assertTrue(result.isEmpty(), "Result must be empty");
    }

    @Test
    void testWhenIOExceptionRaisedWhileConfigFileReading() throws IOException, URISyntaxException {
        final var configFile = createBrokenFileObject(new IOException());
        when(this.filer.getResource(eq(StandardLocation.CLASS_OUTPUT), eq(""), eq(CONFIG_LOCATION))).thenReturn(configFile);

        final var result = this.service.find();

        assertNotNull(result, "Result must be not null always");
        assertTrue(result.isEmpty(), "Result must be empty");

        verify(this.messager, only()).printMessage(eq(Diagnostic.Kind.ERROR), anyString());
    }

    private FileObject createBrokenFileObject(IOException exception) throws URISyntaxException {
        final var configFile = getClass().getResource("/" + CONFIG_LOCATION);
        final var configFileUri = Objects.requireNonNull(configFile).toURI();
        return new TestJavaFileObject(configFileUri) {
            @Override
            public InputStream openInputStream() throws IOException {
                throw exception;
            }
        };
    }

    private void makeChecks(final ClassPathIndexingConfiguration expected, final ClassPathIndexingConfiguration actual) {
        assertEquals(expected.annotations(), actual.annotations(), "Annotations must be equal");
        assertEquals(expected.interfaces(), actual.interfaces(), "Interfaces must be equal");
        assertEquals(expected.classes(), actual.classes(), "Classes must be equal");
        assertEquals(expected.aliases(), actual.aliases(), "Aliases must be equal");
    }

    private static class TestJavaFileObject extends SimpleJavaFileObject {

        protected TestJavaFileObject(URI uri) {
            super(uri, Kind.OTHER);
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return this.uri.toURL().openStream();
        }
    }
}
