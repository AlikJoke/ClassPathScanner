package ru.joke.classpath.converters;

import ru.joke.classpath.ClassMethodResource;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ClassResource;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class ClassMethodResourceConverter extends ExecutableClassMemberResourceConverter<ClassMethodResource> {

    @Override
    public ClassPathResource.Type supportedType() {
        return ClassPathResource.Type.METHOD;
    }

    @Override
    protected ClassMethodResource from(
            final Set<ClassPathResource.Modifier> modifiers,
            final String module,
            final String packageName,
            final String name,
            final Set<String> aliases,
            final Set<ClassPathResource.ClassReference<?>> annotations,
            final String[] parts,
            final Dictionary dictionary
    ) {
        final var nameParts = name.split(MEMBER_OF_CLASS_SEPARATOR);
        final var className = dictionary.map(nameParts[0]);
        final var methodName = dictionary.map(nameParts[1]);
        final var parameters = List.copyOf(extractRefs(nameParts[2], dictionary));

        final var owner = new ClassReferenceImpl<>(packageName + ClassResource.ID_SEPARATOR + className);
        final var methodSignature = createSignature(methodName, nameParts[2]);

        return new ClassMethodResource() {

            private volatile Method method;

            @Override
            public List<ClassReference<?>> parameters() {
                return parameters;
            }

            @Override
            public Method asMethod() throws ClassNotFoundException, NoSuchMethodException {
                if (this.method == null) {
                    synchronized (this) {
                        if (this.method == null) {
                            final var parameterTypes = loadParameters(parameters);
                            this.method = owner().toClass().getDeclaredMethod(methodName, parameterTypes);
                        }
                    }
                }

                return this.method;
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
                return owner.canonicalName() + ID_SEPARATOR + methodSignature;
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

            @Override
            public Set<Modifier> modifiers() {
                return modifiers;
            }

            @Override
            public int hashCode() {
                return Objects.hash(id());
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof ClassMethodResource f && f.id().equals(id());
            }

            @Override
            public String toString() {
                return type().name() + ":" + id();
            }
        };
    }
}
