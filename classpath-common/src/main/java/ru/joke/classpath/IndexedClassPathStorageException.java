package ru.joke.classpath;

/**
 * An exception describing an unexpected error that occurred while writing to or reading
 * from the index of classpath resources. The index takes the form of a file in a specific
 * format defined by the library's logic.
 *
 * @author Alik
 * @see IndexedClassPathException
 */
public final class IndexedClassPathStorageException extends IndexedClassPathException {

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message; can be {@code null}.
     * @param cause the cause; can be {@code null}.
     */
    public IndexedClassPathStorageException(final String message, final Exception cause) {
        super(message, cause);
    }
}
