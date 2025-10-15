package ru.joke.classpath.indexer.internal;

import javax.lang.model.element.Element;
import javax.lang.model.element.QualifiedNameable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A scanned resource filter for inclusion in the index. It enables specifying inclusion
 * and exclusion rules for indexable resources.
 *
 * @author Alik
 */
public final class ScanningResourcesFilter implements Predicate<Element> {

    private static final String SEPARATOR = ";";

    private final Predicate<Element> predicate;

    /**
     * Constructs the filter by the included elements mask and excluded elements mask.
     *
     * @param includedToScanElements mask of included to index elements; can be {@code null}.
     * @param excludedFromScanElements mask of excluded from index elements; can be {@code null}.
     */
    public ScanningResourcesFilter(
            final String includedToScanElements,
            final String excludedFromScanElements
    ) {
        final Predicate<Element> predicate = appendConditions(collectElements(includedToScanElements), e -> true, false);
        this.predicate = appendConditions(collectElements(excludedFromScanElements), predicate, true);
    }

    @Override
    public boolean test(Element element) {
        return this.predicate.test(element);
    }


    private Predicate<Element> appendConditions(
            final Set<Pattern> patterns,
            final Predicate<Element> predicate,
            final boolean negate
    ) {
        if (patterns.isEmpty()) {
            return predicate;
        }

        Predicate<Element> temp = e -> false;
        for (var pattern : patterns) {
            temp = temp.or(e -> pattern.asMatchPredicate().test(findQualifiedName(e)));
        }

        return predicate.and(negate ? temp.negate() : temp);
    }

    private String findQualifiedName(final Element element) {
        if (element instanceof QualifiedNameable q) {
            return q.getQualifiedName().toString();
        }

        return findQualifiedName(element.getEnclosingElement());
    }

    private Set<Pattern> collectElements(final String elementsStr) {
        if (elementsStr == null || elementsStr.isBlank()) {
            return Collections.emptySet();
        }

        return Arrays.stream(elementsStr.split(SEPARATOR))
                        .filter(e -> !e.isBlank())
                        .map(Pattern::compile)
                        .collect(Collectors.toSet());
    }
}
