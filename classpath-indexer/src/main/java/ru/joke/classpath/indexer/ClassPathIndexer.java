package ru.joke.classpath.indexer;

import ru.joke.classpath.IndexedClassPathException;
import ru.joke.classpath.services.ClassPathResourcesService;
import ru.joke.classpath.services.IndexedClassPathLocation;
import ru.joke.classpath.indexer.internal.ClassPathIndexingContext;
import ru.joke.classpath.indexer.internal.ScanningResourcesFilter;
import ru.joke.classpath.indexer.internal.collectors.ClassPathResourcesCollector;
import ru.joke.classpath.indexer.internal.factories.DelegatingResourceFactory;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.function.Predicate;

/**
 * An annotation processor implementation that indexes classpath resources which are either
 * annotated or specified in the indexing configuration.<br>
 * As essential operational parameters, it requires the specification of the following information:
 * <ul>
 *     <li><i>rootProjectOutputDir</i>: The path to the root project's build output directory (e.g., target, out, build, or another).
 *     For a multi-module project, this must point to the root (parent) project. If not specified, the path to the
 *     temporary directory from {@literal java.io.tmpdir} property will be used.</li>
 *     <li><i>includedToScanElements</i>: Specifies a set of resource/element masks that are to be indexed. This
 *     parameter allows only resources matching these masks to be included in the index. The masks must be valid
 *     Java regular expressions, separated by a semicolon ({@literal ;}). If this parameter is absent, all resources
 *     found via indexing settings or those annotated with {@link ru.joke.classpath.ClassPathIndexed} will be indexed.</li>
 *     <li><i>excludedFromScanElements</i>: Allows explicitly excluding resources from indexing. The specification
 *     format is the same as for the <i>includedToScanElements</i> parameter.</li>
 * </ul>
 * This class is thread-safe.
 *
 * @author Alik
 * @see AbstractProcessor
 * @see ru.joke.classpath.ClassPathIndexed
 */
@SupportedAnnotationTypes(ClassPathIndexer.ANY_ANNOTATIONS)
@SupportedOptions({
        ClassPathIndexer.ROOT_OUTPUT_DIR_PROPERTY,
        ClassPathIndexer.EXCLUDED_SCAN_ELEMENTS_PROPERTY,
        ClassPathIndexer.INCLUDED_SCAN_ELEMENTS_PROPERTY
})
public class ClassPathIndexer extends AbstractProcessor {

    static final String ANY_ANNOTATIONS = "*";
    static final String ROOT_OUTPUT_DIR_PROPERTY = "rootProjectOutputDir";
    static final String EXCLUDED_SCAN_ELEMENTS_PROPERTY = "excludedFromScanElements";
    static final String INCLUDED_SCAN_ELEMENTS_PROPERTY = "includedToScanElements";

    private static final String TMP_DIR_PROPERTY = "java.io.tmpdir";
    private static final String CONFIG_OUTPUT_DIR = "classpath-indexing";

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        final var noCompiledElements = roundEnv.getRootElements().isEmpty();
        if (noCompiledElements && roundEnv.processingOver()) {
            return false;
        }

        final var context = buildContext(roundEnv);
        final var noScannedElementConfigs = annotations.isEmpty() && context.currentScannedResources().isEmpty() && context.prevScannedResources().isEmpty();
        if (noScannedElementConfigs || noCompiledElements) {
            context.flushCurrentScannedResources();
            return false;
        }

        final var classPathResourceFactory = new DelegatingResourceFactory(context);
        final var collector = new ClassPathResourcesCollector(context, classPathResourceFactory);
        collector.collect();

        context.flushCurrentScannedResources();
        ClassPathResourcesService.getInstance().write(
                () -> createIndexedClassPathConfigFile().getName(),
                context.collectedResources()
        );

        return false;
    }

    private ClassPathIndexingContext buildContext(final RoundEnvironment roundEnv) {
        final var processingFilter = createProcessingFilter();
        final var outputConfigDirectory = findOrCreateOutputConfigDirectory();
        final var context = ClassPathIndexingContext.create(
                outputConfigDirectory,
                this.processingEnv,
                roundEnv,
                processingFilter
        );

        context.indexingConfiguration().ifPresent(context.currentScannedResources()::fillFrom);

        return context;
    }

    private FileObject createIndexedClassPathConfigFile() {
        try {
            return this.processingEnv.getFiler().createResource(
                    StandardLocation.CLASS_OUTPUT,
                    "",
                    IndexedClassPathLocation.INDEXED_RESOURCES_FILE
            );
        } catch (IOException e) {
            throw new IndexedClassPathException(e);
        }
    }

    private File findOrCreateOutputConfigDirectory() {
        final var outputRootDir = this.processingEnv.getOptions().getOrDefault(
                ROOT_OUTPUT_DIR_PROPERTY,
                System.getProperty(TMP_DIR_PROPERTY)
        );

        final var targetOutputConfigDir = new File(outputRootDir, CONFIG_OUTPUT_DIR);
        if (!targetOutputConfigDir.mkdir() && !targetOutputConfigDir.exists()) {
            throw new IndexedClassPathException("Couldn't create output config dir: " + targetOutputConfigDir.getAbsolutePath());
        }

        return targetOutputConfigDir;
    }

    private Predicate<Element> createProcessingFilter() {
        final var options = this.processingEnv.getOptions();
        final var includedElements = options.get(INCLUDED_SCAN_ELEMENTS_PROPERTY);
        final var excludedElements = options.get(EXCLUDED_SCAN_ELEMENTS_PROPERTY);

        return new ScanningResourcesFilter(includedElements, excludedElements);
    }
}
