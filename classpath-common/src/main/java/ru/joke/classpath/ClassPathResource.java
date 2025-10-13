package ru.joke.classpath;

import java.util.Set;

/**
 * Representation of a resource located on the Java classpath. <br>
 * It denotes resources that the library can index and subsequently scan to find
 * information about classpath resources according to various search criteria.<br><br>
 * To index resources, they must either be annotated with {@link ClassPathIndexed}
 * or marked via a configuration file.
 *
 * @author Alik
 *
 * @see PackageResource
 * @see ModuleResource
 * @see ClassConstructorResource
 * @see ClassMethodResource
 * @see ClassMemberResource
 * @see ClassFieldResource
 * @see ClassResource
 * @see ClassPathIndexed
 */
public interface ClassPathResource {

    /**
     * The separator used to separate the JPMS module name part,
     * to which the resource belongs, within the resource identifier.
     */
    String MODULE_SEPARATOR = "/";

    /**
     * Returns the identifier of this resource according to its type.
     *
     * @return the identifier of this resource; cannot be {@code null} or empty.
     */
    String id();

    /**
     * Returns the name of this resource.
     *
     * @return the name of this resource; cannot be {@code null}.
     */
    String name();

    /**
     * Returns the aliases of this resource.<br>
     * A single alias can refer to multiple different resources.
     *
     * @return the aliases; cannot be {@code null}.
     */
    Set<String> aliases();

    /**
     * Returns the JPMS module to which the resource belongs.<br>
     * If the module is not defined or is unnamed, an empty string will be returned.
     *
     * @return the name of the module; cannot be {@code null}.
     */
    String module();

    /**
     * Returns the annotations present on this resource.<br>
     * The library recursively indexes all resource annotations; i.e., if a resource is
     * annotated with {@code @A}, which in turn is annotated with {@code @B},
     * this method will return both {@code @A} and {@code @B}.
     *
     * @return the annotations; cannot be {@code null}.
     * @see ClassReference
     */
    Set<ClassReference<?>> annotations();

    /**
     * Returns the fully qualified package name to which the resource belongs.<br>
     * For modules and packages, an empty string is always returned. If a class or other
     * resource is in the empty (default) package, an empty string will be returned.
     *
     * @return the fully qualified package name; cannot be {@code null}.
     */
    String packageName();

    /**
     * Returns the type of the indexed resource.
     *
     * @return the type of the resource; cannot be {@code null}.
     * @see Type
     */
    Type type();

    /**
     * Returns the set of resource modifiers.
     *
     * @return resource modifiers; cannot be {@code null}.
     */
    Set<Modifier> modifiers();

    /**
     * Returns a brief, descriptive string representation of the resource.
     *
     * @return descriptive string representation of the resource; cannot be {@code null} or empty.
     */
    default String toStringDescription() {
        return type() + "@" + id();
    }

    /**
     * A reference to a class (where 'class' in this context refers to any resource that
     * can be represented as a {@link java.lang.Class} object).
     *
     * @param <T> type of referenced Java class.
     */
    interface ClassReference<T> {

        /**
         * The separator used for forming the binary name of a nested class.
         */
        char BINARY_NESTED_ID_SEPARATOR = '$';

        /**
         * The separator used for forming the canonical name of a class.
         */
        char CANONICAL_NAME_SEPARATOR = '.';

        /**
         * Returns the canonical name of the class that this interface refers to.
         *
         * @return the canonical name of the class; cannot be {@code null} or empty.
         */
        String canonicalName();


        /**
         * Returns the binary name of the class that this interface refers to.
         *
         * @return the binary name of the class; cannot be {@code null} or empty.
         */
        String binaryName();

        /**
         * Obtains the class descriptor as a {@link java.lang.Class} object.<br>
         * If the class is not loaded, it loads the class using the provided {@link java.lang.ClassLoader}.
         *
         * @return referenced class object; cannot be {@code null}.
         * @throws ClassNotFoundException if the class cannot be loaded by the {@link ClassLoader} of caller class
         */
        default Class<T> toClass() throws ClassNotFoundException {
            final var callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
            return toClass(callerClass.getClassLoader());
        }

        /**
         * Obtains the class descriptor as a {@link java.lang.Class} object.<br>
         * If the class is not loaded, it loads the class using the class loader that
         * was used to load the class calling this method.
         *
         * @param loader target classloader; cannot be {@code null}.
         *
         * @return referenced class object; cannot be {@code null}.
         * @throws ClassNotFoundException if the class cannot be loaded by the specified {@link ClassLoader}
         */
        Class<T> toClass(ClassLoader loader) throws ClassNotFoundException;
    }

    /**
     * An enumeration of supported resource types from the Java classpath.
     *
     * @author Alik
     * @see AliasedEnum
     */
    enum Type implements AliasedEnum {

        /**
         * JPMS Module.
         */
        MODULE("m"),
        /**
         * Java Class.
         */
        CLASS("c"),
        /**
         * Constructor of the class.
         */
        CONSTRUCTOR("cr"),
        /**
         * Method of the class (static or instance methods).
         */
        METHOD("md"),
        /**
         * Field of the class (static or instance fields) / enum element in enum type.
         */
        FIELD("f"),
        /**
         * Java package.
         */
        PACKAGE("p");

        private final String alias;

        Type(String alias) {
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
        public static Type from(final String alias) {
            return AliasedEnum.from(alias, values());
        }
    }

    /**
     * An enumeration of supported resource modifiers for indexing.
     *
     * @author Alik
     * @see AliasedEnum
     */
    enum Modifier implements AliasedEnum {

        /**
         * Modifier {@code final} (for classes, methods and fields).
         */
        FINAL("f"),
        /**
         * Modifier {@code abstract} (for classes and methods).
         */
        ABSTRACT("a"),
        /**
         * Modifier {@code transient} (for fields).
         */
        TRANSIENT("t"),
        /**
         * Modifier {@code volatile} (for fields).
         */
        VOLATILE("v"),
        /**
         * Modifier {@code synchronized} (for methods and fields).
         */
        SYNCHRONIZED("sy"),
        /**
         * Modifier {@code native} (for methods).
         */
        NATIVE("n"),
        /**
         * Modifier {@code public} (for classes, methods and fields).
         */
        PUBLIC("pc"),
        /**
         * Modifier {@code protected} (for classes, methods and fields).
         */
        PROTECTED("pd"),
        /**
         * Modifier {@code private} (for classes, methods and fields).
         */
        PRIVATE("pv"),
        /**
         * Modifier {@code default} (for methods).
         */
        DEFAULT("d"),
        /**
         * Modifier {@code sealed} (for classes and packages).
         */
        SEALED("s"),
        /**
         * Modifier {@code opened} (for modules).
         */
        OPENED("o"),
        /**
         * Modifier {@code static} (for classes, methods and fields).
         */
        STATIC("st"),
        /**
         * Modifier {@code non-sealed} (for classes).
         */
        NON_SEALED("ns");

        private final String alias;

        Modifier(String alias) {
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
        public static Modifier from(final String alias) {
            return AliasedEnum.from(alias, values());
        }
    }
}
