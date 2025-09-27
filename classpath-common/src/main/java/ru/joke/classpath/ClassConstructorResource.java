package ru.joke.classpath;

import java.lang.reflect.Constructor;

public interface ClassConstructorResource <T> extends ClassMemberResource.Executable {

    Constructor<T> asConstructor() throws ClassNotFoundException, NoSuchMethodException;

    @Override
    default Type type() {
        return Type.CONSTRUCTOR;
    }
}
