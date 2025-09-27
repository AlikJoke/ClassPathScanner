package ru.joke.classpath;

import java.lang.reflect.Field;

public interface ClassFieldResource extends ClassMemberResource {

    Field asField() throws NoSuchFieldException, ClassNotFoundException;

    @Override
    default ClassPathResource.Type type() {
        return ClassPathResource.Type.FIELD;
    }
}
