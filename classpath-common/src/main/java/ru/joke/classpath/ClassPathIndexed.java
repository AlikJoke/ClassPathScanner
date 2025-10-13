package ru.joke.classpath;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * An annotation used to mark classpath resources for subsequent indexing. If a resource is
 * marked with this annotation, the following rules apply:
 * <ul>
 * <li>For interfaces: both the interface itself and all interfaces and implementations
 * inheriting from it will be included in the index.</li>
 * <li>For classes: both the class itself and all subclasses of this class will be included in the index.</li>
 * <li>For annotations: both the annotation itself and all resources annotated with it
 * will be included in the index (this also applies to annotated annotations at lower levels).</li>
 * <li>For packages, modules, constructors, and fields: only the marked elements themselves are included in the index.</li>
 * </ul>
 * This annotation also allows specifying aliases for the resource.<br>
 * An alternative method of marking resources is to specify the resources in a configuration file.
 *
 * @author Alik
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { CONSTRUCTOR, FIELD, METHOD, MODULE, TYPE, ANNOTATION_TYPE, PACKAGE })
public @interface ClassPathIndexed {

    /**
     * Aliases of the marked resource.
     *
     * @return aliases; cannot be {@code null}.
     */
    String[] value() default {};
}
