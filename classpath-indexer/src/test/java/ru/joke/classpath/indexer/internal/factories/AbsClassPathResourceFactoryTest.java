package ru.joke.classpath.indexer.internal.factories;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;
import ru.joke.classpath.indexer.internal.configs.ClassPathIndexingConfiguration;
import ru.joke.classpath.indexer.internal.configs.ClassPathIndexingConfigurationService;
import ru.joke.classpath.indexer.test_util.TestTypeElement;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

abstract class AbsClassPathResourceFactoryTest<E extends Element, R extends ClassPathResource, F extends ClassPathResourceFactory<R, E>> {

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

    protected abstract Function<ClassPathIndexingContext, F> factoryCreator();
}
