package ru.joke.classpath.converters;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ConcreteClassPathResourceConverter;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class MethodResourceConverter extends AbsClassPathResourceConverter<ClassPathResource.MethodResource> implements ConcreteClassPathResourceConverter<ClassPathResource.MethodResource> {

    @Override
    public Optional<ClassPathResource.MethodResource> fromString(String resource) {
        final var parts = resource.split("\\" + BLOCK_SEPARATOR);
        if (parts.length < 6) {
            return Optional.empty();
        }

        final var module = parts[1];
        final var packageName = parts[2];

        final var nameParts = parts[3].split(MEMBER_OF_CLASS_SEPARATOR);
        final var className = nameParts[0];
        final var methodName = nameParts[1];
        final var parameters = List.copyOf(extractRefs(nameParts[2]));

        final var aliases = Set.of(parts[4].split(ELEMENTS_IN_BLOCK_DELIMITER));
        final var annotations = extractRefs(parts[5]);

        final var owner = new ClassReferenceImpl<>(packageName + "." + className);

        return Optional.of(new ClassPathResource.MethodResource() {

            @Override
            public List<ClassReference<?>> parameters() {
                return parameters;
            }

            @Override
            public Method asMethod() throws ClassNotFoundException, NoSuchMethodException {
                final var parameterTypes = new Class[parameters.size()];
                for (int i = 0; i < parameters.size(); i++) {
                    parameterTypes[i] = parameters.get(i).toClass();
                }

                return owner().toClass().getDeclaredMethod(methodName, parameterTypes);
            }

            @Override
            public ClassReference<?> owner() {
                return owner;
            }

            @Override
            public String name() {
                return methodName;
            }

            @Override
            public String id() {
                return parts[3];
            }

            @Override
            public Set<String> aliases() {
                return aliases;
            }

            @Override
            public String module() {
                return module;
            }

            @Override
            public Set<ClassReference<?>> annotations() {
                return annotations;
            }

            @Override
            public String packageName() {
                return packageName;
            }
        });
    }

    @Override
    public ClassPathResource.Type supportedType() {
        return ClassPathResource.Type.METHOD;
    }

    @Override
    protected String getResourceName(ClassPathResource.MethodResource resource) {
        final var parameters =
                resource.parameters()
                        .stream()
                        .map(ClassPathResource.ClassReference::canonicalName)
                        .collect(Collectors.joining(ELEMENTS_IN_BLOCK_DELIMITER));
        return resource.owner().canonicalName().substring(resource.packageName().length() + 1)
                + MEMBER_OF_CLASS_SEPARATOR
                + resource.name()
                + MEMBER_OF_CLASS_SEPARATOR
                + parameters;
    }
}
