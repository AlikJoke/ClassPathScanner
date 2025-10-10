package ru.joke.classpath.indexer.internal.factories;

import org.junit.jupiter.api.Test;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ClassResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;
import ru.joke.classpath.indexer.test_util.TestModuleElement;
import ru.joke.classpath.indexer.test_util.TestTypeElement;
import ru.joke.classpath.indexer.test_util.fixtures.TestClass;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.io.Externalizable;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.constant.Constable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static ru.joke.classpath.ClassPathResource.MODULE_SEPARATOR;

class ClassResourceFactoryTest extends AbsClassPathResourceFactoryTest<TypeElement, ClassResource<?>, ClassResourceFactory> {

    @Test
    void testTopLevelClass() {
        executeTests(
                TestClass.class,
                TestClass.class,
                EnumSet.of(ClassPathResource.Modifier.PUBLIC, ClassPathResource.Modifier.ABSTRACT),
                Set.of(ClassPathIndexed.class, Documented.class, Retention.class, Target.class),
                TestClass.class.getSimpleName(),
                Collections.emptySet()
        );
    }

    @Test
    void testNestedClass() {
        executeTests(
                TestClass.class,
                TestClass.NestedClass.class,
                EnumSet.of(ClassPathResource.Modifier.PUBLIC, ClassPathResource.Modifier.STATIC),
                Set.of(ClassPathIndexed.class, Documented.class, Retention.class, Target.class),
                TestClass.class.getSimpleName() + "$" + TestClass.NestedClass.class.getSimpleName(),
                Collections.emptySet()
        );
    }

    @Test
    void testEnumClass() {
        executeTests(
                TestClass.class,
                TestClass.Enum.class,
                EnumSet.of(ClassPathResource.Modifier.SEALED, ClassPathResource.Modifier.PUBLIC, ClassPathResource.Modifier.STATIC),
                Collections.emptySet(),
                TestClass.class.getSimpleName() + "$" + TestClass.Enum.class.getSimpleName(),
                Set.of(Constable.class, Serializable.class, Comparable.class)
        );
    }

    @Test
    void testInterfaceClass() {
        executeTests(
                TestClass.class,
                TestClass.Interface.class,
                EnumSet.of(ClassPathResource.Modifier.ABSTRACT, ClassPathResource.Modifier.PUBLIC, ClassPathResource.Modifier.STATIC),
                Collections.emptySet(),
                TestClass.class.getSimpleName() + "$" + TestClass.Interface.class.getSimpleName(),
                Set.of(Externalizable.class, Serializable.class, Cloneable.class)
        );
    }

    @Test
    void testNestedClassInInterface() {
        executeTests(
                TestClass.class,
                TestClass.Interface.Test.class,
                EnumSet.of(ClassPathResource.Modifier.PUBLIC, ClassPathResource.Modifier.ABSTRACT, ClassPathResource.Modifier.STATIC),
                Collections.emptySet(),
                TestClass.class.getSimpleName() + "$" + TestClass.Interface.class.getSimpleName() + "$" + TestClass.Interface.Test.class.getSimpleName(),
                Set.of(TestClass.Interface.class, Externalizable.class, Serializable.class, Cloneable.class)
        );
    }

    @Test
    void testNestedAnnotationInInterface() {
        executeTests(
                TestClass.class,
                TestClass.Interface.TestAnnotation.class,
                EnumSet.of(ClassPathResource.Modifier.PUBLIC, ClassPathResource.Modifier.ABSTRACT, ClassPathResource.Modifier.STATIC),
                Set.of(Documented.class, Retention.class, Target.class),
                TestClass.class.getSimpleName() + "$" + TestClass.Interface.class.getSimpleName() + "$" + TestClass.Interface.TestAnnotation.class.getSimpleName(),
                Set.of(Annotation.class)
        );
    }

    @Test
    void testRecord() {
        executeTests(
                TestClass.class,
                TestClass.Record.class,
                EnumSet.of(ClassPathResource.Modifier.PUBLIC, ClassPathResource.Modifier.FINAL, ClassPathResource.Modifier.STATIC),
                Collections.emptySet(),
                TestClass.class.getSimpleName() + "$" + TestClass.Record.class.getSimpleName(),
                Set.of(TestClass.Interface.class, Externalizable.class, Serializable.class, Cloneable.class)
        );
    }

