package ru.joke.classpath.indexer.test_util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

abstract class Util {

    static Set<Class<?>> findAllClasses(final String packageName) {
        try (final var stream = ClassLoader.getSystemClassLoader().getResourceAsStream(packageName.replaceAll("[.]", "/"))) {
            if (stream == null) {
                return Collections.emptySet();
            }

            final var reader = new BufferedReader(new InputStreamReader(stream));
            return reader.lines()
                            .filter(line -> line.endsWith(".class"))
                            .map(line -> getClass(line, packageName))
                            .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?> getClass(String className, String packageName) {
        try {
            final var binaryClassName = packageName + "." + className.substring(0, className.lastIndexOf('.'));
            return Class.forName(binaryClassName);
        } catch (ClassNotFoundException ignored) {
        }
        return null;
    }
}
