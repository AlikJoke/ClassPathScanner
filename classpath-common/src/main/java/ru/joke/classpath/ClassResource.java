package ru.joke.classpath;

import java.util.List;
import java.util.Set;

import static ru.joke.classpath.ClassPathResource.ClassReference.CANONICAL_NAME_SEPARATOR;

/**
 * Representation of a named Java class resource located on the classpath.
 *
 * @param <T> type of the class
 * @author Alik
 * @see ClassPathResource
 */
public interface ClassResource<T> extends ClassPathResource {

    /**
     * Obtains the class descriptor as a {@link java.lang.Class} object.<br>
     * If the class is not loaded, it loads the class using the class loader
     * of the calling method class.
     * If the class is not found among the classloader's classes then the exception will be thrown.
     *
     * @return class object; cannot be {@code null}.
     * @throws ClassNotFoundException if the class cannot be loaded by the {@link ClassLoader} of caller class
     */
    default Class<T> asClass() throws ClassNotFoundException {
        final var callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
        return asClass(callerClass.getClassLoader());
    }

    /**
     * Obtains the class descriptor as a {@link java.lang.Class} object.<br>
     * If the class is not loaded, it loads the class using the given class loader.
     * If the class is not found among the classloader's classes then the exception will be thrown.
     *
     * @param loader target classloader; cannot be {@code null}.
     *
     * @return referenced class object; cannot be {@code null}.
     * @throws ClassNotFoundException if the class cannot be loaded by the specified {@link ClassLoader}
     */
    Class<T> asClass(ClassLoader loader) throws ClassNotFoundException;

    /**
     * Returns all interfaces that the given class implements or — in the case
     * of interfaces — extends, taking the entire inheritance hierarchy into account.
     *
     * @return all interfaces of the class; cannot be {@code null}.
     * @see ru.joke.classpath.ClassPathResource.ClassReference
     */
    Set<ClassReference<?>> interfaces();

    /**
     * Returns the superclass hierarchy as a list, with elements ordered according
     * to the inheritance chain. The {@link java.lang.Object} type is always excluded
     * from this list.
     *
     * @return hierarchy of super classes; cannot be {@code null}.
     * @see ru.joke.classpath.ClassPathResource.ClassReference
     */
    List<ClassReference<?>> superClasses();

    /**
     * Returns the kind of the class.
     *
     * @return the kind; cannot be {@code null}.
     */
    Kind kind();

    /**
     * @return id in the format {@code <module name>/<fully qualified binary name>}.
     */
    @Override
    default String id() {
        final var modulePart = module();
        final var packagePart = packageName();
        final var fullNamePart = packagePart == null || packagePart.isEmpty() ? name() : packagePart + CANONICAL_NAME_SEPARATOR + name();
        return modulePart == null || modulePart.isEmpty()
                ? fullNamePart
                : modulePart + MODULE_SEPARATOR + fullNamePart;
    }

    @Override
    default Type type() {
        return Type.CLASS;
    }

    /**
     * An enumeration of supported class kinds from the Java classpath.
     *
     * @author Alik
     * @see AliasedEnum
     */
    enum Kind implements AliasedEnum {

        /**
         * Standard class.
         */
        CLASS("c"),
        /**
         * Interface class.
         */
        INTERFACE("i"),
        /**
         * Enum class.
         */
        ENUM("e"),
        /**
         * Record class.
         */
        RECORD("r"),
        /**
         * Annotation class.
         */
        ANNOTATION("a");

        private final String alias;

        Kind(String alias) {
            this.alias = alias;
        }

        @Override
        public String alias() {
            return this.alias;
        }

        /**
         * A static factory method that allows retrieving an enum element by its alias.
         *
         * @param alias alias of the enum element; can be {@code null}.
         * @return enum element with specified alias; can be {@code null} if alias is unknown.
         */
        public static Kind from(final String alias) {
            return AliasedEnum.from(alias, values());
        }
    }
}
