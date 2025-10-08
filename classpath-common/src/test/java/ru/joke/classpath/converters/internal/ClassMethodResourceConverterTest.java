package ru.joke.classpath.converters.internal;

import ru.joke.classpath.ClassMethodResource;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.fixtures.TestAnnotation2;
import ru.joke.classpath.fixtures.TestClass;

import java.lang.annotation.Inherited;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClassMethodResourceConverterTest extends AbsClassPathResourceConverterTest<ClassMethodResource, ClassMethodResourceConverter> {

    private static final String EXPECTED_STR = "md|pv|0|1|6#2#7;8;9|3|4;5";

    @Override
    void makeTypeSpecificChecks(ClassMethodResource expected, ClassMethodResource actual) throws Exception {

        final var javaMethod = actual.asMethod();
        assertNotNull(javaMethod, "Java method object must be not null");
        assertEquals(expected.name(), javaMethod.getName(), "Name of the method must be equal");

        assertEquals(expected.parameters().size(), actual.parameters().size(), "Count of method parameters must be equal");
        for (int i = 0; i < expected.parameters().size(); i++) {
            final var expectedParam = expected.parameters().get(i);
            final var actualParam = actual.parameters().get(i);

            assertEquals(expectedParam.binaryName(), actualParam.binaryName(), "Binary name of the parameter must be equal");
            assertEquals(expectedParam.toClass(), actualParam.toClass(), "Parameter type must be equal");
        }

        javaMethod.setAccessible(true);
        final String value = (String) javaMethod.invoke(new TestClass.StaticNested.Inner(), "", 0, new TestClass.StaticNested());
        assertEquals("v1", value,"Value of the field must be equal");
    }

    @Override
    ClassMethodResourceConverter createConverter() {
        return new ClassMethodResourceConverter();
    }

    @Override
    ClassPathResource.Type testResourceType() {
        return ClassPathResource.Type.METHOD;
    }

    @Override
    ClassMethodResource createTestResource() {
        return new ClassMethodResource() {

            @Override
            public Method asMethod(ClassLoader loader) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<ClassReference<?>> parameters() {
                return List.of(
                        new ClassReferenceImpl<>(String.class.getName()),
                        new ClassReferenceImpl<>(int.class.getCanonicalName()),
                        new ClassReferenceImpl<>(TestClass.StaticNested.class.getName())
                );
            }

            @Override
            public ClassReference<?> owner() {
                return new ClassReferenceImpl<>(TestClass.StaticNested.Inner.class.getName());
            }

            @Override
            public String id() {
                return module() + '/' + owner().binaryName() + ID_SEPARATOR + name() + '(' + String.class.getName() + ';' + int.class.getCanonicalName() + ';' + TestClass.StaticNested.class.getName() + ")";
            }

            @Override
            public String name() {
                return "getVar";
            }

            @Override
            public Set<String> aliases() {
                return Set.of("v1");
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
                return EnumSet.of(Modifier.PRIVATE);
            }
        };
    }

    @Override
    String getExpectedStringRepresentation() {
        return EXPECTED_STR;
    }

    @Override
    void fillDictionary(Map<String, String> dictionaryMap, ClassMethodResource resource) {
        super.fillDictionary(dictionaryMap, resource);
        dictionaryMap.put(String.valueOf(dictionaryMap.size()), resource.owner().binaryName().substring(resource.packageName().length() + 1));
        resource.parameters().forEach(p -> {
            if (!dictionaryMap.containsValue(p.binaryName())) {
                dictionaryMap.put(String.valueOf(dictionaryMap.size()), p.binaryName());
            }
        });
    }
}