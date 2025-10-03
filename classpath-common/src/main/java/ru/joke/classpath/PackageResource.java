package ru.joke.classpath;

import java.util.Optional;

public interface PackageResource extends ClassPathResource {
    @Override
    default String id() {
        return name();
    }

    default Optional<Package> asPackage() {
        final var callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
        return asPackage(callerClass.getClassLoader());
    }

    Optional<Package> asPackage(ClassLoader loader);

    @Override
    default Type type() {
        return Type.PACKAGE;
    }

    @Override
    default String packageName() {
        return "";
    }
}
