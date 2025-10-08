package ru.joke.classpath.services.internal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.joke.classpath.ClassPathResource;
import ru.joke.classpath.IndexedClassPathResources;
import ru.joke.classpath.ModuleResource;
import ru.joke.classpath.PackageResource;
import ru.joke.classpath.services.IndexedClassPathLocation;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultClassPathResourcesServiceTest {

    private static final String META_INF_DIR = "META-INF";

    private DefaultClassPathResourcesService service;
    private File indexFile;

    @BeforeEach
    void setUp() throws URISyntaxException {
        this.service = new DefaultClassPathResourcesService();
        final var metaInfDirResource = getClass().getResource("/" + META_INF_DIR);
        final var metaInfDirUri = Objects.requireNonNull(metaInfDirResource).toURI();
        final var dirPath = Path.of(metaInfDirUri);
        this.indexFile = new File(dirPath.toFile(), UUID.randomUUID().toString());
    }

    @AfterEach
    void tearDown() {
        this.indexFile.delete();
    }

    @Test
    void testWriteWhenNoResources() {
        final IndexedClassPathLocation location = () -> this.indexFile.getAbsolutePath();
        this.service.write(location, new IndexedClassPathResources());

        assertEquals(0, this.indexFile.length(), "File must be empty");
    }

    @Test
    void testConversion() {
        final var resources = new IndexedClassPathResources();

        final var moduleResource = createModuleResource();
        resources.add(moduleResource);

        final var packageResource = createPackageResource();
        resources.add(packageResource);

        this.service.write(() -> this.indexFile.getAbsolutePath(), resources);

        assertTrue(this.indexFile.length() > 0, "File must be not empty");

        final IndexedClassPathLocation relativeLocation = () -> META_INF_DIR + "/" + this.indexFile.getName();
        final var resourcesFromFile = this.service.read(relativeLocation, r -> true);
        assertEquals(2, resourcesFromFile.size(), "Resources count must be equal");

        assertTrue(resourcesFromFile.contains(moduleResource), "Resources must contain module");
        assertTrue(resourcesFromFile.contains(packageResource), "Resources must contain package");

        final var filteredResourcesFromFile = this.service.read(relativeLocation, r -> r.type() == ClassPathResource.Type.PACKAGE);
        assertEquals(1, filteredResourcesFromFile.size(), "Filtered resources count must be equal");
        assertTrue(filteredResourcesFromFile.contains(packageResource), "Filtered resources must contain package");
    }

    private PackageResource createPackageResource() {
        return new PackageResource() {

            @Override
            public Optional<Package> asPackage(ClassLoader loader) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String name() {
                return getClass().getPackageName();
            }

            @Override
            public Set<String> aliases() {
                return Collections.emptySet();
            }

            @Override
            public String module() {
                return "";
            }

            @Override
            public Set<ClassReference<?>> annotations() {
                return Collections.emptySet();
            }

            @Override
            public Set<Modifier> modifiers() {
                return EnumSet.noneOf(Modifier.class);
            }

            @Override
            public int hashCode() {
                return Objects.hashCode(id());
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof PackageResource f && Objects.equals(f.id(), id());
            }
        };
    }

    private ModuleResource createModuleResource() {
        return new ModuleResource() {

            @Override
            public String name() {
                return "test.module";
            }

            @Override
            public Set<String> aliases() {
                return Collections.emptySet();
            }

            @Override
            public Optional<Module> asModule(ModuleLayer layer) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Set<ClassReference<?>> annotations() {
                return Collections.emptySet();
            }

            @Override
            public Set<Modifier> modifiers() {
                return EnumSet.noneOf(Modifier.class);
            }

            @Override
            public int hashCode() {
                return Objects.hashCode(id());
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof ModuleResource f && Objects.equals(f.id(), id());
            }
        };
    }
}
