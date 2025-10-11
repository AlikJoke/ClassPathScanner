package ru.joke.classpath.indexer.internal.factories;

import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.PackageResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;
import ru.joke.classpath.indexer.test_util.TestModuleElement;
import ru.joke.classpath.indexer.test_util.TestPackageElement;
import ru.joke.classpath.indexer.test_util.fixtures.TestClass;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static ru.joke.classpath.ClassPathResource.MODULE_SEPARATOR;

class PackageResourceFactoryTest extends AbsClassPathResourceFactoryTest<PackageElement, PackageResource, PackageResourceFactory> {

    @Test
    void testPackageInNamedModule() {
        final var packageResource = executeTestsForPackageOfClass(TestClass.class);
        makeAnnotationsCheck(
                Set.of(ClassPathIndexed.class, Documented.class, Retention.class, Target.class),
                packageResource
        );
    }

    @Test
    void testPackageInUnnamedModule() {
        final var packageResource = executeTestsForPackageOfClass(Answer.class);
        makeAnnotationsCheck(
                Collections.emptySet(),
                packageResource
        );
    }

    private PackageResource executeTestsForPackageOfClass(final Class<?> clazz) {
        final var moduleElement = new TestModuleElement(clazz.getModule());
        final var testPackage = clazz.getPackage();
        final var testPackageElement = new TestPackageElement(testPackage, moduleElement);

        final var aliasesFromConfig = Map.of(testPackage.getName(), Set.of("p1", "p2"));
        final var factory = prepareFactory(aliasesFromConfig, testPackageElement, moduleElement);
        final var result = factory.create(testPackageElement);

        assertNotNull(result, "Result must be not null");
        assertTrue(result.isPresent(), "Result must be not empty");

        final var packageResource = result.get();

        assertEquals(testPackage.getName(), packageResource.name(), "Name of the package must be equal");
        assertTrue(packageResource.packageName().isEmpty(), "Package of the package must be empty");
        if (clazz.getModule().isNamed()) {
            assertEquals(clazz.getModule().getName(), packageResource.module(), "Module of the resource must be equal");
            assertEquals(packageResource.module() + MODULE_SEPARATOR + packageResource.name(), packageResource.id(), "Id of the package resource must be equal");
        } else {
            assertTrue(packageResource.module().isEmpty(), "Module of the resource must be empty");
            assertEquals(packageResource.name(), packageResource.id(), "Id of the package resource must be equal to package name in unnamed module");
        }

        assertEquals(ClassPathResource.Type.PACKAGE, packageResource.type(), "Type must be equal");
        assertThrows(UnsupportedOperationException.class, packageResource::asPackage);

        if (testPackage.isSealed()) {
            assertEquals(1, packageResource.modifiers().size(), "Modifiers count must be equal");
            assertTrue(packageResource.modifiers().contains(ClassPathResource.Modifier.SEALED), "Package should be sealed");
        } else {
            assertTrue(packageResource.modifiers().isEmpty(), "Modifiers must be empty");
        }

        makeAliasesCheck(
                testPackage,
                packageResource,
                aliasesFromConfig.get(testPackage.getName())
        );

        final var result2 = factory.create(testPackageElement);
        makeEqualityChecks(packageResource, result2.orElse(null));

        return packageResource;
    }

    @Override
    protected Function<ClassPathIndexingContext, PackageResourceFactory> factoryCreator() {
        return PackageResourceFactory::new;
    }

    @Override
    protected Set<ElementKind> expectedSupportedKinds() {
        return Set.of(ElementKind.PACKAGE);
    }
}
