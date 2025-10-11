package ru.joke.classpath.indexer.internal.factories;

import org.junit.jupiter.api.Test;
import ru.joke.classpath.ClassFieldResource;
import ru.joke.classpath.ClassMemberResource;
import ru.joke.classpath.ClassPathIndexed;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;
import ru.joke.classpath.indexer.test_util.TestModuleElement;
import ru.joke.classpath.indexer.test_util.TestTypeElement;
import ru.joke.classpath.indexer.test_util.TestVariableElement;
import ru.joke.classpath.indexer.test_util.fixtures.TestClass;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static ru.joke.classpath.ClassPathResource.MODULE_SEPARATOR;

class ClassFieldResourceFactoryTest extends AbsClassPathResourceFactoryTest<VariableElement, ClassFieldResource, ClassFieldResourceFactory> {

    @Test
    void testIntClassField() throws NoSuchFieldException {
        executeTests(
                TestClass.class,
                "intField",
                EnumSet.of(ClassPathResource.Modifier.PRIVATE, ClassPathResource.Modifier.VOLATILE),
                Set.of(ClassPathIndexed.class, Documented.class, Retention.class, Target.class)
        );
    }

    @Test
    void testRefClassField() throws NoSuchFieldException {
        executeTests(
                TestClass.NestedClass.class,
                "stringField",
                EnumSet.of(ClassPathResource.Modifier.PRIVATE, ClassPathResource.Modifier.STATIC, ClassPathResource.Modifier.FINAL),
                Set.of(ClassPathIndexed.class, Documented.class, Retention.class, Target.class)
        );
    }

    @Test
    void testEnumConstantField() throws NoSuchFieldException {
        executeTests(
                TestClass.Enum.class,
                "E1",
                EnumSet.of(ClassPathResource.Modifier.STATIC, ClassPathResource.Modifier.FINAL, ClassPathResource.Modifier.PUBLIC),
                Set.of()
        );
    }

    @Test
    void testRecordField() throws NoSuchFieldException {
        executeTests(
                TestClass.Record.class,
                "intField",
                EnumSet.of(ClassPathResource.Modifier.PRIVATE, ClassPathResource.Modifier.FINAL),
                Set.of(ClassPathIndexed.class, Documented.class, Retention.class, Target.class, Deprecated.class)
        );
    }

    @Test
    void testInterfaceField() throws NoSuchFieldException {
        executeTests(
                TestClass.Interface.class,
                "ref",
                EnumSet.of(ClassPathResource.Modifier.PUBLIC, ClassPathResource.Modifier.STATIC, ClassPathResource.Modifier.FINAL),
                Set.of(Deprecated.class, Documented.class, Retention.class, Target.class)
        );
    }

    void executeTests(
            final Class<?> ownerType,
            final String fieldName,
            final Set<ClassPathResource.Modifier> expectedModifiers,
            final Set<Class<?>> expectedAnnotationTypes
    ) throws NoSuchFieldException {
        final var field = ownerType.getDeclaredField(fieldName);
        final var testFieldElement = new TestVariableElement(field);
        final var moduleElement = new TestModuleElement(ownerType.getModule());

        final var expectedId = ownerType.getModule().getName() + MODULE_SEPARATOR + ownerType.getName() + ClassMemberResource.ID_SEPARATOR + fieldName;
        final var aliasesFromConfig = Map.of(expectedId, Set.of("test", "f1"));
        final var factory = prepareFactory(aliasesFromConfig, new TestTypeElement(ownerType), moduleElement);
        final var result = factory.create(testFieldElement);

        assertNotNull(result, "Result must be not null");
        assertTrue(result.isPresent(), "Result must be not empty");

        final var fieldResource = result.get();

        assertEquals(field.getName(), fieldResource.name(), "Name of the field must be equal");
        assertEquals(ownerType.getPackageName(), fieldResource.packageName(), "Package name must be equal");
        assertEquals(ownerType.getModule().getName(), fieldResource.module(), "Module name must be equal");
        assertEquals(ClassPathResource.Type.FIELD, fieldResource.type(), "Resource type must be equal");
        assertThrows(UnsupportedOperationException.class, fieldResource::asField);

        makeAnnotationsCheck(
                expectedAnnotationTypes,
                fieldResource
        );
        makeAliasesCheck(
                field,
                fieldResource,
                aliasesFromConfig.get(expectedId)
        );

        assertEquals(expectedModifiers, fieldResource.modifiers(), "Modifiers must be equal");
        assertThrows(UnsupportedOperationException.class, fieldResource.owner()::toClass);
        assertEquals(ownerType.getCanonicalName(), fieldResource.owner().canonicalName(), "Owner type must be equal");
        assertEquals(ownerType.getName(), fieldResource.owner().binaryName(), "Owner type must be equal");
        assertEquals(expectedId, fieldResource.id(), "Id must be equal");

        final var result2 = factory.create(testFieldElement);
        makeEqualityChecks(fieldResource, result2.orElse(null));
    }

    @Override
    protected Function<ClassPathIndexingContext, ClassFieldResourceFactory> factoryCreator() {
        return ClassFieldResourceFactory::new;
    }

    @Override
    protected Set<ElementKind> expectedSupportedKinds() {
        return Set.of(ElementKind.FIELD, ElementKind.ENUM_CONSTANT);
    }
}
