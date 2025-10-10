package ru.joke.classpath.indexer.test_util;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public final class TestPackageElement extends TestElement<Package> implements PackageElement {

    private final ModuleElement module;

    public TestPackageElement(
            final Package pkg,
            final ModuleElement module
    ) {
        super(
                new TestNoTypeMirror(TypeKind.PACKAGE, pkg),
                pkg.isSealed() ? EnumSet.of(Modifier.SEALED) : EnumSet.noneOf(Modifier.class),
                pkg.getName(),
                pkg
        );
        this.module = module;
    }

    @Override
    public Element getEnclosingElement() {
        return this.module;
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
        return Util.findAllClasses(this.source.getName())
                    .stream()
                    .map(TestTypeElement::new)
                    .collect(Collectors.toList());
    }

    @Override
    public Name getQualifiedName() {
        return new TestName(this.source.getName());
    }

    @Override
    public boolean isUnnamed() {
        return this.source.getName().isEmpty();
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.PACKAGE;
    }
}
