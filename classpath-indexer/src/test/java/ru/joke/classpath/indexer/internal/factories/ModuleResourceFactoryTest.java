package ru.joke.classpath.indexer.internal.factories;

import org.junit.jupiter.api.Test;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ModuleResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;
import ru.joke.classpath.indexer.test_util.TestModuleElement;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ModuleElement;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.module.ModuleDescriptor;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ModuleResourceFactoryTest extends AbsClassPathResourceFactoryTest<ModuleElement, ModuleResource, ModuleResourceFactory> {

    @Test
    void testRealModule() {
        final var testModule = getClass().getModule();
        final var testModuleElement = new TestModuleElement(testModule);
        final var aliasesFromConfig = Map.of(
                testModule.getName(), Set.of("m1", "m2"),
                "test_module", Set.of("test")
        );
        final var factory = prepareFactory(aliasesFromConfig, testModuleElement);
        final var result = factory.create(testModuleElement);

        assertNotNull(result, "Result must be not null");
        assertTrue(result.isPresent(), "Result must be not empty");

        final var moduleResource = result.get();

        makeCommonChecks(moduleResource, testModule);

        assertTrue(moduleResource.modifiers().isEmpty(), "Modifiers must be empty");

        makeAnnotationsCheck(
                Collections.emptySet(),
                moduleResource
        );
        makeAliasesCheck(
                testModule,
                moduleResource,
                aliasesFromConfig.get(testModule.getName())
        );

        final var result2 = factory.create(testModuleElement);
        makeEqualityChecks(moduleResource, result2.orElse(null));
    }

    @Test
    void testFakeModule() {
        final var aliasesFromAnnotation = Set.of("m1", "m2");
        final Module testModule = createFakeModule(aliasesFromAnnotation.toArray(new String[0]));
        final var testModuleElement = new TestModuleElement(testModule);

        final var aliasesFromConfig = Map.of(testModule.getName(), Set.of("m3"));
        final var factory = prepareFactory(aliasesFromConfig, testModuleElement);
        final var result = factory.create(testModuleElement);

        assertNotNull(result, "Result must be not null");
        assertTrue(result.isPresent(), "Result must be not empty");

        final var moduleResource = result.get();

        makeCommonChecks(moduleResource, testModule);
        assertEquals(1, moduleResource.modifiers().size(), "Modifiers count must be equal");
        assertTrue(moduleResource.modifiers().contains(ClassPathResource.Modifier.OPENED), "Module should be opened");

        makeAnnotationsCheck(
                Set.of(ClassPathIndexed.class, Deprecated.class, Documented.class, Retention.class, Target.class),
                moduleResource
        );
        makeAliasesCheck(
                testModule,
                moduleResource,
                aliasesFromConfig.get(testModule.getName())
        );

        final var result2 = factory.create(testModuleElement);
        makeEqualityChecks(moduleResource, result2.orElse(null));
    }

    private void makeCommonChecks(
            final ModuleResource moduleResource,
            final Module testModule
    ) {
        assertEquals(testModule.getName(), moduleResource.name(), "Name of the module must be equal");
        assertTrue(moduleResource.packageName().isEmpty(), "Package of the module must be empty");
        assertEquals(moduleResource.name(), moduleResource.module(), "Name of the resource must be equal to module");
        assertEquals(moduleResource.id(), moduleResource.module(), "Id of the resource must be equal to module name");
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

    @Override
    protected Set<ElementKind> expectedSupportedKinds() {
        return Set.of(ElementKind.MODULE);
    }
}
