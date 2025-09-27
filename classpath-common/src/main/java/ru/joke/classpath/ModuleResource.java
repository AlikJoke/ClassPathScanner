package ru.joke.classpath;

import java.util.Optional;

public interface ModuleResource extends ClassPathResource {
    @Override
    default String id() {
        return name();
    }

    Optional<Module> asModule();

    @Override
    default Type type() {
        return Type.MODULE;
    }

    @Override
    default String packageName() {
        return "";
    }
}
