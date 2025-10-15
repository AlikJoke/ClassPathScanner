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

/**
 * Indexing context of the {@link ru.joke.classpath.indexer.ClassPathIndexer}.
 *
 * @author Alik
 */
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

    /**
     * It writes the serialized resources scanned during the current annotation processing round to disk.
     * These resources will then be used to inform the search for other resources in subsequent rounds.
     *
     * @see ScannedResources
     */
    public void flushCurrentScannedResources() {
        if (!this.currentScannedResources.isEmpty()) {
            this.scannedResourcesConfigurationService.serialize(this.currentScannedResources);
        }
    }

    /**
     * Returns the resources scanned during the current annotation processing round.
     *
     * @return scanned resources; cannot be {@code null}.
     * @see ScannedResources
     */
    public ScannedResources currentScannedResources() {
        return currentScannedResources;
    }

    /**
     * Returns the scanned resources collected in previous annotation processor rounds.
     *
     * @return cannot be {@code null}.
     * @see ScannedResources
     */
    public Set<ScannedResources> prevScannedResources() {
        return prevScannedResources;
    }

    /**
     * Returns the name of the JPMS module currently being processed by the annotation processor in this round.
     *
     * @return module name; cannot be {@code null} but can be empty if the module unnamed.
     */
    public String moduleName() {
        return moduleName;
    }

    /**
     * Returns the resource indexing configuration for this module.
     *
     * @return indexing configuration; cannot be {@code null}.
     * @see ClassPathIndexingConfiguration
     */
    public Optional<ClassPathIndexingConfiguration> indexingConfiguration() {
        return Optional.ofNullable(indexingConfiguration);
    }

    /**
     * Returns the current {@link RoundEnvironment} object of annotation processor.
     *
     * @return cannot be {@code null}.
     * @see RoundEnvironment
     */
    public RoundEnvironment roundEnvironment() {
        return roundEnvironment;
    }

    /**
     * Returns all elements within the currently processed module that could potentially be indexed.
     *
     * @return cannot be {@code null}.
     */
    public Set<Element> elements() {
        return elements;
    }

    /**
     * Returns an object that aggregates all resources collected so far which are destined for the index.
     *
     * @return collected resources; cannot be {@code null}.
     * @see ClassPathResources
     */
    public ClassPathResources collectedResources() {
        return collectedResources;
    }

    /**
     * Returns the util {@link Elements} object of annotation processor.
     *
     * @return cannot be {@code null}.
     * @see Elements
     */
    public Elements elementUtils() {
        return elementUtils;
    }

    /**
     * Returns the filter that determines resource inclusion in the index.
     *
     * @return filter; cannot be {@code null}.
     */
    public Predicate<Element> processingFilter() {
        return processingFilter;
    }

    /**
     * Creates the context of the annotation processor.
     *
     * @param targetOutputConfigDir directory where serialized representations of scanned resources from
     *                              all annotation processor rounds will be stored; cannot be {@code null}.
     * @param processingEnvironment annotation processor processing environment object; cannot be {@code null}.
     * @param roundEnvironment      current round environment; cannot be {@code null}.
     * @param processingFilter      filter that determines resource inclusion in the index; cannot be {@code null}.
     * @return context of the processor; cannot be {@code null}.
     * @see ProcessingEnvironment
     * @see RoundEnvironment
     */
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
        final var scannedResourcesConfigurationService = new ScannedResourcesConfigurationService(
                targetOutputConfigDir,
                processingEnvironment.getMessager()
        );

        return create(
                processingEnvironment,
                roundEnvironment,
                processingFilter,
                indexingConfigurationService,
                scannedResourcesConfigurationService
        );
    }

    /**
     * Creates the context of the annotation processor.
     *
     * @param processingEnvironment                annotation processor processing environment object; cannot be {@code null}.
     * @param roundEnvironment                     current round environment; cannot be {@code null}.
     * @param processingFilter                     filter that determines resource inclusion in the index; cannot be {@code null}.
     * @param indexingConfigurationService         service for managing resource indexing settings; cannot be {@code null}.
     * @param scannedResourcesConfigurationService service for managing scanned resources in rounds; cannot be {@code null}.
     * @return context of the processor; cannot be {@code null}.
     * @see ProcessingEnvironment
     * @see RoundEnvironment
     * @see ClassPathIndexingConfigurationService
     * @see ScannedResourcesConfigurationService
     */
    public static ClassPathIndexingContext create(
            final ProcessingEnvironment processingEnvironment,
            final RoundEnvironment roundEnvironment,
            final Predicate<Element> processingFilter,
            final ClassPathIndexingConfigurationService indexingConfigurationService,
            final ScannedResourcesConfigurationService scannedResourcesConfigurationService
    ) {

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
