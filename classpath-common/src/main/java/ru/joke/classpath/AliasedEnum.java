package ru.joke.classpath;

public interface AliasedEnum {

    String alias();

    static <T extends Enum<T>&AliasedEnum> T from(final String alias, final T[] elements) {
        for (var type : elements) {
            if (type.alias().equals(alias)) {
                return type;
            }
        }

        return null;
    }
}
