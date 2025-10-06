package ru.joke.classpath;

import java.util.List;

public interface ClassMemberResource extends ClassPathResource {

    String ID_SEPARATOR = "#";

    ClassReference<?> owner();

    interface Executable extends ClassMemberResource {

        List<ClassReference<?>> parameters();
    }
}
