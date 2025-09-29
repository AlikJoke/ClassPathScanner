package ru.joke.classpath.scanner.impl;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.scanner.ClassPathScanner;

import java.util.function.Predicate;

public interface PredicateBasedClassPathScanner extends ClassPathScanner, Predicate<ClassPathResource> {

}
