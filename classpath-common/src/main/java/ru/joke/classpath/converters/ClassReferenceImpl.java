package ru.joke.classpath.converters;

import ru.joke.classpath.ClassPathResource;

final class ClassReferenceImpl<T> implements ClassPathResource.ClassReference<T> {

    private final String canonicalName;
    private volatile Class<T> clazz;

    ClassReferenceImpl(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    @Override
    public String canonicalName() {
        return this.canonicalName;
    }

    @Override
    public Class<T> toClass() throws ClassNotFoundException {
        if (this.clazz == null) {
            synchronized (this) {
                if (this.clazz == null) {
                    @SuppressWarnings("unchecked")
                    final var loadedClass = (Class<T>) Class.forName(this.canonicalName, false, Thread.currentThread().getContextClassLoader());
                    return this.clazz = loadedClass;
                }
            }
        }

        return this.clazz;
    }
}
