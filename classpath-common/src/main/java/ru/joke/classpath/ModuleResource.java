package ru.joke.classpath;

import java.util.Optional;

/**
 * Representation of a JPMS module resource located on the classpath.
 *
 * @author Alik
 * @see ClassPathResource
 */
public interface ModuleResource extends ClassPathResource {

    /**
     * @return id in the format {@code <module name>}; always equal to {@link #name()}.
     */
    @Override
    default String id() {
        return name();
    }

    /**
     * @return cannot be empty; always equal to {@link #name()}.
     */
    @Override
    default String module() {
        return name();
    }

    /**
     * Returns the module representation as a JDK standard {@link java.lang.Module} object.<br>
     * The module lookup is performed within the module layer to which the module calling
     * the given class method belongs.
     * If the module is not found among the modules of provided layer, {@link Optional#empty()} will be returned.
     *
     * @return wrapped module object; cannot be {@code null}.
     */
    default Optional<Module> asModule() {
        final var callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
        return asModule(callerClass.getModule().getLayer());
    }

    /**
     * Returns the module representation as a JDK standard {@link java.lang.Module} object.<br>
     * The module lookup is performed within the scope of the given layer.
     * If the module is not found among the modules of provided layer, {@link Optional#empty()} will be returned.
     *
     * @param layer layer of modules to search this module in JVM; cannot be {@code null}.
     * @return wrapped module object; cannot be {@code null}.
     */
    Optional<Module> asModule(ModuleLayer layer);

    @Override
    default Type type() {
        return Type.MODULE;
    }

    /**
     * @return always empty string.
     */
    @Override
    default String packageName() {
        return "";
    }
}
