package ru.joke.classpath.converters.internal;

abstract class LazyObject<T, C> {

    protected volatile T object;

    public final T get(C context) throws Exception {
        T result;
        if ((result = this.object) == null) {
            synchronized (this) {
                if ((result = this.object) == null) {
                    this.object = result = load(context);
                }
            }
        }

        return result;
    }

    protected abstract T load(C context) throws Exception;
}
