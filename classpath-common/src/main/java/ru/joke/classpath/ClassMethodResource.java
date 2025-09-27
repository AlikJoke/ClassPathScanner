package ru.joke.classpath;

import java.lang.reflect.Method;

public interface ClassMethodResource extends ClassMemberResource.Executable {

    Method asMethod() throws ClassNotFoundException, NoSuchMethodException;

    @Override
    default Type type() {
        return Type.METHOD;
    }
}
