package ru.joke.classpath.indexer.internal.factories;

import org.junit.jupiter.api.Test;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ModuleResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;
import ru.joke.classpath.indexer.test_util.TestModuleElement;
import ru.joke.classpath.indexer.test_util.TestTypeElement;

import javax.lang.model.element.ModuleElement;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.module.ModuleDescriptor;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ModuleResourceFactoryTest extends AbsClassPathResourceFactoryTest<ModuleElement, ModuleResource, ModuleResourceFactory> {

    @Test
    void testRealModule() {
        final var testModule = getClass().getModule();
        final var testModuleElement = new TestModuleElement(testModule);
        final var aliasesFromConfig = Map.of(testModule.getName(), Set.of("m1", "m2"));
        final var result = prepareFactory(aliasesFromConfig, testModuleElement).create(testModuleElement);

        assertNotNull(result, "Result must be not null");
        assertTrue(result.isPresent(), "Result must be not empty");

        final var moduleResource = result.get();

        makeCommonChecks(moduleResource, testModule);

        assertEquals(aliasesFromConfig.get(testModule.getName()), moduleResource.aliases(), "Aliases must be equal");
        assertTrue(moduleResource.modifiers().isEmpty(), "Modifiers must be empty");
        assertTrue(moduleResource.annotations().isEmpty(), "Annotations list must be empty");
    }

    @Test
    void testFakeModule() {
        final var aliasesFromAnnotation = Set.of("m1", "m2");
        final Module testModule = createFakeModule(aliasesFromAnnotation.toArray(new String[0]));
        final var testModuleElement = new TestModuleElement(testModule);

        final var aliasesFromConfig = Map.of(testModule.getName(), Set.of("m3"));
        final var expectedAnnotationsMap =
                Set.of(ClassPathIndexed.class, Deprecated.class, Documented.class, Retention.class, Target.class)
                        .stream()
                        .map(TestTypeElement::new)
                        .collect(Collectors.toMap(e -> e.getQualifiedName().toString(), Function.identity()));

        final var result = prepareFactory(aliasesFromConfig, testModuleElement).create(testModuleElement);

        assertNotNull(result, "Result must be not null");
        assertTrue(result.isPresent(), "Result must be not empty");

        final var moduleResource = result.get();

        makeCommonChecks(moduleResource, testModule);
        assertEquals(1, moduleResource.modifiers().size(), "Modifiers count must be equal");
        assertTrue(moduleResource.modifiers().contains(ClassPathResource.Modifier.OPENED), "Module should be opened");
        assertEquals(5, moduleResource.annotations().size(), "Annotations count must be equal");

        final var actualAnnotations =
                moduleResource.annotations()
                                .stream()
                                .map(ClassPathResource.ClassReference::canonicalName)
                                .collect(Collectors.toSet());

        assertEquals(expectedAnnotationsMap.keySet(), actualAnnotations, "Annotations must be equal");

        assertEquals(3, moduleResource.aliases().size(), "Aliases count must be equal");
        assertTrue(moduleResource.aliases().containsAll(aliasesFromConfig.get(testModule.getName())), "Aliases must contain all aliases from config");
        assertTrue(moduleResource.aliases().containsAll(aliasesFromAnnotation), "Aliases must contain all aliases from annotation under module");
    }

    private void makeCommonChecks(
            final ModuleResource moduleResource,
            final Module testModule
    ) {
        assertEquals(testModule.getName(), moduleResource.name(), "Name of the module must be equal");
        assertTrue(moduleResource.packageName().isEmpty(), "Package of the module must be empty");
        assertEquals(moduleResource.name(), moduleResource.module(), "Name of the resource must be equal to module");
        assertEquals(ClassPathResource.Type.MODULE, moduleResource.type(), "Type must be equal");
        assertThrows(UnsupportedOperationException.class, moduleResource::asModule);
    }

    private Module createFakeModule(String... aliases) {
        final var module = mock(Module.class);
        final var moduleName = "ru.joke.test";
        when(module.getName()).thenReturn(moduleName);

        final var indexedAnnotation = new ClassPathIndexed() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return ClassPathIndexed.class;
            }

            @Override
            public String[] value() {
                return aliases;
            }
        };
        when(module.getAnnotation(ClassPathIndexed.class)).thenReturn(indexedAnnotation);
        when(module.getDeclaredAnnotations()).thenReturn(new Annotation[] { indexedAnnotation, () -> Deprecated.class });

        final var moduleDescriptor = ModuleDescriptor.newOpenModule(moduleName).build();
        when(module.getDescriptor()).thenReturn(moduleDescriptor);

        return module;
    }

    @Override
    protected Function<ClassPathIndexingContext, ModuleResourceFactory> factoryCreator() {
        return ModuleResourceFactory::new;
    }
}
