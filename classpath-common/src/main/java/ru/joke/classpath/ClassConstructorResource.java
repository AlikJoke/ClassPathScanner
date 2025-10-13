package ru.joke.classpath;

import java.lang.reflect.Constructor;

/**
 * A representation of a Java class constructor located on the classpath.
 *
 * @author Alik
 * @see ClassPathResource
 * @see ClassMemberResource.Executable
 * @param <T> type of the owner class
 */
public interface ClassConstructorResource <T> extends ClassMemberResource.Executable {

    /**
     * Returns a reflective reference to the constructor {@link Constructor} using the Reflection API.<br>
     * If the declaring class is not loaded, it loads the class using the class loader
     * of the calling method class.
     *
     * @return constructor reference; cannot be {@code null}.
     * @throws ClassNotFoundException if the owner class cannot be loaded by the {@link ClassLoader} of caller class
     * @throws NoSuchMethodException if the specified constructor cannot be found in the owner class
     */
    default Constructor<T> asConstructor() throws ClassNotFoundException, NoSuchMethodException {
        final var callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
        return asConstructor(callerClass.getClassLoader());
    }

    /**
     * Returns a reflective reference to the constructor {@link Constructor} using the Reflection API.<br>
     * If the declaring class is not loaded, it loads the class using the given class loader.
     *
     * @param loader target classloader; cannot be {@code null}.
     *
     * @return constructor reference; cannot be {@code null}.
     * @throws ClassNotFoundException if the owner class cannot be loaded by the given {@link ClassLoader}
     * @throws NoSuchMethodException if the specified constructor cannot be found in the owner class
     */
    Constructor<T> asConstructor(ClassLoader loader) throws ClassNotFoundException, NoSuchMethodException;

    @Override
    default Type type() {
        return Type.CONSTRUCTOR;
    }
}
