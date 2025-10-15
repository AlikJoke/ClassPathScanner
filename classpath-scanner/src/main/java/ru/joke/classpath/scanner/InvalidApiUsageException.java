package ru.joke.classpath.scanner;

import ru.joke.classpath.IndexedClassPathException;

/**
 * An exception describing an incorrect use of the library's API that does not conform to the documentation.
 *
 * @author Alik
 */
public final class InvalidApiUsageException extends IndexedClassPathException {

    /**
     * Constructs a new runtime exception with the specified detail message.
     *
     * @param msg the detailed message; can be {@code null}.
     */
    public InvalidApiUsageException(final String msg) {
        super(msg);
    }
}
