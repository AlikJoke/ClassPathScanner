package ru.joke.classpath.converters;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.joke.classpath.*;
import ru.joke.classpath.converters.internal.ClassFieldResourceConverter;
import ru.joke.classpath.converters.internal.ClassResourceConverter;
import ru.joke.classpath.converters.internal.ModuleResourceConverter;
import ru.joke.classpath.converters.internal.PackageResourceConverter;
import ru.joke.classpath.test_util.TestDictionary;
import ru.joke.classpath.fixtures.TestClass;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class DelegateClassPathResourceConverterTest {

    private DelegateClassPathResourceConverter converter;

    @BeforeEach
    void setUp() {
        this.converter = new DelegateClassPathResourceConverter();
    }

    @Test
    void testClassResourceToString() {
        final var resource = createClassResource();
        final var dictionary = createDictionary(resource);

        final var expected = new ClassResourceConverter().toString(resource, dictionary);
        final var actual = this.converter.toString(resource, dictionary);
        assertEquals(expected, actual);
    }

    @Test
    void testModuleResourceToString() {
        final var resource = createModuleResource();
        final var dictionary = createDictionary(resource);

        final var expected = new ModuleResourceConverter().toString(resource, dictionary);
        final var actual = this.converter.toString(resource, dictionary);
        assertEquals(expected, actual);
    }

    @Test
    void testPackageResourceToString() {
        final var resource = createPackageResource();
        final var dictionary = createDictionary(resource);

        final var expected = new PackageResourceConverter().toString(resource, dictionary);
        final var actual = this.converter.toString(resource, dictionary);
        assertEquals(expected, actual);
    }

    @Test
    void testFieldResourceToString() {
        final var resource = createFieldResource();
        final var dictionary = createDictionary(resource);
        dictionary.addMapping(String.valueOf(dictionary.size()), TestClass.class.getSimpleName());

        final var expected = new ClassFieldResourceConverter().toString(resource, dictionary);
        final var actual = this.converter.toString(resource, dictionary);
        assertEquals(expected, actual);
    }

    @Test
    void testPackageResourceFromString() {
        final var stringResource = "p||0|0|||";
        final var packageResourceConverter = new PackageResourceConverter();
        final var dictionary = createDictionary(createPackageResource());

        final var expected = packageResourceConverter.fromString(stringResource, dictionary);
        final var actual = this.converter.fromString(stringResource, dictionary);

        assertTrue(expected.isPresent(), "Expected resource must present");
        assertTrue(actual.isPresent(), "Actual resource must present");

        makeCommonChecks(expected.get(), actual.get(), PackageResource.class);
    }

    @Test
    void testModuleResourceFromString() {
        final var stringResource = "m||0|0|||";
        final var moduleResourceConverter = new ModuleResourceConverter();
        final var dictionary = createDictionary(createModuleResource());

        final var expected = moduleResourceConverter.fromString(stringResource, dictionary);
        final var actual = this.converter.fromString(stringResource, dictionary);

        assertTrue(expected.isPresent(), "Expected resource must present");
        assertTrue(actual.isPresent(), "Actual resource must present");

        makeCommonChecks(expected.get(), actual.get(), ModuleResource.class);
    }

    @Test
    void testClassResourceFromString() {
        final var stringResource = "c||0|1||||||a";
        final var classResourceConverter = new ClassResourceConverter();
        final var dictionary = createDictionary(createClassResource());

        final var expected = classResourceConverter.fromString(stringResource, dictionary);
        final var actual = this.converter.fromString(stringResource, dictionary);

        assertTrue(expected.isPresent(), "Expected resource must present");
        assertTrue(actual.isPresent(), "Actual resource must present");

        final var actualResource = actual.get();

        final var expectedClass = expected.get();
        final var actualClass = (ClassResource<?>) actualResource;
        makeCommonChecks(expectedClass, actualClass, ClassResource.class);
        assertEquals(expectedClass.interfaces(), actualClass.interfaces(), "Class interfaces must be equal");
        assertEquals(expectedClass.superClasses(), actualClass.superClasses(), "Class superclasses must be equal");
        assertEquals(expectedClass.kind(), actualClass.kind(), "Class kind must be equal");
    }

    @Test
    void testUnknownResourceFromString() {
        final var dictionary = createDictionary(createModuleResource());
        final var result = this.converter.fromString("t|||||||", dictionary);

        assertNotNull(result, "Result of the converter must be not null");
        assertTrue(result.isEmpty(), "Result of the converter must be empty");
    }

    private void makeCommonChecks(ClassPathResource expected, ClassPathResource actual, Class<?> expectedType) {

        assertInstanceOf(expectedType, actual, "Resource must be instance of " + expectedType);
        assertEquals(expected, actual, "Resources must be equal");
        assertEquals(expected.type(), actual.type(), "Resource type must be equal");
        assertEquals(expected.packageName(), actual.packageName(), "Package name must be equal");
        assertEquals(expected.module(), actual.module(), "Module name must be equal");
        assertEquals(expected.modifiers(), actual.modifiers(), "Modifiers must be equal");
        assertEquals(expected.aliases(), actual.aliases(), "Aliases must be equal");
        assertEquals(expected.annotations(), actual.annotations(), "Annotations must be equal");
    }

    private Dictionary createDictionary(final ClassPathResource resource) {
        final Map<String, String> map = new HashMap<>();
        map.put("0", resource.name());
        map.put("1", resource.packageName());

        return new TestDictionary(map);
    }

    private ClassResource<?> createClassResource() {
        return new ClassResource<>() {
            @Override
            public Class<Object> asClass(ClassLoader loader) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Set<ClassReference<?>> interfaces() {
                return Collections.emptySet();
            }

            @Override
            public List<ClassReference<?>> superClasses() {
                return Collections.emptyList();
            }

            @Override
            public Kind kind() {
                return Kind.ANNOTATION;
            }

            @Override
            public String name() {
                return "TestAnnotation";
            }

            @Override
            public Set<String> aliases() {
                return Collections.emptySet();
            }

            @Override
            public String module() {
                return "";
            }

            @Override
            public Set<ClassReference<?>> annotations() {
                return Collections.emptySet();
            }

            @Override
            public String packageName() {
                return getClass().getPackageName();
            }

            @Override
            public Set<Modifier> modifiers() {
                return EnumSet.noneOf(Modifier.class);
            }
        };
    }

    private ClassFieldResource createFieldResource() {
        return new ClassFieldResource() {

            @Override
            public ClassReference<?> owner() {
                return new ClassReference<>() {
                    @Override
                    public String canonicalName() {
                        return TestClass.class.getCanonicalName();
                    }

                    @Override
                    public String binaryName() {
                        return TestClass.class.getName();
                    }

                    @Override
                    public Class<Object> toClass(ClassLoader loader) {
                        throw new UnsupportedOperationException();
                    }
                };
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
                return Collections.emptySet();
            }

            @Override
            public String module() {
                return "";
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
                return EnumSet.noneOf(Modifier.class);
            }
        };
    }

    private ModuleResource createModuleResource() {
        return new ModuleResource() {

            @Override
            public String name() {
                return "test.module";
            }

            @Override
            public Set<String> aliases() {
                return Collections.emptySet();
            }

            @Override
            public Optional<Module> asModule(ModuleLayer layer) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Set<ClassReference<?>> annotations() {
                return Collections.emptySet();
            }

            @Override
            public Set<Modifier> modifiers() {
                return EnumSet.noneOf(Modifier.class);
            }
        };
    }

    private PackageResource createPackageResource() {
        return new PackageResource() {

            @Override
            public Optional<Package> asPackage(ClassLoader loader) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String name() {
                return getClass().getPackageName();
            }

            @Override
            public Set<String> aliases() {
                return Collections.emptySet();
            }

            @Override
            public String module() {
                return "";
            }

            @Override
            public Set<ClassReference<?>> annotations() {
                return Collections.emptySet();
            }

            @Override
            public Set<Modifier> modifiers() {
                return EnumSet.noneOf(Modifier.class);
            }
        };
    }
}
