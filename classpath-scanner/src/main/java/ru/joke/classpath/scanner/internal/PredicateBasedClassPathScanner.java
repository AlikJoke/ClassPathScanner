package ru.joke.classpath.scanner.internal;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.scanner.ClassPathScanner;

import java.util.function.Predicate;

/**
 * Predicate-based {@link ClassPathScanner}.
 *
 * @author Alik
 * @see ClassPathScanner
 */
public interface PredicateBasedClassPathScanner extends ClassPathScanner, Predicate<ClassPathResource> {
}
