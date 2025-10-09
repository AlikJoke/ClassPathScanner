package ru.joke.classpath.indexer.internal.factories;

import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.PackageResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;
import ru.joke.classpath.indexer.test_util.TestModuleElement;
import ru.joke.classpath.indexer.test_util.TestPackageElement;
import ru.joke.classpath.indexer.test_util.TestTypeElement;
import ru.joke.classpath.indexer.test_util.fixtures.TestClass;

import javax.lang.model.element.PackageElement;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PackageResourceFactoryTest extends AbsClassPathResourceFactoryTest<PackageElement, PackageResource, PackageResourceFactory> {

    @Test
    void testPackageInNamedModule() {
        final var packageResource = executeTestsForPackageOfClass(TestClass.class);

        final var expectedAnnotationsMap =
                Set.of(ClassPathIndexed.class, Documented.class, Retention.class, Target.class)
                        .stream()
                        .map(TestTypeElement::new)
                        .collect(Collectors.toMap(e -> e.getQualifiedName().toString(), Function.identity()));

        final var actualAnnotations =
                packageResource.annotations()
                                .stream()
                                .map(ClassPathResource.ClassReference::canonicalName)
                                .collect(Collectors.toSet());

        assertEquals(expectedAnnotationsMap.keySet(), actualAnnotations, "Annotations must be equal");
    }

    @Test
    void testPackageInUnnamedModule() {
        final var packageResource = executeTestsForPackageOfClass(Answer.class);
        assertTrue(packageResource.annotations().isEmpty(), "Annotations must be empty");
    }

    private PackageResource executeTestsForPackageOfClass(final Class<?> clazz) {
        final var moduleElement = new TestModuleElement(clazz.getModule());
        final var testPackage = clazz.getPackage();
        final var testPackageElement = new TestPackageElement(testPackage, moduleElement);

        final var aliasesFromConfig = Map.of(testPackage.getName(), Set.of("p1", "p2"));
        final var result = prepareFactory(aliasesFromConfig, testPackageElement, moduleElement).create(testPackageElement);

        assertNotNull(result, "Result must be not null");
        assertTrue(result.isPresent(), "Result must be not empty");

        final var packageResource = result.get();

        assertEquals(testPackage.getName(), packageResource.name(), "Name of the package must be equal");
        assertTrue(packageResource.packageName().isEmpty(), "Package of the package must be empty");
        if (clazz.getModule().isNamed()) {
            assertEquals(clazz.getModule().getName(), packageResource.module(), "Module of the resource must be equal");
        } else {
            assertTrue(packageResource.module().isEmpty(), "Module of the resource must be empty");
        }

        assertEquals(ClassPathResource.Type.PACKAGE, packageResource.type(), "Type must be equal");
        assertThrows(UnsupportedOperationException.class, packageResource::asPackage);

        if (testPackage.isSealed()) {
            assertEquals(1, packageResource.modifiers().size(), "Modifiers count must be equal");
            assertTrue(packageResource.modifiers().contains(ClassPathResource.Modifier.SEALED), "Package should be sealed");
        } else {
            assertTrue(packageResource.modifiers().isEmpty(), "Modifiers must be empty");
        }

        assertTrue(packageResource.aliases().containsAll(aliasesFromConfig.get(testPackage.getName())), "Aliases must contain all aliases from config");

        final ClassPathIndexed annotation = testPackage.getAnnotation(ClassPathIndexed.class);
        if (annotation != null) {
            assertEquals(3, packageResource.aliases().size(), "Aliases count must be equal");
            assertTrue(
                    packageResource.aliases().containsAll(List.of(annotation.value())),
                    "Aliases must contain all aliases from annotation under package"
            );
        } else {
            assertEquals(2, packageResource.aliases().size(), "Aliases count must be equal");
        }

        return packageResource;
    }

    @Override
    protected Function<ClassPathIndexingContext, PackageResourceFactory> factoryCreator() {
        return PackageResourceFactory::new;
    }
}
