package ru.joke.classpath.converters.internal;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.IndexedClassPathException;
import ru.joke.classpath.util.LazyObject;

final class ClassReferenceImpl<T> implements ClassPathResource.ClassReference<T> {

    private final String canonicalName;
    private final LazyObject<Class<T>, ClassLoader> clazz;

    ClassReferenceImpl(String canonicalName) {
        this.canonicalName = canonicalName;
        this.clazz = new LazyObject<>() {
            @Override
            protected Class<T> load(ClassLoader loader) throws Exception {
                @SuppressWarnings("unchecked")
                final var loadedClass = (Class<T>) Class.forName(canonicalName, false, loader);
                return loadedClass;
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
}
