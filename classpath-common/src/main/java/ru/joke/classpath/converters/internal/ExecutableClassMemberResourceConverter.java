package ru.joke.classpath.converters.internal;

import ru.joke.classpath.ClassMemberResource;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.converters.Dictionary;

import java.util.List;
import java.util.stream.Collectors;

abstract class ExecutableClassMemberResourceConverter<T extends ClassMemberResource.Executable> extends AbsClassPathResourceConverter<T> implements ConcreteClassPathResourceConverter<T> {

    private static final int COMPONENTS_COUNT = 7;

    protected ExecutableClassMemberResourceConverter() {
        super(COMPONENTS_COUNT);
    }

    protected String createSignature(
            final String elementName,
            final String parametersStr
    ) {
        return elementName + "(" + String.join(",", parametersStr) + ")";
    }
    
    protected Class<?>[] loadParameters(
            final List<ClassPathResource.ClassReference<?>> parameters,
            final ClassLoader loader
    ) throws ClassNotFoundException {
        final var parameterTypes = new Class[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            parameterTypes[i] = parameters.get(i).toClass(loader);
        }
        
        return parameterTypes;
    }
    
    @Override
    protected String getResourceName(
            final ClassMemberResource.Executable resource,
            final Dictionary dictionary
    ) {
        final var parameters =
                resource.parameters()
                        .stream()
                        .map(ClassPathResource.ClassReference::binaryName)
                        .map(dictionary::map)
                        .collect(Collectors.joining(ELEMENTS_IN_BLOCK_DELIMITER));
        final var packageLength = resource.packageName().length();
        final var ownerClassSimpleName =
                packageLength == 0
                        ? resource.owner().binaryName()
                        : resource.owner().binaryName().substring(packageLength + 1);

        return dictionary.map(ownerClassSimpleName)
                + MEMBER_OF_CLASS_SEPARATOR
                + dictionary.map(resource.name())
                + MEMBER_OF_CLASS_SEPARATOR
                + parameters;
    }

    @Override
    protected String getResourceName(
            final String resourceNameStr,
            final Dictionary dictionary
    ) {
        return resourceNameStr;
    }
}
