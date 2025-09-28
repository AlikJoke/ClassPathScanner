package ru.joke.classpath.indexer;

import ru.joke.classpath.ClassPathResourcesService;
import ru.joke.classpath.IndexedClassPathLocation;
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
        if (roundEnv.getRootElements().isEmpty()) {
            return false;
        }

        final var processingFilter = createProcessingFilter();
        final var outputConfigDirectory = findOrCreateOutputConfigDirectory();
        final var context = ClassPathIndexingContext.create(
                outputConfigDirectory,
                this.processingEnv,
                roundEnv,
                processingFilter
        );

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

    private FileObject createIndexedClassPathConfigFile() {
        try {
            return this.processingEnv.getFiler().createResource(
                    StandardLocation.CLASS_OUTPUT,
                    "",
                    IndexedClassPathLocation.INDEXED_RESOURCES_FILE
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File findOrCreateOutputConfigDirectory() {
        final var outputRootDir = this.processingEnv.getOptions().getOrDefault(
                ROOT_OUTPUT_DIR_PROPERTY,
                System.getProperty(TMP_DIR_PROPERTY)
        );

        final var targetOutputConfigDir = new File(outputRootDir, CONFIG_OUTPUT_DIR);
        if (!targetOutputConfigDir.mkdir() && !targetOutputConfigDir.exists()) {
            throw new RuntimeException("Couldn't create output config dir: " + targetOutputConfigDir.getAbsolutePath());
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
