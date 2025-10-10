package ru.joke.classpath.converters.internal;

import ru.joke.classpath.ClassConstructorResource;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.fixtures.TestClass;

import java.lang.reflect.Constructor;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClassConstructorResourceConverterTest extends AbsClassPathResourceConverterTest<ClassConstructorResource<?>, ClassConstructorResourceConverter> {

    private static final String EXPECTED_STR = "cr|pd|0|1|3#2#4,4,5,6,7||";

    @Override
    void makeTypeSpecificChecks(ClassConstructorResource<?> expected, ClassConstructorResource<?> actual) throws Exception {

        final var javaConstructor = actual.asConstructor();
        assertNotNull(javaConstructor, "Java constructor object must be not null");

        assertEquals(expected.parameters().size(), actual.parameters().size(), "Count of constructor parameters must be equal");
        for (int i = 0; i < expected.parameters().size(); i++) {
            final var expectedParam = expected.parameters().get(i);
            final var actualParam = actual.parameters().get(i);

            assertEquals(expectedParam.binaryName(), actualParam.binaryName(), "Binary name of the parameter must be equal");
            assertEquals(expectedParam.toClass(), actualParam.toClass(), "Parameter type must be equal");
        }

        javaConstructor.setAccessible(true);
        assertNotNull(javaConstructor.newInstance("", "", 0, new String[0], new int[0]));
    }

    @Override
    ClassConstructorResourceConverter createConverter() {
        return new ClassConstructorResourceConverter();
    }

    @Override
    ClassPathResource.Type testResourceType() {
        return ClassPathResource.Type.CONSTRUCTOR;
    }

    @Override
    ClassConstructorResource<?> createTestResource() {
        return new ClassConstructorResource<>() {

            @Override
            public Constructor<Object> asConstructor(ClassLoader loader) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<ClassReference<?>> parameters() {
                return List.of(
                        new ClassReferenceImpl<>(String.class.getName()),
                        new ClassReferenceImpl<>(String.class.getName()),
                        new ClassReferenceImpl<>(int.class.getCanonicalName()),
                        new ClassReferenceImpl<>(String[].class.getName()),
                        new ClassReferenceImpl<>(int[].class.getName())
                );
            }

            @Override
            public ClassReference<?> owner() {
                return new ClassReferenceImpl<>(TestClass.StaticNested.Inner.class.getName());
            }

            @Override
            public String id() {
                return module() + '/' + owner().binaryName() + ID_SEPARATOR + name()
                        + '(' + String.class.getName() + ',' + String.class.getName() + ',' + int.class.getCanonicalName() + ',' + String[].class.getName() + ',' + int[].class.getName() + ")";
            }

            @Override
            public String name() {
                return "<cinit>";
            }

            @Override
            public Set<String> aliases() {
                return Collections.emptySet();
            }

            @Override
            public String module() {
                return getClass().getModule().getName();
            }

            @Override
            public Set<ClassReference<?>> annotations() {
                return Collections.emptySet();
            }

            @Override
            public String packageName() {
                return TestClass.class.getPackageName();
            }

            @Override
            public Set<Modifier> modifiers() {
                return EnumSet.of(Modifier.PROTECTED);
            }
        };
    }

    @Override
    String getExpectedStringRepresentation() {
        return EXPECTED_STR;
    }

    @Override
    void fillDictionary(Map<String, String> dictionaryMap, ClassConstructorResource<?> resource) {
        super.fillDictionary(dictionaryMap, resource);
        dictionaryMap.put(String.valueOf(dictionaryMap.size()), resource.owner().binaryName().substring(resource.packageName().length() + 1));
        resource.parameters().forEach(p -> {
            if (!dictionaryMap.containsValue(p.binaryName())) {
                dictionaryMap.put(String.valueOf(dictionaryMap.size()), p.binaryName());
            }
        });
    }
}