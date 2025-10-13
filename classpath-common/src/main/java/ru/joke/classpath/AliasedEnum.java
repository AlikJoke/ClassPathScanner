package ru.joke.classpath;

/**
 * A representation of an enum whose elements have aliases that can be used to locate
 * the element corresponding to a given alias.
 *
 * @author Alik
 */
public interface AliasedEnum {

    /**
     * Returns the alias of the specified enum element.
     *
     * @return the alias; cannot be {@code null}.
     */
    String alias();

    /**
     * A static factory method that allows retrieving concrete enum element by its alias.
     *
     * @param alias alias of the target enum element; can be {@code null}.
     * @param elements elements of the enum; cannot be {@code null}.
     * @param <T> concrete type of the enum
     * @return enum element with specified alias; can be {@code null} if alias is unknown.
     */
    static <T extends Enum<T>&AliasedEnum> T from(final String alias, final T[] elements) {
        for (var type : elements) {
            if (type.alias().equals(alias)) {
                return type;
            }
        }

        return null;
    }
}
