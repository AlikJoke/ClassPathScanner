package ru.joke.classpath.indexer.internal.factories;

import ru.joke.classpath.ModuleResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ModuleElement;
import java.util.*;

final class ModuleResourceFactory extends ClassPathResourceFactory<ModuleResource, ModuleElement> {

    ModuleResourceFactory(final ClassPathIndexingContext indexingContext) {
        super(indexingContext);
    }

    @Override
    public ModuleResource doCreate(ModuleElement source) {
        return new ModuleResource() {
            @Override
            public Optional<Module> asModule(ModuleLayer layer) {
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
            public Set<ClassReference<?>> annotations() {
                final Set<ClassReference<?>> annotations = new HashSet<>();
                collectAnnotations(source, annotations);

                return annotations;
            }

            @Override
            public Set<Modifier> modifiers() {
                final Set<Modifier> modifiers = new HashSet<>(mapModifiers(source.getModifiers()));
                if (source.isOpen()) {
                    modifiers.add(Modifier.OPENED);
                }

                return EnumSet.copyOf(modifiers);
            }

            @Override
            public int hashCode() {
                return Objects.hashCode(id());
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof ModuleResource f && Objects.equals(f.id(), id());
            }

            @Override
            public String toString() {
                return toStringDescription();
            }
        };
    }

    @Override
    protected Set<ElementKind> supportedTypes() {
        return Set.of(ElementKind.MODULE);
    }
}
