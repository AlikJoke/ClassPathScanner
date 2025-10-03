package ru.joke.classpath;

import java.lang.reflect.Method;

public interface ClassMethodResource extends ClassMemberResource.Executable {

    default Method asMethod() throws ClassNotFoundException, NoSuchMethodException {
        final var callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
        return asMethod(callerClass.getClassLoader());
    }

    Method asMethod(ClassLoader loader) throws ClassNotFoundException, NoSuchMethodException;

    @Override
    default Type type() {
        return Type.METHOD;
    }
}
