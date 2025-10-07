package ru.joke.classpath.converters.internal;

import ru.joke.classpath.ClassPathIndexed;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.PackageResource;
import ru.joke.classpath.fixtures.TestAnnotation;
import ru.joke.classpath.fixtures.TestAnnotation2;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PackageResourceConverterTest extends AbsClassPathResourceConverterTest<PackageResource, PackageResourceConverter> {

    private static final String EXPECTED_STR = "p|s|0||1|2;3|4;5;6;7;8;9;10";

    @Override
    void makeTypeSpecificChecks(PackageResource expected, PackageResource actual) {

        assertTrue(actual.packageName().isEmpty(), "Package name of the package must be empty always");

        final var javaPkg = actual.asPackage();
        assertNotNull(javaPkg, "Java package object must be not null");
        assertTrue(javaPkg.isPresent(), "Java package object must present'");
        assertEquals(expected.name(), javaPkg.get().getName(), "Name of the package must be equal");
    }

    @Override
    PackageResourceConverter createConverter() {
        return new PackageResourceConverter();
    }

    @Override
    ClassPathResource.Type testResourceType() {
        return ClassPathResource.Type.PACKAGE;
    }

    @Override
    PackageResource createTestResource() {
        return new PackageResource() {

            @Override
            public Optional<Package> asPackage(ClassLoader loader) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String name() {
                return "ru.joke.classpath.fixtures";
            }

            @Override
            public Set<String> aliases() {
                return Set.of("p1", "test_package");
            }

            @Override
            public String module() {
                return getClass().getModule().getName();
            }

            @Override
            public Set<ClassReference<?>> annotations() {
                final Set<ClassReference<?>> result = new LinkedHashSet<>();
                result.add(new ClassReferenceImpl<>(TestAnnotation2.class.getCanonicalName()));
                result.add(new ClassReferenceImpl<>(TestAnnotation.class.getCanonicalName()));
                result.add(new ClassReferenceImpl<>(Documented.class.getCanonicalName()));
                result.add(new ClassReferenceImpl<>(Target.class.getCanonicalName()));
                result.add(new ClassReferenceImpl<>(Inherited.class.getCanonicalName()));
                result.add(new ClassReferenceImpl<>(ClassPathIndexed.class.getCanonicalName()));
                result.add(new ClassReferenceImpl<>(Retention.class.getCanonicalName()));

                return result;
            }

            @Override
            public Set<Modifier> modifiers() {
                return EnumSet.of(Modifier.SEALED);
            }
        };
    }

    @Override
    String getExpectedStringRepresentation() {
        return EXPECTED_STR;
    }
}