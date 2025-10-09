package ru.joke.classpath.indexer.internal;

import ru.joke.classpath.ClassPathResources;
import ru.joke.classpath.IndexedClassPathResources;
import ru.joke.classpath.indexer.internal.configs.ClassPathIndexingConfiguration;
import ru.joke.classpath.indexer.internal.configs.ClassPathIndexingConfigurationService;
import ru.joke.classpath.indexer.internal.configs.ScannedResources;
import ru.joke.classpath.indexer.internal.configs.ScannedResourcesConfigurationService;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.util.Elements;
import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class ClassPathIndexingContext {

    private final ScannedResources currentScannedResources;
    private final Set<ScannedResources> prevScannedResources;
    private final String moduleName;
    private final ClassPathIndexingConfiguration indexingConfiguration;
    private final RoundEnvironment roundEnvironment;
    private final ScannedResourcesConfigurationService scannedResourcesConfigurationService;
    private final Set<Element> elements;
    private final ClassPathResources collectedResources;
    private final Elements elementUtils;
    private final Predicate<Element> processingFilter;

    private ClassPathIndexingContext(
            final ScannedResources currentScannedResources,
            final String moduleName,
            final ClassPathIndexingConfiguration indexingConfiguration,
            final ScannedResourcesConfigurationService scannedResourcesConfigurationService,
            final RoundEnvironment roundEnvironment,
            final Set<Element> elements,
            final ClassPathResources collectedResources,
            final Elements elementUtils,
            final Predicate<Element> processingFilter
    ) {
        this.currentScannedResources = currentScannedResources;
        this.prevScannedResources = scannedResourcesConfigurationService.deserializeAllAvailable();
        this.scannedResourcesConfigurationService = scannedResourcesConfigurationService;
        this.moduleName = moduleName;
        this.indexingConfiguration = indexingConfiguration;
        this.roundEnvironment = roundEnvironment;
        this.elements = elements;
        this.collectedResources = collectedResources;
        this.elementUtils = elementUtils;
        this.processingFilter = processingFilter;
    }

    public void flushCurrentScannedResources() {
        if (!this.currentScannedResources.isEmpty()) {
            this.scannedResourcesConfigurationService.serialize(this.currentScannedResources);
        }
    }

    public ScannedResources currentScannedResources() {
        return currentScannedResources;
    }

    public Set<ScannedResources> prevScannedResources() {
        return prevScannedResources;
    }

    public String moduleName() {
        return moduleName;
    }

    public Optional<ClassPathIndexingConfiguration> indexingConfiguration() {
        return Optional.ofNullable(indexingConfiguration);
    }

    public RoundEnvironment roundEnvironment() {
        return roundEnvironment;
    }

    public Set<Element> elements() {
        return elements;
    }

    public ClassPathResources collectedResources() {
        return collectedResources;
    }

    public Elements elementUtils() {
        return elementUtils;
    }

    public Predicate<Element> processingFilter() {
        return processingFilter;
    }

    public static ClassPathIndexingContext create(
            final File targetOutputConfigDir,
            final ProcessingEnvironment processingEnvironment,
            final RoundEnvironment roundEnvironment,
            final Predicate<Element> processingFilter
    ) {
        final var indexingConfigurationService = new ClassPathIndexingConfigurationService(
                processingEnvironment.getFiler(),
                processingEnvironment.getMessager()
        );

        return create(
                targetOutputConfigDir,
                processingEnvironment,
                roundEnvironment,
                processingFilter,
                indexingConfigurationService
        );
    }

    public static ClassPathIndexingContext create(
            final File targetOutputConfigDir,
            final ProcessingEnvironment processingEnvironment,
            final RoundEnvironment roundEnvironment,
            final Predicate<Element> processingFilter,
            final ClassPathIndexingConfigurationService indexingConfigurationService
    ) {
        final var scannedResourcesConfigurationService = new ScannedResourcesConfigurationService(
                targetOutputConfigDir,
                processingEnvironment.getMessager()
        );
        final var moduleName =
                roundEnvironment.getRootElements()
                        .stream()
                        .filter(e -> e instanceof ModuleElement)
                        .map(e -> (ModuleElement) e)
                        .map(ModuleElement::getQualifiedName)
                        .map(Object::toString)
                        .filter(Objects::nonNull)
                        .findAny()
                        .orElse("");

        final var indexingConfiguration = indexingConfigurationService.find().orElse(null);

        final Set<Element> elements = new HashSet<>();
        roundEnvironment.getRootElements().forEach(e -> collectElements(e, elements));

        final Set<Element> filteredElements =
                elements
                        .stream()
                        .filter(processingFilter)
                        .collect(Collectors.toSet());

        return new ClassPathIndexingContext(
                new ScannedResources(),
                moduleName,
                indexingConfiguration,
                scannedResourcesConfigurationService,
                roundEnvironment,
                filteredElements,
                new IndexedClassPathResources(),
                processingEnvironment.getElementUtils(),
                processingFilter
        );
    }

    private static void collectElements(Element parent, Set<Element> elements) {
        parent.getEnclosedElements()
                .forEach(e -> collectElements(e, elements));
        elements.add(parent);
    }
}
