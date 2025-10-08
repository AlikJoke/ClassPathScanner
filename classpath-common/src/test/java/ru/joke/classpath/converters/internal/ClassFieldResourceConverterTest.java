package ru.joke.classpath.converters.internal;

import ru.joke.classpath.ClassFieldResource;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.fixtures.TestAnnotation2;
import ru.joke.classpath.fixtures.TestClass;

import java.lang.annotation.Inherited;
import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClassFieldResourceConverterTest extends AbsClassPathResourceConverterTest<ClassFieldResource, ClassFieldResourceConverter> {

    private static final String EXPECTED_STR = "f|f;t;pv|0|1|6#2|3|4;5";

    @Override
    void makeTypeSpecificChecks(ClassFieldResource expected, ClassFieldResource actual) throws Exception {

        final var javaField = actual.asField();
        assertNotNull(javaField, "Java field object must be not null");
        assertEquals(expected.name(), javaField.getName(), "Name of the field must be equal");

        javaField.setAccessible(true);
        final String value = (String) javaField.get(new TestClass.StaticNested.Inner());
        assertEquals("v1", value,"Value of the field must be equal");
    }

    @Override
    ClassFieldResourceConverter createConverter() {
        return new ClassFieldResourceConverter();
    }

    @Override
    ClassPathResource.Type testResourceType() {
        return ClassPathResource.Type.FIELD;
    }

    @Override
    ClassFieldResource createTestResource() {
        return new ClassFieldResource() {

            @Override
            public ClassReference<?> owner() {
                return new ClassReferenceImpl<>(TestClass.StaticNested.Inner.class.getName());
            }

            @Override
            public Field asField(ClassLoader loader) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String name() {
                return "var";
            }

            @Override
            public Set<String> aliases() {
                return Set.of("var1");
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
                return EnumSet.of(Modifier.PRIVATE, Modifier.FINAL, Modifier.TRANSIENT);
            }
        };
    }

    @Override
    String getExpectedStringRepresentation() {
        return EXPECTED_STR;
    }

    @Override
    void fillDictionary(Map<String, String> dictionaryMap, ClassFieldResource resource) {
        super.fillDictionary(dictionaryMap, resource);
        dictionaryMap.put(String.valueOf(dictionaryMap.size()), resource.owner().binaryName().substring(resource.packageName().length() + 1));
    }
}