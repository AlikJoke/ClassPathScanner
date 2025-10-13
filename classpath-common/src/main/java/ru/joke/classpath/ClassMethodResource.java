package ru.joke.classpath;

import java.lang.reflect.Method;

/**
 * A representation of a named Java class method located on the classpath.
 *
 * @author Alik
 * @see ClassPathResource
 * @see ClassMemberResource.Executable
 */
public interface ClassMethodResource extends ClassMemberResource.Executable {

    /**
     * Returns a reflective reference to the method {@link Method} using the Reflection API.<br>
     * If the declaring class is not loaded, it loads the class using the class loader
     * of the calling method class.
     *
     * @return method reference; cannot be {@code null}.
     * @throws ClassNotFoundException if the owner class cannot be loaded by the {@link ClassLoader} of caller class
     * @throws NoSuchMethodException if the specified method cannot be found in the owner class
     */
    default Method asMethod() throws ClassNotFoundException, NoSuchMethodException {
        final var callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
        return asMethod(callerClass.getClassLoader());
    }

    /**
     * Returns a reflective reference to the method {@link Method} using the Reflection API.<br>
     * If the declaring class is not loaded, it loads the class using the given class loader.
     *
     * @param loader target classloader; cannot be {@code null}.
     *
     * @return method reference; cannot be {@code null}.
     * @throws ClassNotFoundException if the owner class cannot be loaded by the given {@link ClassLoader}
     * @throws NoSuchMethodException if the specified method cannot be found in the owner class
     */
    Method asMethod(ClassLoader loader) throws ClassNotFoundException, NoSuchMethodException;

    @Override
    default Type type() {
        return Type.METHOD;
    }
}
