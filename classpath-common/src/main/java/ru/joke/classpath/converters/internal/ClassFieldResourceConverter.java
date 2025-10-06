package ru.joke.classpath.converters.internal;

import ru.joke.classpath.ClassFieldResource;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.ClassResource;
import ru.joke.classpath.IndexedClassPathException;
import ru.joke.classpath.converters.Dictionary;
import ru.joke.classpath.util.LazyObject;

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

            private final LazyObject<Field, ClassLoader> field = new LazyObject<>() {
                @Override
                protected Field load(ClassLoader loader) throws Exception {
                    return owner().toClass(loader).getDeclaredField(fieldName);
                }
            };

            @Override
            public Field asField(ClassLoader loader) throws NoSuchFieldException, ClassNotFoundException {
                try {
                    return this.field.get(loader);
                } catch (NoSuchFieldException | ClassNotFoundException e) {
                    throw e;
                } catch (Exception e) {
                    throw new IndexedClassPathException(e);
                }
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
                return Objects.hashCode(id());
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof ClassFieldResource f && Objects.equals(f.id(), id());
            }

            @Override
            public String toString() {
                return ClassFieldResourceConverter.this.toStringDescription(this);
            }
        };
    }

    @Override
    protected String getResourceName(
            final ClassFieldResource resource,
            final Dictionary dictionary
    ) {
        final var ownerClassSimpleName = resource.owner().canonicalName().substring(resource.packageName().length() + 1);
        return dictionary.map(ownerClassSimpleName) + MEMBER_OF_CLASS_SEPARATOR + dictionary.map(resource.name());
    }

    @Override
    protected String getResourceName(
            final String resourceNameStr,
            final Dictionary dictionary
    ) {
        return resourceNameStr;
    }
}
