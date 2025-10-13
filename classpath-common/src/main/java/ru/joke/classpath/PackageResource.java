package ru.joke.classpath;

import java.util.Optional;

/**
 * Representation of a Java package resource located on the classpath.
 *
 * @author Alik
 * @see ClassPathResource
 */
public interface PackageResource extends ClassPathResource {

    /**
     * @return id in the format {@code <module name>/<package name>}.
     */
    @Override
    default String id() {
        final var module = module();
        return module == null || module.isEmpty()
                ? name()
                : module + MODULE_SEPARATOR + name();
    }

    /**
     * Returns the package representation as a JDK standard {@link java.lang.Package} object.<br>
     * The package lookup is performed within the scope of the classloader of the calling method class.
     * If the package is not found among the classloader's defined packages, {@link Optional#empty()} will be returned.
     *
     * @return wrapped package object; cannot be {@code null}.
     */
    default Optional<Package> asPackage() {
        final var callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
        return asPackage(callerClass.getClassLoader());
    }

    /**
     * Returns the package representation as a JDK standard {@link java.lang.Package} object.<br>
     * The package lookup is performed within the scope of the given classloader.
     * If the package is not found among the classloader's defined packages, {@link Optional#empty()} will be returned.
     *
     * @param loader target classloader; cannot be {@code null}.
     * @return wrapped package object; cannot be {@code null}.
     */
    Optional<Package> asPackage(ClassLoader loader);

    @Override
    default Type type() {
        return Type.PACKAGE;
    }

    /**
     * @return always empty string.
     */
    @Override
    default String packageName() {
        return "";
    }
}
