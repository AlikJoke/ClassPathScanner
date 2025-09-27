package ru.joke.classpath;

import java.util.Set;

public interface ClassPathResource {

    String id();

    String name();

    Set<String> aliases();

    String module();

    Set<ClassReference<?>> annotations();

    String packageName();

    Type type();

    Set<Modifier> modifiers();

    interface ClassReference<T> {

        String canonicalName();

        Class<T> toClass() throws ClassNotFoundException;
    }

    enum Type implements AliasedEnum {

        MODULE("m"),
        CLASS("c"),
        CONSTRUCTOR("cr"),
        METHOD("md"),
        FIELD("f"),
        PACKAGE("p");

        private final String alias;

        Type(String alias) {
            this.alias = alias;
        }

        @Override
        public String alias() {
            return this.alias;
        }

        public static Type from(final String alias) {
            return AliasedEnum.from(alias, values());
        }
    }

    enum Modifier implements AliasedEnum {

        FINAL("f"),
        ABSTRACT("a"),
        TRANSIENT("t"),
        VOLATILE("v"),
        SYNCHRONIZED("sy"),
        NATIVE("n"),
        PUBLIC("pc"),
        PROTECTED("pd"),
        PRIVATE("pv"),
        DEFAULT("d"),
        SEALED("s"),
        OPENED("o"),
        STATIC("st"),
        NON_SEALED("ns");

        private final String alias;

        Modifier(String alias) {
            this.alias = alias;
        }

        @Override
        public String alias() {
            return this.alias;
        }

        public static Modifier from(final String alias) {
            return AliasedEnum.from(alias, values());
        }
    }
}
