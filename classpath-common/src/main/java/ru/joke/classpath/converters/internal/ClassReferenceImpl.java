package ru.joke.classpath.converters.internal;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.IndexedClassPathException;
import ru.joke.classpath.util.LazyObject;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

final class ClassReferenceImpl<T> implements ClassPathResource.ClassReference<T> {

    private static final Map<String, Class<?>> primitiveTypesMap =
            Set.of(
                    boolean.class,
                    byte.class,
                    char.class,
                    short.class,
                    int.class,
                    long.class,
                    float.class,
                    double.class,
                    void.class
            ).stream().collect(Collectors.toMap(Class::getCanonicalName, Function.identity()));

    private final String canonicalName;
    private final LazyObject<Class<T>, ClassLoader> clazz;

    ClassReferenceImpl(String canonicalName) {
        this.canonicalName = canonicalName;
        this.clazz = new LazyObject<>() {
            @Override
            protected Class<T> load(ClassLoader loader) throws Exception {
                @SuppressWarnings("unchecked")
                var result = (Class<T>) primitiveTypesMap.get(canonicalName);
                if (result == null) {
                    @SuppressWarnings("unchecked")
                    final var loadedClass = (Class<T>) Class.forName(canonicalName, false, loader);
                    result = loadedClass;
                }

                return result;
            }
        };
    }

    @Override
    public String canonicalName() {
        return this.canonicalName;
    }

    @Override
    public Class<T> toClass(ClassLoader loader) throws ClassNotFoundException {
        try {
            return this.clazz.get(loader);
        } catch (ClassNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new IndexedClassPathException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final var that = (ClassReferenceImpl<?>) o;
        return canonicalName.equals(that.canonicalName);
    }

    @Override
    public int hashCode() {
        return canonicalName.hashCode();
    }

    @Override
    public String toString() {
        return "ClassReference{" + "canonicalName='" + canonicalName + '\'' + '}';
    }
}
