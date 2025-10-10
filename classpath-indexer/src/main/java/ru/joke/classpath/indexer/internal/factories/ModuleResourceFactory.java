package ru.joke.classpath.indexer.internal.factories;

import ru.joke.classpath.ClassPathResource;
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
        final var name = source.getQualifiedName().toString();
        final var aliases = findAliases(source, name);

        final var modifiers = collectModifiers(source);

        final Set<ClassPathResource.ClassReference<?>> annotations = new HashSet<>();
        collectAnnotations(source, annotations);

        return new ModuleResource() {
            @Override
            public Optional<Module> asModule(ModuleLayer layer) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public Set<String> aliases() {
                return aliases;
            }

            @Override
            public Set<ClassReference<?>> annotations() {
                return annotations;
            }

            @Override
            public Set<Modifier> modifiers() {
                return modifiers;
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

    private Set<ClassPathResource.Modifier> collectModifiers(final ModuleElement source) {
        final Set<ClassPathResource.Modifier> modifiers = new HashSet<>(mapModifiers(source.getModifiers()));
        if (source.isOpen()) {
            modifiers.add(ClassPathResource.Modifier.OPENED);
        }

        return modifiers.isEmpty()
                ? EnumSet.noneOf(ClassPathResource.Modifier.class)
                : EnumSet.copyOf(modifiers);
    }
}
