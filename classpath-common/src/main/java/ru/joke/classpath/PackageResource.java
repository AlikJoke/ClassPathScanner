package ru.joke.classpath;

import java.util.Optional;

public interface PackageResource extends ClassPathResource {
    @Override
    default String id() {
        return name();
    }

    Optional<Package> asPackage();

    @Override
    default Type type() {
        return Type.PACKAGE;
    }

    @Override
    default String packageName() {
        return "";
    }
}
