package ru.joke.classpath;

import java.util.List;

public interface ClassMemberResource extends ClassPathResource {

    String ID_SEPARATOR = "#";

    ClassReference<?> owner();

    @Override
    default String id() {
        return packageName() + ID_SEPARATOR + name();
    }

    interface Executable extends ClassMemberResource {

        List<ClassReference<?>> parameters();
    }
}
