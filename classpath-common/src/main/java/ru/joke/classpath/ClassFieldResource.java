package ru.joke.classpath;

import java.lang.reflect.Field;

public interface ClassFieldResource extends ClassMemberResource {
    @Override
    default String id() {
        final var module = module();
        return (module.isEmpty() ? "" : module.concat("/")) + owner().binaryName() + ID_SEPARATOR + name();
    }

    default Field asField() throws ClassNotFoundException, NoSuchFieldException {
        final var callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
        return asField(callerClass.getClassLoader());
    }

    Field asField(ClassLoader loader) throws ClassNotFoundException, NoSuchFieldException;

    @Override
    default ClassPathResource.Type type() {
        return ClassPathResource.Type.FIELD;
    }
}
