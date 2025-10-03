package ru.joke.classpath;

import java.util.Set;

public interface ClassResource<T> extends ClassPathResource {

    String ID_SEPARATOR = ".";

    default Class<T> asClass() throws ClassNotFoundException {
        final var callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
        return asClass(callerClass.getClassLoader());
    }

    Class<T> asClass(ClassLoader loader) throws ClassNotFoundException;

    Set<ClassReference<?>> interfaces();

    Set<ClassReference<?>> superClasses();

    Kind kind();

    @Override
    default String id() {
        return packageName() + ID_SEPARATOR + name();
    }

    @Override
    default Type type() {
        return Type.CLASS;
    }

    enum Kind implements AliasedEnum {

        CLASS("c"),
        INTERFACE("i"),
        ENUM("e"),
        RECORD("r"),
        ANNOTATION("a");

        private final String alias;

        Kind(String alias) {
            this.alias = alias;
        }

        @Override
        public String alias() {
            return this.alias;
        }

        public static Kind from(final String alias) {
            return AliasedEnum.from(alias, values());
        }
    }
}
