package ru.joke.classpath;

import java.util.Optional;

public interface ModuleResource extends ClassPathResource {
    @Override
    default String id() {
        return name();
    }

    @Override
    default String module() {
        return name();
    }

    default Optional<Module> asModule() {
        final var callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
        return asModule(callerClass.getModule().getLayer());
    }

    Optional<Module> asModule(ModuleLayer layer);

    @Override
    default Type type() {
        return Type.MODULE;
    }

    @Override
    default String packageName() {
        return "";
    }
}
