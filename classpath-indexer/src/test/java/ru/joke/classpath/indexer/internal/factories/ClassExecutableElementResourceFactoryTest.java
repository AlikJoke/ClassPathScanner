package ru.joke.classpath.indexer.internal.factories;

import org.junit.jupiter.api.Test;
import ru.joke.classpath.ClassMemberResource;
import ru.joke.classpath.ClassMethodResource;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;
import ru.joke.classpath.indexer.test_util.TestExecutableElement;
import ru.joke.classpath.indexer.test_util.TestModuleElement;
import ru.joke.classpath.indexer.test_util.TestTypeElement;
import ru.joke.classpath.indexer.test_util.fixtures.TestClass;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static ru.joke.classpath.ClassMemberResource.Executable.*;

class ClassExecutableElementResourceFactoryTest extends AbsClassPathResourceFactoryTest<ExecutableElement, ClassMemberResource.Executable, ClassExecutableElementResourceFactory> {

    @Test
    void testFinalIntClassMethod() throws NoSuchMethodException {
        final var method = TestClass.class.getDeclaredMethod("getIntField");
        executeTests(
                method,
                EnumSet.of(ClassPathResource.Modifier.FINAL),
                Set.of(ClassPathIndexed.class, Documented.class, Retention.class, Target.class)
        );
    }

    @Test
    void testClassConstructorWithArg() throws NoSuchMethodException {
        final var constructor = TestClass.class.getDeclaredConstructor(int.class);
        executeTests(
                constructor,
                EnumSet.noneOf(ClassPathResource.Modifier.class),
                Set.of(ClassPathIndexed.class, Documented.class, Retention.class, Target.class)
        );
    }

    @Test
    void testClassConstructorWithVarArgs() throws NoSuchMethodException {
        final var constructor = TestClass.class.getDeclaredConstructor(int[].class);
        executeTests(
                constructor,
                EnumSet.noneOf(ClassPathResource.Modifier.class),
                Set.of(ClassPathIndexed.class, Documented.class, Retention.class, Target.class)
        );
    }

    @Test
    void testStaticRefMethodInNestedClass() throws NoSuchMethodException {
        final var method = TestClass.NestedClass.class.getDeclaredMethod("getStringField");
        executeTests(
                method,
                EnumSet.of(ClassPathResource.Modifier.STATIC),
                Set.of(ClassPathIndexed.class, Documented.class, Retention.class, Target.class)
        );
    }

    @Test
    void testNestedClassConstructor() throws NoSuchMethodException {
        final var constructor = TestClass.NestedClass.class.getDeclaredConstructor();
        executeTests(
                constructor,
                EnumSet.of(ClassPathResource.Modifier.PUBLIC),
                Set.of(ClassPathIndexed.class, Documented.class, Retention.class, Target.class)
        );
    }

    @Test
    void testNestedClassConstructorWithArrayParam() throws NoSuchMethodException {
        final var constructor = TestClass.NestedClass.class.getDeclaredConstructor(int[].class);
        executeTests(
                constructor,
                EnumSet.of(ClassPathResource.Modifier.PUBLIC),
                Collections.emptySet()
        );
    }

    @Test
    void testEnumMethod() throws NoSuchMethodException {
        final var method = TestClass.Enum.class.getDeclaredMethod("enumMethod", TestClass.Enum.class);
        executeTests(
                method,
                EnumSet.of(ClassPathResource.Modifier.SYNCHRONIZED),
                Collections.emptySet()
        );
    }

    @Test
    void testEnumConstructor() throws NoSuchMethodException {
        final var constructor = TestClass.Enum.class.getDeclaredConstructor(String.class, int.class);
        executeTests(
                constructor,
                EnumSet.of(ClassPathResource.Modifier.PRIVATE),
                Collections.emptySet()
        );
    }

    @Test
    void testNativeClassMethod() throws NoSuchMethodException {
        final var method = TestClass.class.getDeclaredMethod("nativeMethod");
        executeTests(
                method,
                EnumSet.of(ClassPathResource.Modifier.NATIVE),
                Collections.emptySet()
        );
    }

    @Test
    void testInterfaceStaticMethod() throws NoSuchMethodException {
        final var method = TestClass.Interface.class.getDeclaredMethod("staticInterfaceMethod", TestClass.NestedClass.class, int.class, String.class);
        executeTests(
                method,
                EnumSet.of(ClassPathResource.Modifier.PRIVATE, ClassPathResource.Modifier.STATIC),
                Collections.emptySet()
        );
    }

