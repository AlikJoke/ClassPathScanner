package ru.joke.classpath;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A representation of a classpath resource that is a class member — i.e., a method, a constructor, or a field.
 *
 * @author Alik
 * @see ClassPathResource
 */
public interface ClassMemberResource extends ClassPathResource {

    /**
     * The separator used in identifiers of class-member resources (separates the declaring class name from the resource name).
     */
    String ID_SEPARATOR = "#";

    /**
     * Returns a reference to the resource's declaring class.
     *
     * @return reference to the owner class; cannot be {@code null}.
     */
    ClassReference<?> owner();

    /**
     * @return id in the format {@code <module name>/<fully qualified owner class binary name>#<name of the resource>}.
     */
    @Override
    default String id() {
        final var modulePart = module();
        final String memberPart = owner().binaryName() + ID_SEPARATOR + name();

        return modulePart == null || modulePart.isEmpty()
                ? memberPart
                : modulePart + MODULE_SEPARATOR + memberPart;
    }

    /**
     * A representation of an executable class member (a constructor or a method).
     *
     * @author Alik
     * @see ClassMemberResource
     * @see ClassPathResource
     */
    interface Executable extends ClassMemberResource {

        /**
         * The separator used between parameters of a method/constructor.
         */
        String PARAMETERS_DELIMITER = ",";
        /**
         * The opening parenthesis used to denote a method's or constructor's parameters.
         */
        String PARAMETERS_SIGNATURE_START_BRACKET = "(";
        /**
         * The closing parenthesis used to denote a method's or constructor's parameters.
         */
        String PARAMETERS_SIGNATURE_END_BRACKET = ")";

        /**
         * Returns the parameters of the executable resource (method or constructor) in the order they appear in the resource's signature.
         *
         * @return the parameters; cannot be {@code null}.
         */
        List<ClassReference<?>> parameters();

        /**
         * @return id in the format {@code <module name>/<fully qualified owner class binary name>#<name of the resource>(<param1 type binary name>,<param2 type binary name>)}.
         */
        @Override
        default String id() {
            return ClassMemberResource.super.id() + createParametersSignature();
        }

        private String createParametersSignature() {
            return parameters()
                    .stream()
                    .map(ClassPathResource.ClassReference::binaryName)
                    .collect(Collectors.joining(PARAMETERS_DELIMITER, PARAMETERS_SIGNATURE_START_BRACKET, PARAMETERS_SIGNATURE_END_BRACKET));
        }
    }
}
