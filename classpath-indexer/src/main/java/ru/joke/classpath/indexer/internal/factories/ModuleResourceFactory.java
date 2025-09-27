package ru.joke.classpath.indexer.internal.factories;

import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ModuleElement;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

final class ModuleResourceFactory extends ClassPathResourceFactory<ClassPathResource.ModuleResource, ModuleElement> {

    ModuleResourceFactory(final ClassPathIndexingContext indexingContext) {
        super(indexingContext);
    }

    @Override
    public ClassPathResource.ModuleResource doCreate(ModuleElement source) {
        return new ClassPathResource.ModuleResource() {
            @Override
            public Optional<Module> asModule() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String name() {
                return source.getQualifiedName().toString();
            }

            @Override
            public Set<String> aliases() {
                return findAliases(source, name());
            }

            @Override
            public String module() {
                return name();
            }

            @Override
            public Set<ClassReference<?>> annotations() {
                final Set<ClassReference<?>> annotations = new HashSet<>();
                collectAnnotations(source, annotations);

                return annotations;
            }

            @Override
            public String packageName() {
                return "";
            }
        };
    }

    @Override
    protected Set<ElementKind> supportedTypes() {
        return Set.of(ElementKind.MODULE);
    }
}
