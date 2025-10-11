package ru.joke.classpath.indexer.internal.collectors;

import org.junit.jupiter.api.Test;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;
import ru.joke.classpath.indexer.internal.configs.ClassPathIndexingConfiguration;
import ru.joke.classpath.indexer.internal.configs.ClassPathIndexingConfigurationService;
import ru.joke.classpath.indexer.internal.configs.ScannedResources;
import ru.joke.classpath.indexer.internal.configs.ScannedResourcesConfigurationService;
import ru.joke.classpath.indexer.internal.factories.DelegatingResourceFactory;
import ru.joke.classpath.indexer.test_util.TestPackageElement;
import ru.joke.classpath.indexer.test_util.TestTypeElement;
import ru.joke.classpath.indexer.test_util.TestVariableElement;
import ru.joke.classpath.indexer.test_util.fixtures.TestClass;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClassPathResourcesCollectorTest {

    @Test
    void testWhenNoIndexingConfig() {

        final var testData = createCollector(
                null,
                e -> e.getKind() == ElementKind.PACKAGE || e instanceof TypeElement t && !t.getNestingKind().isNested(),
                Collections.emptySet(),
                Set.of(new TestPackageElement(TestClass.class.getPackage(), null), new TestTypeElement(TestClass.class))
        );
        final var collector = testData.collector();

        collector.collect();

        final var currentScannedResources = testData.context().currentScannedResources();
        assertEquals(Set.of(TestClass.class.getCanonicalName()), currentScannedResources.classes(), "Classes in scanned resources must be equal");
        assertTrue(currentScannedResources.annotations().isEmpty(), "Annotations in scanned resources must be empty");
        assertTrue(currentScannedResources.interfaces().isEmpty(), "Interfaces in scanned resources must be empty");
        assertTrue(currentScannedResources.aliases().isEmpty(), "Aliases in scanned resources must be empty");

        final var resources = testData.context().collectedResources();
        assertEquals(2, resources.size(), "Indexed resources count must be equal");
        for (var resource : resources) {
            assertTrue(resource.type() == ClassPathResource.Type.PACKAGE || resource.type() == ClassPathResource.Type.CLASS, "Resource type must be equal");
        }
    }

    @Test
    void testWhenIndexingConfigPresent() throws NoSuchFieldException {

        final var config = new ClassPathIndexingConfiguration(
                Set.of(TestClass.NestedClass.class.getCanonicalName()),
                Set.of(Deprecated.class.getCanonicalName()),
                Map.of(TestClass.Interface.Test.class.getCanonicalName(), Set.of("c1")),
                Set.of()
        );
        final var prevScannedResources = new ScannedResources();
        prevScannedResources.interfaces().add(Serializable.class.getCanonicalName());

        final var testData = createCollector(
                config,
                e -> e.getKind() == ElementKind.FIELD && "ref".equals(e.getSimpleName().toString()) || e.getKind().isClass() && !e.getSimpleName().isEmpty(),
                Collections.singleton(prevScannedResources),
                Set.of(
                        new TestVariableElement(TestClass.Interface.class.getDeclaredField("ref")),
                        new TestTypeElement(TestClass.class)
                )
        );
        final var collector = testData.collector();

        collector.collect();

        final var currentScannedResources = testData.context().currentScannedResources();
        assertEquals(
                Set.of(TestClass.class.getCanonicalName(), TestClass.NestedClass.class.getCanonicalName(), TestClass.Interface.Test.class.getCanonicalName()),
                currentScannedResources.classes(),
                "Classes in scanned resources must be equal"
        );
        assertTrue(currentScannedResources.annotations().isEmpty(), "Annotations in scanned resources must be empty");
        assertTrue(currentScannedResources.interfaces().isEmpty(), "Interfaces in scanned resources must be empty");
        assertTrue(currentScannedResources.aliases().isEmpty(), "Aliases in scanned resources must be empty");

        final var resources = testData.context().collectedResources();
        assertEquals(5, resources.size(), "Collected resources count must be equal");
        final var collectedElements =
                resources
                        .stream()
                        .map(ClassPathResource::name)
                        .collect(Collectors.toSet());
        assertTrue(
                collectedElements.contains("ref"),
                "Field must present in collected resources"
        );
        assertTrue(
                collectedElements.contains(TestClass.class.getSimpleName()),
                "Class must present in collected resources"
        );
        assertTrue(
                collectedElements.contains(TestClass.class.getSimpleName() + '$' + TestClass.Enum.class.getSimpleName()),
                "Enum must present in collected resources"
        );
        assertTrue(
                collectedElements.contains(TestClass.class.getSimpleName() + '$' + TestClass.NestedClass.class.getSimpleName()),
                "Nested class must present in collected resources"
        );
        assertTrue(
                collectedElements.contains(TestClass.class.getSimpleName() + '$' + TestClass.Interface.class.getSimpleName() + '$' + TestClass.Interface.Test.class.getSimpleName()),
                "Class in interface must present in collected resources"
        );
    }

    private TestData createCollector(
            ClassPathIndexingConfiguration indexingConfiguration,
            Predicate<Element> processingFilter,
            Set<ScannedResources> prevScannedResources,
            Set<Element> annotatedElements
    ) {
        final var packageElement = new TestPackageElement(TestClass.class.getPackage(), null);
        final var classElement = new TestTypeElement(TestClass.class);

        final var scannedResourcesConfigService = mock(ScannedResourcesConfigurationService.class);
        when(scannedResourcesConfigService.deserializeAllAvailable()).thenReturn(prevScannedResources);

        final var context = prepareContext(
                indexingConfiguration,
                processingFilter,
                scannedResourcesConfigService,
                annotatedElements,
                packageElement, classElement
        );

        return new TestData(
                new ClassPathResourcesCollector(context, new DelegatingResourceFactory(context)),
                context
        );
    }

    private ClassPathIndexingContext prepareContext(
            final ClassPathIndexingConfiguration indexingConfig,
            final Predicate<Element> processingFilter,
            final ScannedResourcesConfigurationService scannedResourcesConfigurationService,
            final Set<Element> annotatedElements,
            final Element... rootElements
    ) {
        final var indexingConfigurationService = mock(ClassPathIndexingConfigurationService.class);
        when(indexingConfigurationService.find()).thenReturn(Optional.ofNullable(indexingConfig));

        final var roundEnv = mock(RoundEnvironment.class);

        when(roundEnv.getElementsAnnotatedWithAny(any(TypeElement[].class))).then(i -> annotatedElements);
        when(roundEnv.getRootElements()).then(i -> Set.of(rootElements));

        final var processingEnv = mock(ProcessingEnvironment.class);
        when(processingEnv.getMessager()).thenReturn(mock(Messager.class));

        final var elementUtils = mock(Elements.class);
        when(elementUtils.getTypeElement(anyString()))
                .then(i -> {
                    final var className = i.getArgument(0, String.class);
                    final String binaryClassName;
                    if (!className.equals(TestClass.class.getCanonicalName()) && className.startsWith(TestClass.class.getCanonicalName())) {
                        binaryClassName = TestClass.class.getPackageName() + '.'
                                + className.replace(TestClass.class.getPackageName() + ".", "").replace('.', '$');
                    } else {
                        binaryClassName = className;
                    }
                    final var cls = getClass().getClassLoader().loadClass(binaryClassName);
                    return new TestTypeElement(cls);
                });
        when(processingEnv.getElementUtils()).thenReturn(elementUtils);

        return ClassPathIndexingContext.create(
                processingEnv,
                roundEnv,
                processingFilter,
                indexingConfigurationService,
                scannedResourcesConfigurationService
        );
    }

    private record TestData(
            ClassPathResourcesCollector collector,
            ClassPathIndexingContext context
    ) {}
}
