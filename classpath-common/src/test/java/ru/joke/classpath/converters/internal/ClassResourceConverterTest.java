package ru.joke.classpath.converters.internal;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ClassResource;
import ru.joke.classpath.fixtures.TestAnnotation2;
import ru.joke.classpath.fixtures.TestClass;
import ru.joke.classpath.fixtures.TestInterface;

import java.io.Serializable;
import java.lang.annotation.Inherited;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClassResourceConverterTest extends AbsClassPathResourceConverterTest<ClassResource<?>, ClassResourceConverter> {

    private static final String EXPECTED_STR = "c|f;pc;st|0|1|2|3;4|5;6|7;8|9;10|c";

    @Override
    void makeTypeSpecificChecks(ClassResource<?> expected, ClassResource<?> actual) throws Exception {

        assertEquals(expected.kind(), actual.kind(), "Kind of class must be equal");

        final var cls = actual.asClass();
        assertNotNull(cls, "Java class object must be not null");

        assertEquals(expected.superClasses().size(), actual.superClasses().size(), "Count of superclasses must be equal");
        for (int i = 0; i < expected.superClasses().size(); i++) {
            final var expectedSuperclass = expected.superClasses().get(i);
            final var actualSuperclass = actual.superClasses().get(i);

            assertEquals(expectedSuperclass.binaryName(), actualSuperclass.binaryName(), "Binary name of the superclass must be equal");
            assertEquals(expectedSuperclass.toClass(), actualSuperclass.toClass(), "Superclass type must be equal");
        }

        assertEquals(expected.interfaces().size(), actual.interfaces().size(), "Count of interfaces must be equal");
        final var actualInterfacesMap =
                actual.interfaces()
                        .stream()
                        .collect(Collectors.toMap(ClassPathResource.ClassReference::binaryName, Function.identity()));
        for (final var expectedInterface : expected.interfaces()) {
            final var actualInterface = actualInterfacesMap.get(expectedInterface.binaryName());

            assertNotNull(actualInterface, "Interface must be not null");
            assertEquals(expectedInterface.toClass(), actualInterface.toClass(), "Interface type must be equal");
        }
    }

    @Override
    ClassResourceConverter createConverter() {
        return new ClassResourceConverter();
    }

    @Override
    ClassPathResource.Type testResourceType() {
        return ClassPathResource.Type.CLASS;
    }

    @Override
    ClassResource<?> createTestResource() {
        return new ClassResource<>() {

            @Override
            public Class<Object> asClass(ClassLoader loader) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Set<ClassReference<?>> interfaces() {
                final Set<ClassReference<?>> interfaces = new LinkedHashSet<>();
                interfaces.add(new ClassReferenceImpl<>(TestInterface.class.getName()));
                interfaces.add(new ClassReferenceImpl<>(Serializable.class.getName()));

                return interfaces;
            }

            @Override
            public List<ClassReference<?>> superClasses() {
                return List.of(
                        new ClassReferenceImpl<>(TestClass.StaticNested.class.getName()),
                        new ClassReferenceImpl<>(TestClass.class.getName())
                );
            }

            @Override
            public Kind kind() {
                return Kind.CLASS;
            }

            @Override
            public String name() {
                return TestClass.class.getSimpleName() + "$" + TestClass.StaticNested.class.getSimpleName() + "$" + TestClass.StaticNested.Inner.class.getSimpleName();
            }

            @Override
            public Set<String> aliases() {
                return Set.of("c1", "c2");
            }

            @Override
            public String module() {
                return getClass().getModule().getName();
            }

            @Override
            public Set<ClassReference<?>> annotations() {
                final Set<ClassReference<?>> result = new LinkedHashSet<>();
                result.add(new ClassReferenceImpl<>(TestAnnotation2.class.getName()));
                result.add(new ClassReferenceImpl<>(Inherited.class.getName()));

                return result;
            }

            @Override
            public String packageName() {
                return TestClass.class.getPackageName();
            }

            @Override
            public Set<Modifier> modifiers() {
                return EnumSet.of(Modifier.STATIC, Modifier.PUBLIC, Modifier.FINAL);
            }
        };
    }

    @Override
    String getExpectedStringRepresentation() {
        return EXPECTED_STR;
    }

    @Override
    void fillDictionary(Map<String, String> dictionaryMap, ClassResource<?> resource) {
        super.fillDictionary(dictionaryMap, resource);
        for (var iface : resource.interfaces()) {
            if (!dictionaryMap.containsValue(iface.binaryName())) {
                dictionaryMap.put(String.valueOf(dictionaryMap.size()), iface.binaryName());
            }
        }
        for (var clazz : resource.superClasses()) {
            if (!dictionaryMap.containsValue(clazz.binaryName())) {
                dictionaryMap.put(String.valueOf(dictionaryMap.size()), clazz.binaryName());
            }
        }
    }
}