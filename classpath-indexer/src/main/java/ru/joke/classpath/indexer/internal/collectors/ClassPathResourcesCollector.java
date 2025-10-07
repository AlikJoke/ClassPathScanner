package ru.joke.classpath.indexer.internal.collectors;

import ru.joke.classpath.ClassPathIndexed;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;
import ru.joke.classpath.indexer.internal.configs.ScannedResources;
import ru.joke.classpath.indexer.internal.configs.ClassPathIndexingConfiguration;
import ru.joke.classpath.indexer.internal.factories.ClassPathResourceFactory;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ClassPathResourcesCollector {

    private final ClassPathIndexingContext indexingContext;
    private final ClassPathResourceFactory<ClassPathResource, Element> resourceFactory;

    public ClassPathResourcesCollector(
            final ClassPathIndexingContext indexingContext,
            final ClassPathResourceFactory<ClassPathResource, Element> resourceFactory
    ) {
        this.indexingContext = indexingContext;
        this.resourceFactory = resourceFactory;
    }

    public void collect() {

        final var scannedAnnotations = collectScannedAnnotations();
        collectAnnotatedResources(scannedAnnotations);

        final var scannedInterfaces = collectScannedInterfaces();
        collectImplementations(scannedInterfaces);

        final var scannedClasses = collectScannedClasses();
        collectSubclasses(scannedClasses);

        removeDuplicatedScannedResources();
    }

    private void removeDuplicatedScannedResources() {
        final var currentScannedResources = this.indexingContext.currentScannedResources();

        final var annotations = collectPrevScannedResources(ScannedResources::annotations);
        currentScannedResources.annotations().removeAll(annotations);
        currentScannedResources.annotations().remove(ClassPathIndexed.class.getCanonicalName());

        final var interfaces = collectPrevScannedResources(ScannedResources::interfaces);
        currentScannedResources.interfaces().removeAll(interfaces);

        final var classes = collectPrevScannedResources(ScannedResources::classes);
        currentScannedResources.classes().removeAll(classes);
    }

    private Set<String> collectPrevScannedResources(final Function<ScannedResources, Set<String>> func) {
        return this.indexingContext.prevScannedResources()
                                    .stream()
                                    .map(func)
                                    .flatMap(Set::stream)
                                    .collect(Collectors.toSet());
    }

    private Set<TypeElement> collectScannedAnnotations() {
        final Set<String> result = new HashSet<>();
        result.add(ClassPathIndexed.class.getCanonicalName());
        this.indexingContext.indexingConfiguration()
                            .map(ClassPathIndexingConfiguration::annotations)
                            .ifPresent(result::addAll);

        this.indexingContext.prevScannedResources().forEach(sr -> result.addAll(sr.annotations()));

        return result
                    .stream()
                    .map(this.indexingContext.elementUtils()::getTypeElement)
                    .filter(Objects::nonNull)
                    .filter(e -> e.getKind() == ElementKind.ANNOTATION_TYPE)
                    .collect(Collectors.toSet());
    }

    private Set<String> collectScannedInterfaces() {
        final Set<String> result = new HashSet<>();

        this.indexingContext.indexingConfiguration()
                            .map(ClassPathIndexingConfiguration::interfaces)
                            .ifPresent(result::addAll);

        this.indexingContext.prevScannedResources().forEach(sr -> result.addAll(sr.interfaces()));
        result.addAll(this.indexingContext.currentScannedResources().interfaces());

        return result;
    }

    private Set<String> collectScannedClasses() {
        final Set<String> result = new HashSet<>();

        this.indexingContext.indexingConfiguration()
                            .map(ClassPathIndexingConfiguration::classes)
                            .ifPresent(result::addAll);

        this.indexingContext.prevScannedResources().forEach(sr -> result.addAll(sr.classes()));
        result.addAll(this.indexingContext.currentScannedResources().classes());

        return result;
    }

    private void collectImplementations(final Set<String> interfaces) {
        final Set<String> nestedInterfaces = new HashSet<>();

        final var elements = this.indexingContext.elements();
        elements
                .stream()
                .filter(e -> e instanceof TypeElement)
                .filter(e -> e.getKind() == ElementKind.INTERFACE || e.getKind().isClass())
                .map(e -> (TypeElement) e)
                .filter(e -> toQualifiedNames(e.getInterfaces()).anyMatch(interfaces::contains))
                .peek(e -> this.resourceFactory.create(e).ifPresent(this.indexingContext.collectedResources()::add))
                .peek(e -> {
                    if (e.getKind() == ElementKind.INTERFACE) {
                        nestedInterfaces.add(e.getQualifiedName().toString());
                    }
                })
                .forEach(e -> {
                    final var qualifiedName = e.getQualifiedName().toString();
                    if (e.getKind() == ElementKind.INTERFACE) {
                        this.indexingContext.currentScannedResources().interfaces().add(qualifiedName);
                    }

                    if (e.getKind() == ElementKind.CLASS && !e.getModifiers().contains(Modifier.FINAL)) {
                        this.indexingContext.currentScannedResources().classes().add(qualifiedName);
                    }
                });

        if (!nestedInterfaces.isEmpty()) {
            collectImplementations(nestedInterfaces);
        }
    }

    private void collectSubclasses(final Set<String> classes) {
        final Set<String> subClasses = new HashSet<>();

        final var elements = this.indexingContext.elements();
        elements
                .stream()
                .filter(e -> e instanceof TypeElement)
                .filter(e -> e.getKind().isClass())
                .map(e -> (TypeElement) e)
                .filter(e -> e.getSuperclass() instanceof DeclaredType s && s.asElement() instanceof QualifiedNameable q && classes.contains(q.getQualifiedName().toString()))
                .peek(e -> this.resourceFactory.create(e).ifPresent(this.indexingContext.collectedResources()::add))
                .filter(e -> e.getKind() == ElementKind.CLASS && !e.getModifiers().contains(Modifier.FINAL))
                .map(QualifiedNameable::getQualifiedName)
                .map(Object::toString)
                .peek(subClasses::add)
                .forEach(this.indexingContext.currentScannedResources().classes()::add);

        if (!subClasses.isEmpty()) {
            collectSubclasses(subClasses);
        }
    }

    private void collectAnnotatedResources(final Set<? extends TypeElement> annotations) {
        final var annotatedElements = this.indexingContext.roundEnvironment().getElementsAnnotatedWithAny(annotations.toArray(new TypeElement[0]));
        final var nestedAnnotations =
                annotatedElements
                        .stream()
                        .filter(e -> e instanceof TypeElement te && te.getKind() == ElementKind.ANNOTATION_TYPE)
                        .map(a -> (TypeElement) a)
                        .filter(this.indexingContext.processingFilter())
                        .collect(Collectors.toSet());

        annotatedElements
                .stream()
                .filter(this.indexingContext.processingFilter())
                .map(this.resourceFactory::create)
                .flatMap(Optional::stream)
                .forEach(this.indexingContext.collectedResources()::add);

        collectScannedResources(
                annotations,
                this.indexingContext.currentScannedResources().annotations(),
                e -> e.getKind() == ElementKind.ANNOTATION_TYPE
        );

        collectScannedResources(
                annotatedElements,
                this.indexingContext.currentScannedResources().classes(),
                e -> e.getKind() == ElementKind.CLASS && !e.getModifiers().contains(Modifier.FINAL)
        );

        collectScannedResources(
                annotatedElements,
                this.indexingContext.currentScannedResources().interfaces(),
                e -> e.getKind() == ElementKind.INTERFACE
        );

        if (!nestedAnnotations.isEmpty()) {
            collectAnnotatedResources(nestedAnnotations);
        }
    }

    private void collectScannedResources(
            final Set<? extends Element> foundElements,
            final Set<String> resources,
            final Predicate<Element> filter
    ) {
        foundElements
                .stream()
                .filter(filter)
                .filter(this.indexingContext.processingFilter())
                .filter(e -> e instanceof QualifiedNameable)
                .map(e -> (QualifiedNameable) e)
                .map(QualifiedNameable::getQualifiedName)
                .map(Object::toString)
                .forEach(resources::add);
    }

    private Stream<String> toQualifiedNames(final List<? extends TypeMirror> types) {
        return types
                .stream()
                .filter(t -> t instanceof DeclaredType)
                .map(t -> (DeclaredType) t)
                .map(DeclaredType::asElement)
                .filter(t -> t instanceof QualifiedNameable)
                .map(t -> (QualifiedNameable) t)
                .map(QualifiedNameable::getQualifiedName)
                .map(Object::toString);
    }
}
