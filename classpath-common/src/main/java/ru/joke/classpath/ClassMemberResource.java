package ru.joke.classpath;

import java.util.List;
import java.util.stream.Collectors;

public interface ClassMemberResource extends ClassPathResource {

    String ID_SEPARATOR = "#";

    ClassReference<?> owner();

    @Override
    default String id() {
        final var modulePart = module();
        final String memberPart = owner().binaryName() + ID_SEPARATOR + name();

        return modulePart == null || modulePart.isEmpty()
                ? memberPart
                : modulePart + MODULE_SEPARATOR + memberPart;
    }

    interface Executable extends ClassMemberResource {

        String PARAMETERS_DELIMITER = ",";
        String PARAMETERS_SIGNATURE_START_BRACKET = "(";
        String PARAMETERS_SIGNATURE_END_BRACKET = ")";

        List<ClassReference<?>> parameters();

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
