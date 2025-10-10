package ru.joke.classpath.converters.internal;

import ru.joke.classpath.ClassMethodResource;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.IndexedClassPathException;
import ru.joke.classpath.converters.Dictionary;
import ru.joke.classpath.util.LazyObject;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static ru.joke.classpath.ClassPathResource.ClassReference.CANONICAL_NAME_SEPARATOR;

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
        final var parameters = extractRefs(nameParts[2], dictionary, PARAMETER_TYPES_DELIMITER);

        final var ownerClassBinaryName = packageName +  CANONICAL_NAME_SEPARATOR + className;
        final var owner = new ClassReferenceImpl<>(ownerClassBinaryName);

        return new ClassMethodResource() {

            private final LazyObject<Method, ClassLoader> method = new LazyObject<>() {
                @Override
                protected Method load(ClassLoader loader) throws Exception {
                    final var parameterTypes = loadParameters(parameters, loader);
                    return owner().toClass(loader).getDeclaredMethod(methodName, parameterTypes);
                }
            };

            @Override
            public List<ClassReference<?>> parameters() {
                return parameters;
            }

            @Override
            public Method asMethod(ClassLoader loader) throws ClassNotFoundException, NoSuchMethodException {
                try {
                    return this.method.get(loader);
                } catch (ClassNotFoundException | NoSuchMethodException e) {
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
                return methodName;
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
                return obj instanceof ClassMethodResource f && Objects.equals(f.id(), id());
            }

            @Override
            public String toString() {
                return toStringDescription();
            }
        };
    }
}
