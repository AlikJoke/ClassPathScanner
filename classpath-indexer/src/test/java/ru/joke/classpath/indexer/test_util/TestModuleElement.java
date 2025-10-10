package ru.joke.classpath.indexer.test_util;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class TestModuleElement extends TestElement<Module> implements ModuleElement {

    public TestModuleElement(final Module module) {
        super(
                new TestNoTypeMirror(TypeKind.MODULE, module),
                EnumSet.noneOf(Modifier.class),
                module.getName(),
                module
        );
    }


    @Override
    public Element getEnclosingElement() {
        return null;
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
        final var packages = source.getPackages();
        return packages
                .stream()
                .map(getClass().getClassLoader()::getDefinedPackage)
                .filter(Objects::nonNull)
                .map(pkg -> new TestPackageElement(pkg, this))
                .collect(Collectors.toList());
    }

    @Override
    public Name getQualifiedName() {
        return new TestName(this.source.getName());
    }

    @Override
    public boolean isOpen() {
        return this.source.getDescriptor().isOpen();
    }

    @Override
    public boolean isUnnamed() {
        return this.source.getDescriptor().isAutomatic();
    }

    @Override
    public List<? extends Directive> getDirectives() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.MODULE;
    }
}