    @Test
    void testInterfaceDefaultMethod() throws NoSuchMethodException {
        final var method = TestClass.Interface.class.getDeclaredMethod("getRef");
        executeTests(
                method,
                EnumSet.of(ClassPathResource.Modifier.PUBLIC, ClassPathResource.Modifier.DEFAULT),
                Set.of(Documented.class, Retention.class, Target.class, Deprecated.class)
        );
    }

    @Test
    void testRecordGetterMethod() throws NoSuchMethodException {
        final var method = TestClass.Record.class.getDeclaredMethod("intField");
        executeTests(
                method,
                EnumSet.of(ClassPathResource.Modifier.PUBLIC),
                Set.of(ClassPathIndexed.class, Deprecated.class, Documented.class, Retention.class, Target.class)
        );
    }

    @Test
    void testRecordConstructor() throws NoSuchMethodException {
        final var constructor = TestClass.Record.class.getDeclaredConstructor(int.class);
        executeTests(
                constructor,
                EnumSet.of(ClassPathResource.Modifier.PUBLIC),
                Set.of(ClassPathIndexed.class, Deprecated.class, Documented.class, Retention.class, Target.class)
        );
    }

    void executeTests(
            final Executable executable,
            final Set<ClassPathResource.Modifier> expectedModifiers,
            final Set<Class<?>> expectedAnnotationTypes
    ) {
        final var testFieldElement = new TestExecutableElement(executable);
        final var ownerType = executable.getDeclaringClass();
        final var moduleElement = new TestModuleElement(ownerType.getModule());

        final var expectedMethodParamsStr =
                Arrays.stream(executable.getParameters())
                        .map(p -> p.getType().getName())
                        .collect(Collectors.joining(PARAMETERS_DELIMITER, PARAMETERS_SIGNATURE_START_BRACKET, PARAMETERS_SIGNATURE_END_BRACKET));
        final var expectedName = executable instanceof Constructor<?> ? "<cinit>" : executable.getName();
        final var expectedId = ownerType.getModule().getName() + MODULE_SEPARATOR
                + ownerType.getName() + ClassMethodResource.ID_SEPARATOR + expectedName
                + expectedMethodParamsStr;

        final var aliasesFromConfig = Map.of(expectedId, Set.of("test_" + executable.getName()));
        final var factory = prepareFactory(aliasesFromConfig, new TestTypeElement(ownerType), moduleElement);
        final var result = factory.create(testFieldElement);

        assertNotNull(result, "Result must be not null");
        assertTrue(result.isPresent(), "Result must be not empty");

        final var resource = result.get();

        assertEquals(expectedName, resource.name(), "Name of the resource must be equal");
        assertEquals(ownerType.getPackageName(), resource.packageName(), "Package name must be equal");
        assertEquals(ownerType.getModule().getName(), resource.module(), "Module name must be equal");
        if (executable instanceof Method) {
            assertEquals(ClassPathResource.Type.METHOD, resource.type(), "Resource type must be equal");
        } else {
            assertEquals(ClassPathResource.Type.CONSTRUCTOR, resource.type(), "Resource type must be equal");
        }
        
        if (resource instanceof ClassMethodResource m) {
            assertThrows(UnsupportedOperationException.class, m::asMethod);
        }

        makeAnnotationsCheck(
                expectedAnnotationTypes,
                resource
        );
        makeAliasesCheck(
                executable,
                resource,
                aliasesFromConfig.get(expectedId)
        );

        assertEquals(expectedModifiers, resource.modifiers(), "Modifiers must be equal");
        assertThrows(UnsupportedOperationException.class, resource.owner()::toClass);
        assertEquals(ownerType.getCanonicalName(), resource.owner().canonicalName(), "Owner type must be equal");
        assertEquals(ownerType.getName(), resource.owner().binaryName(), "Owner type must be equal");

        assertEquals(executable.getParameterCount(), resource.parameters().size(), "Parameters count must be equal");
        for (int i = 0; i < executable.getParameters().length; i++) {
            final var expectedParam = executable.getParameters()[i];
            final var actualParam = resource.parameters().get(i);

            assertEquals(expectedParam.getType().getName(), actualParam.binaryName(), "Type of the parameter must be equal");
        }

        assertEquals(expectedId, resource.id(), "Id must be equal");
    }

    @Override
    protected Function<ClassPathIndexingContext, ClassExecutableElementResourceFactory> factoryCreator() {
        return ClassExecutableElementResourceFactory::new;
    }

    @Override
    protected Set<ElementKind> expectedSupportedKinds() {
        return Set.of(ElementKind.METHOD, ElementKind.CONSTRUCTOR);
    }
}