    void executeTests(
            final Class<?> rootType,
            final Class<?> targetType,
            final Set<ClassPathResource.Modifier> expectedModifiers,
            final Set<Class<?>> expectedAnnotationTypes,
            final String expectedName,
            final Set<Class<?>> expectedInterfaces
    ) {
        final var testTypeElement = new TestTypeElement(targetType);
        final var moduleElement = new TestModuleElement(targetType.getModule());

        final var expectedId = targetType.getModule().getName() + MODULE_SEPARATOR + targetType.getName();
        final var aliasesFromConfig = Map.of(expectedId, Set.of("test", "f1"));
        final var factory = prepareFactory(
                aliasesFromConfig,
                s -> new LoadClassFunction().apply(toBinaryClassName(rootType, s)),
                new TestTypeElement(rootType), moduleElement
        );
        final var result = factory.create(testTypeElement);

        assertNotNull(result, "Result must be not null");
        assertTrue(result.isPresent(), "Result must be not empty");

        final var classResource = result.get();

        assertEquals(expectedName, classResource.name(), "Name of the class must be equal");
        assertEquals(targetType.getPackageName(), classResource.packageName(), "Package name must be equal");
        assertEquals(targetType.getModule().getName(), classResource.module(), "Module name must be equal");
        assertEquals(ClassPathResource.Type.CLASS, classResource.type(), "Resource type must be equal");
        assertThrows(UnsupportedOperationException.class, classResource::asClass);

        final ClassResource.Kind expectedKind =
                targetType.isEnum()
                        ? ClassResource.Kind.ENUM
                        : targetType.isAnnotation()
                            ? ClassResource.Kind.ANNOTATION
                            : targetType.isRecord()
                                ? ClassResource.Kind.RECORD
                                : targetType.isInterface()
                                    ? ClassResource.Kind.INTERFACE
                                    : ClassResource.Kind.CLASS;

        assertEquals(expectedKind, classResource.kind(), "Class kind must be equal");

        makeAnnotationsCheck(
                expectedAnnotationTypes,
                classResource
        );
        makeAliasesCheck(
                targetType,
                classResource,
                aliasesFromConfig.get(expectedId)
        );

        assertEquals(expectedModifiers, classResource.modifiers(), "Modifiers must be equal");
        assertEquals(expectedId, classResource.id(), "Id must be equal");

        final List<Class<?>> superClasses = new ArrayList<>();
        collectParents(targetType, superClasses);

        assertEquals(superClasses.size(), classResource.superClasses().size(), "Superclasses count must be equal");
        for (int i = 0; i < superClasses.size(); i++) {
            final var expectedParent = superClasses.get(i);
            final var actualParent = classResource.superClasses().get(i);

            assertEquals(expectedParent.getName(), actualParent.binaryName(), "Superclass name must be equal");
        }

        assertEquals(expectedInterfaces.size(), classResource.interfaces().size(), "Interfaces count must be equal");
        final var expectedInterfacesNames =
                expectedInterfaces
                        .stream()
                        .map(Class::getName)
                        .collect(Collectors.toSet());
        final var actualInterfacesNames =
                classResource.interfaces()
                        .stream()
                        .map(ClassPathResource.ClassReference::binaryName)
                        .collect(Collectors.toSet());
        assertEquals(expectedInterfacesNames, actualInterfacesNames, "Interfaces must be equal");
    }

    @Override
    protected Function<ClassPathIndexingContext, ClassResourceFactory> factoryCreator() {
        return ClassResourceFactory::new;
    }

    @Override
    protected Set<ElementKind> expectedSupportedKinds() {
        return Set.of(ElementKind.ANNOTATION_TYPE, ElementKind.ENUM, ElementKind.CLASS, ElementKind.INTERFACE, ElementKind.RECORD);
    }

    private String toBinaryClassName(final Class<?> rootType, final String canonicalName) {
        return canonicalName.startsWith(rootType.getPackageName())
                ? rootType.getPackageName() + "." + canonicalName.substring(rootType.getPackageName().length() + 1).replace('.', '$')
                : canonicalName;
    }

    private void collectParents(final Class<?> type, final List<Class<?>> result) {
        if (type.getSuperclass() == null || type.getSuperclass() == Object.class) {
            return;
        }

        result.add(type.getSuperclass());
        collectParents(type.getSuperclass(), result);
    }
}
