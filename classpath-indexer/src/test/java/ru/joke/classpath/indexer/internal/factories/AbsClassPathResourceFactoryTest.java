package ru.joke.classpath.indexer.internal.factories;

import org.junit.jupiter.api.Test;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;
import ru.joke.classpath.indexer.internal.configs.ClassPathIndexingConfiguration;
import ru.joke.classpath.indexer.internal.configs.ClassPathIndexingConfigurationService;
import ru.joke.classpath.indexer.test_util.TestTypeElement;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.util.Elements;
import java.io.File;
import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

abstract class AbsClassPathResourceFactoryTest<E extends Element, R extends ClassPathResource, F extends ClassPathResourceFactory<R, E>> {

    @Test
    void testSupportedTypes() {
        final var factory = factoryCreator().apply(mock(ClassPathIndexingContext.class));
        assertEquals(expectedSupportedKinds(), factory.supportedTypes(), "Supported types must be equal");
    }

    protected F prepareFactory(
            final Map<String, Set<String>> predefinedAliasesFromConfig,
            final Element... rootElements
    ) {

        final var indexingConfig = new ClassPathIndexingConfiguration(
                Collections.emptySet(),
                Collections.emptySet(),
                predefinedAliasesFromConfig,
                Collections.emptySet()
        );

        final var indexingConfigurationService = mock(ClassPathIndexingConfigurationService.class);
        when(indexingConfigurationService.find()).thenReturn(Optional.of(indexingConfig));

        final var roundEnv = mock(RoundEnvironment.class);
        when(roundEnv.getRootElements()).then(i -> Set.of(rootElements));

        final var processingEnv = mock(ProcessingEnvironment.class);
        when(processingEnv.getMessager()).thenReturn(mock(Messager.class));

        final var elementUtils = mock(Elements.class);
        when(elementUtils.getTypeElement(anyString())).then(i -> {
            final String typeName = i.getArgument(0);
            return new TestTypeElement(getClass().getClassLoader().loadClass(typeName));
        });

        when(processingEnv.getElementUtils()).thenReturn(elementUtils);

        final var context = ClassPathIndexingContext.create(
                mock(File.class),
                processingEnv,
                roundEnv,
                e -> true,
                indexingConfigurationService
        );

        return factoryCreator().apply(context);
    }

    protected void makeAnnotationsCheck(
            final Set<Class<?>> expectedAnnotationTypes,
            final R resource
    ) {
        if (!expectedAnnotationTypes.isEmpty()) {
            final var expectedAnnotationsMap =
                    expectedAnnotationTypes
                            .stream()
                            .map(TestTypeElement::new)
                            .collect(Collectors.toMap(e -> e.getQualifiedName().toString(), Function.identity()));

            final var actualAnnotations =
                    resource.annotations()
                            .stream()
                            .map(ClassPathResource.ClassReference::canonicalName)
                            .collect(Collectors.toSet());

            assertEquals(expectedAnnotationsMap.keySet(), actualAnnotations, "Annotations must be equal");
        } else {
            assertTrue(resource.annotations().isEmpty(), "Annotations must be empty");
        }
    }

    protected void makeAliasesCheck(
            final AnnotatedElement annotatedElement,
            final R resource,
            final Set<String> expectedAliasesFromConfig
    ) {
        final Set<String> expectedAliases = new HashSet<>(expectedAliasesFromConfig);
        final var annotation = annotatedElement.getAnnotation(ClassPathIndexed.class);
        if (annotation != null) {
            expectedAliases.addAll(Set.of(annotation.value()));
        }

        assertEquals(expectedAliases, resource.aliases(), "Aliases must be equal");
    }

    protected abstract Function<ClassPathIndexingContext, F> factoryCreator();

    protected abstract Set<ElementKind> expectedSupportedKinds();
}
