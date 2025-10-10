package ru.joke.classpath.indexer.test_util;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

abstract class Util {

    static TypeMirror toTypeMirror(Class<?> type) {
        return type.isPrimitive()
                ? new TestPrimitiveType(type)
                : type.isArray()
                    ? new TestArrayType(type)
                    : new TestClassType(type);
    }

    static Set<Modifier> collectModifiers(final int modifiers) {
        final Set<Modifier> result = new HashSet<>();
        if (java.lang.reflect.Modifier.isPublic(modifiers)) {
            result.add(Modifier.PUBLIC);
        }
        if (java.lang.reflect.Modifier.isFinal(modifiers)) {
            result.add(Modifier.FINAL);
        }
        if (java.lang.reflect.Modifier.isAbstract(modifiers)) {
            result.add(Modifier.ABSTRACT);
        }
        if (java.lang.reflect.Modifier.isPrivate(modifiers)) {
            result.add(Modifier.PRIVATE);
        }
        if (java.lang.reflect.Modifier.isProtected(modifiers)) {
            result.add(Modifier.PROTECTED);
        }
        if (java.lang.reflect.Modifier.isStatic(modifiers)) {
            result.add(Modifier.STATIC);
        }
        if (java.lang.reflect.Modifier.isSynchronized(modifiers)) {
            result.add(Modifier.SYNCHRONIZED);
        }
        if (java.lang.reflect.Modifier.isVolatile(modifiers)) {
            result.add(Modifier.VOLATILE);
        }
        if (java.lang.reflect.Modifier.isNative(modifiers)) {
            result.add(Modifier.NATIVE);
        }

        return result.isEmpty()
                ? EnumSet.noneOf(Modifier.class)
                : EnumSet.copyOf(result);
    }
    
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
