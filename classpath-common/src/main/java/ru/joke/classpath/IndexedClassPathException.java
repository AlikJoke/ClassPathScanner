package ru.joke.classpath;

/**
 * An exception describing an unexpected error that occurs during the library's operation.
 *
 * @author Alik
 */
public class IndexedClassPathException extends RuntimeException {

    /**
     * Constructs a new runtime exception with the specified cause and a detail message.
     *
     * @param cause the cause; can be {@code null}.
     */
    public IndexedClassPathException(final Exception cause) {
        super(cause);
    }

    /**
     * Constructs a new runtime exception with the specified detail message.
     *
     * @param msg the detailed message; can be {@code null}.
     */
    public IndexedClassPathException(final String msg) {
        super(msg);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message; can be {@code null}.
     * @param cause the cause; can be {@code null}.
     */
    public IndexedClassPathException(final String message, final Exception cause) {
        super(message, cause);
    }
}
