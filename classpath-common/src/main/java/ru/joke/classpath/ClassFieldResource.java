package ru.joke.classpath;

import java.lang.reflect.Field;

/**
 * A representation of a Java class field located on the classpath.
 *
 * @author Alik
 * @see ClassPathResource
 * @see ClassMemberResource
 */
public interface ClassFieldResource extends ClassMemberResource {

    /**
     * Returns a reflective reference to the class field {@link Field} using the Reflection API.<br>
     * If the declaring class is not loaded, it loads the class using the class loader
     * of the calling method class.
     *
     * @return field reference; cannot be {@code null}.
     * @throws ClassNotFoundException if the owner class cannot be loaded by the {@link ClassLoader} of caller class
     * @throws NoSuchFieldException if the specified field cannot be found in the owner class
     */
    default Field asField() throws ClassNotFoundException, NoSuchFieldException {
        final var callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
        return asField(callerClass.getClassLoader());
    }

    /**
     * Returns a reflective reference to the class field {@link Field} using the Reflection API.<br>
     * If the declaring class is not loaded, it loads the class using the given class loader.
     *
     * @param loader target classloader; cannot be {@code null}.
     *
     * @return field reference; cannot be {@code null}.
     * @throws ClassNotFoundException if the owner class cannot be loaded by the given {@link ClassLoader}
     * @throws NoSuchFieldException if the specified field cannot be found in the owner class
     */
    Field asField(ClassLoader loader) throws ClassNotFoundException, NoSuchFieldException;

    @Override
    default ClassPathResource.Type type() {
        return ClassPathResource.Type.FIELD;
    }
}
