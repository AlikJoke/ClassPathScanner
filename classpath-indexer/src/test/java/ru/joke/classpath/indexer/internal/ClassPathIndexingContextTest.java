package ru.joke.classpath.indexer.internal;

import org.junit.jupiter.api.Test;
import ru.joke.classpath.indexer.internal.configs.ClassPathIndexingConfiguration;
import ru.joke.classpath.indexer.internal.configs.ClassPathIndexingConfigurationService;
import ru.joke.classpath.indexer.internal.configs.ScannedResources;
import ru.joke.classpath.indexer.internal.configs.ScannedResourcesConfigurationService;
import ru.joke.classpath.indexer.test_util.TestModuleElement;
import ru.joke.classpath.indexer.test_util.TestTypeElement;
import ru.joke.classpath.indexer.test_util.fixtures.TestClass;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.util.Elements;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.module.ModuleDescriptor;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClassPathIndexingContextTest {

    @Test
    void testFlushingCollectedResources() {
        final var roundEnv = mock(RoundEnvironment.class);
        final var elementUtils = mock(Elements.class);

        final var scannedResourcesConfigurationService = mock(ScannedResourcesConfigurationService.class);
        when(scannedResourcesConfigurationService.deserializeAllAvailable()).thenReturn(Collections.emptySet());

        final var context = prepareExtendedContext(
                null,
                roundEnv,
                e -> true,
                elementUtils,
                scannedResourcesConfigurationService
        );

        context.flushCurrentScannedResources();
        verify(scannedResourcesConfigurationService, never()).serialize(any());

        final var currentScannedResources = context.currentScannedResources();
        currentScannedResources.classes().add(TestClass.class.getCanonicalName());

        context.flushCurrentScannedResources();
        verify(scannedResourcesConfigurationService).serialize(currentScannedResources);
    }

    @Test
    void testStdContextCreation() throws IOException {

        final Predicate<Element> filter = e -> e.getKind().isInterface();
        final var roundEnv = mock(RoundEnvironment.class);
        final var elementUtils = mock(Elements.class);

        final var context = prepareContext(
                roundEnv,
                filter,
                elementUtils,
                new TestTypeElement(TestClass.class)
        );

        assertNotNull(context.indexingConfiguration(), "Indexing config wrapper must be not null always");
        assertTrue(context.indexingConfiguration().isEmpty(), "Indexing config must be empty");
        assertTrue(context.moduleName().isEmpty(), "Module name be empty");

        makeCreatedContextChecks(
                context,
                roundEnv,
                filter,
                elementUtils,
                2
        );
    }

    @Test
    void testExtendedContextCreation() {
        final var indexingConfig = mock(ClassPathIndexingConfiguration.class);
        final var module = createTestModule();
        final var moduleElement = new TestModuleElement(module);
        final var classElement = new TestTypeElement(TestClass.class);

        final var roundEnv = mock(RoundEnvironment.class);
        final var elementUtils = mock(Elements.class);
        final Predicate<Element> processingFilter = e -> e.getKind() == ElementKind.ANNOTATION_TYPE;
        final var scannedResourcesConfigurationService = mock(ScannedResourcesConfigurationService.class);

        final var prevScannedResources = Set.of(new ScannedResources());
        when(scannedResourcesConfigurationService.deserializeAllAvailable()).thenReturn(prevScannedResources);
        final var context = prepareExtendedContext(
                indexingConfig,
                roundEnv,
                processingFilter,
                elementUtils,
                scannedResourcesConfigurationService,
                moduleElement, classElement
        );

        assertTrue(context.indexingConfiguration().isPresent(), "Indexing config must be specified");
        assertSame(indexingConfig, context.indexingConfiguration().get(), "Indexing config must be equal");
        assertEquals(module.getName(), context.moduleName(), "Module name be equal");
        assertSame(prevScannedResources, context.prevScannedResources(), "Prev scanned resources must be empty");

        makeCreatedContextChecks(
                context,
                roundEnv,
                processingFilter,
                elementUtils,
                1
        );
        verify(scannedResourcesConfigurationService).deserializeAllAvailable();
    }

    private void makeCreatedContextChecks(
            final ClassPathIndexingContext context,
            final RoundEnvironment expectedRoundEnv,
            final Predicate<Element> expectedFilter,
            final Elements expectedElementUtils,
            final int expectedElementsCount
    ) {
        assertNotNull(context.indexingConfiguration(), "Indexing config wrapper must be not null always");
        assertNotNull(context.currentScannedResources(), "Current scanned resources must be not null");
        assertTrue(context.currentScannedResources().isEmpty(), "Current scanned resources must be empty");
        assertNotNull(context.collectedResources(), "Collected resources must be not null");
        assertTrue(context.collectedResources().isEmpty(), "Collected resources must be empty");
        assertSame(expectedRoundEnv, context.roundEnvironment(), "Round env must be same");
        assertSame(expectedFilter, context.processingFilter(), "Processing filter must be same");
        assertSame(expectedElementUtils, context.elementUtils(), "Element utils must be same");
        assertNotNull(context.prevScannedResources(), "Prev scanned resources must be same");
        assertNotNull(context.elements(), "Collected compiling elements must be not null");
        assertFalse(context.elements().isEmpty(), "Collected compiling elements must be not empty");
        assertEquals(expectedElementsCount, context.elements().size(), "Collected compiling elements count must be equal");
    }

    private ClassPathIndexingContext prepareExtendedContext(
            final ClassPathIndexingConfiguration indexingConfig,
            final RoundEnvironment roundEnv,
            final Predicate<Element> processingFilter,
            final Elements elementUtils,
            final ScannedResourcesConfigurationService scannedResourcesConfigurationService,
            final Element... rootElements
    ) {
        final var indexingConfigurationService = mock(ClassPathIndexingConfigurationService.class);
        when(indexingConfigurationService.find()).thenReturn(Optional.ofNullable(indexingConfig));

        when(roundEnv.getRootElements()).then(i -> Set.of(rootElements));

        final var processingEnv = mock(ProcessingEnvironment.class);
        when(processingEnv.getMessager()).thenReturn(mock(Messager.class));

        when(processingEnv.getElementUtils()).thenReturn(elementUtils);

        return ClassPathIndexingContext.create(
                processingEnv,
                roundEnv,
                processingFilter,
                indexingConfigurationService,
                scannedResourcesConfigurationService
        );
    }

    private ClassPathIndexingContext prepareContext(
            final RoundEnvironment roundEnv,
            final Predicate<Element> processingFilter,
            final Elements elementUtils,
            final Element... rootElements
    ) throws IOException {
        final var processingEnv = mock(ProcessingEnvironment.class);
        when(processingEnv.getMessager()).thenReturn(mock(Messager.class));

        final var filer = mock(Filer.class);
        when(filer.getResource(any(), anyString(), any())).thenReturn(null);

        when(processingEnv.getFiler()).thenReturn(filer);
        when(processingEnv.getElementUtils()).thenReturn(elementUtils);

        when(roundEnv.getRootElements()).then(i -> Set.of(rootElements));

        return ClassPathIndexingContext.create(
                mock(File.class),
                processingEnv,
                roundEnv,
                processingFilter
        );
    }

    private Module createTestModule() {
        final var module = mock(Module.class);
        final var moduleName = "ru.joke.test";
        when(module.getName()).thenReturn(moduleName);
        when(module.getDeclaredAnnotations()).thenReturn(new Annotation[0]);

        final var moduleDescriptor = ModuleDescriptor.newOpenModule(moduleName).build();
        when(module.getDescriptor()).thenReturn(moduleDescriptor);

        return module;
    }
}
