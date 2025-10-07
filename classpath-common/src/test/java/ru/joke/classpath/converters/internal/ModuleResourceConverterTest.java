package ru.joke.classpath.converters.internal;

import ru.joke.classpath.ClassPathIndexed;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ModuleResource;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ModuleResourceConverterTest extends AbsClassPathResourceConverterTest<ModuleResource, ModuleResourceConverter> {

    private static final String EXPECTED_STR = "m||0||0|1|2;3;4;5";

    @Override
    void makeTypeSpecificChecks(ModuleResource expected, ModuleResource actual) {

        assertTrue(actual.packageName().isEmpty(), "Package name of the module must be empty always");

        final var javaModule = actual.asModule();
        assertNotNull(javaModule, "Java module object must be not null");
        assertTrue(javaModule.isPresent(), "Java module object must present'");
        assertEquals(expected.name(), javaModule.get().getName(), "Name of the module must be equal");
    }

    @Override
    ModuleResourceConverter createConverter() {
        return new ModuleResourceConverter();
    }

    @Override
    ClassPathResource.Type testResourceType() {
        return ClassPathResource.Type.MODULE;
    }

    @Override
    ModuleResource createTestResource() {
        return new ModuleResource() {
            @Override
            public Optional<Module> asModule(ModuleLayer layer) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String name() {
                return getClass().getModule().getName();
            }

            @Override
            public Set<String> aliases() {
                return Set.of("common");
            }

            @Override
            public Set<ClassReference<?>> annotations() {
                final Set<ClassReference<?>> result = new LinkedHashSet<>();
                result.add(new ClassReferenceImpl<>(Documented.class.getCanonicalName()));
                result.add(new ClassReferenceImpl<>(Target.class.getCanonicalName()));
                result.add(new ClassReferenceImpl<>(ClassPathIndexed.class.getCanonicalName()));
                result.add(new ClassReferenceImpl<>(Retention.class.getCanonicalName()));

                return result;
            }

            @Override
            public Set<Modifier> modifiers() {
                return EnumSet.noneOf(Modifier.class);
            }
        };
    }

    @Override
    String getExpectedStringRepresentation() {
        return EXPECTED_STR;
    }
}