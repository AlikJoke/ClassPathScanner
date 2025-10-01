package ru.joke.classpath.converters;

import ru.joke.classpath.ClassFieldResource;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ClassResource;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Set;

public final class ClassFieldResourceConverter extends AbsClassPathResourceConverter<ClassFieldResource> implements ConcreteClassPathResourceConverter<ClassFieldResource> {

    private static final int COMPONENTS_COUNT = 7;

    public ClassFieldResourceConverter() {
        super(COMPONENTS_COUNT);
    }

    @Override
    public ClassPathResource.Type supportedType() {
        return ClassPathResource.Type.FIELD;
    }

    @Override
    protected ClassFieldResource from(
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
        final var fieldName = dictionary.map(nameParts[1]);

        final var owner = new ClassReferenceImpl<>(packageName + ClassResource.ID_SEPARATOR + className);

        return new ClassFieldResource() {

            private volatile Field field;

            @Override
            public Field asField() throws NoSuchFieldException, ClassNotFoundException {
                if (this.field == null) {
                    synchronized (this) {
                        if (this.field == null) {
                            this.field = owner().toClass().getDeclaredField(fieldName);
                        }
                    }
                }

                return this.field;
            }

            @Override
            public ClassReference<?> owner() {
                return owner;
            }

            @Override
            public String name() {
                return fieldName;
            }

            @Override
            public String id() {
                return owner.canonicalName() + ID_SEPARATOR + fieldName;
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
                return obj instanceof ClassFieldResource f && f.id().equals(id());
            }

            @Override
            public String toString() {
                return type().name() + ":" + id();
            }
        };
    }

    @Override
    protected String getResourceName(ClassFieldResource resource, Dictionary dictionary) {
        final var ownerClassSimpleName = resource.owner().canonicalName().substring(resource.packageName().length() + 1);
        return dictionary.map(ownerClassSimpleName) + MEMBER_OF_CLASS_SEPARATOR + dictionary.map(resource.name());
    }

    @Override
    protected String getResourceName(String resourceNameStr, Dictionary dictionary) {
        return resourceNameStr;
    }
}
