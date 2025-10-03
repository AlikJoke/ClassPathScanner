package ru.joke.classpath;

import java.lang.reflect.Constructor;

public interface ClassConstructorResource <T> extends ClassMemberResource.Executable {

    default Constructor<T> asConstructor() throws ClassNotFoundException, NoSuchMethodException {
        final var callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
        return asConstructor(callerClass.getClassLoader());
    }

    Constructor<T> asConstructor(ClassLoader loader) throws ClassNotFoundException, NoSuchMethodException;

    @Override
    default Type type() {
        return Type.CONSTRUCTOR;
    }
}
