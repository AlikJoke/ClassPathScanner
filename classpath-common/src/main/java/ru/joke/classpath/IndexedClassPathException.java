package ru.joke.classpath;

public class IndexedClassPathException extends RuntimeException {

    public IndexedClassPathException(final Exception ex) {
        super(ex);
    }

    public IndexedClassPathException(final String message, final Exception ex) {
        super(message, ex);
    }
}
