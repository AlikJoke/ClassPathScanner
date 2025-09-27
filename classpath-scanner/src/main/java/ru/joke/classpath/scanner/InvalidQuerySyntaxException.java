package ru.joke.classpath.scanner;

public final class InvalidQuerySyntaxException extends RuntimeException {

    public InvalidQuerySyntaxException(final String msg) {
        super(msg);
    }
}
