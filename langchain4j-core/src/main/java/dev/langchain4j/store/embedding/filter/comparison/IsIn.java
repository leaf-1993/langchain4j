package dev.langchain4j.store.embedding.filter.comparison;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.store.embedding.filter.Filter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static dev.langchain4j.internal.ValidationUtils.ensureNotBlank;
import static dev.langchain4j.internal.ValidationUtils.ensureNotEmpty;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;
import static dev.langchain4j.store.embedding.filter.comparison.NumberComparator.containsAsBigDecimals;
import static dev.langchain4j.store.embedding.filter.comparison.TypeChecker.ensureTypesAreCompatible;
import static dev.langchain4j.store.embedding.filter.comparison.UUIDComparator.containsAsUUID;
import static java.util.Collections.unmodifiableSet;

public class IsIn implements Filter {

    private final String key;
    private final Collection<?> comparisonValues;

    public IsIn(String key, Collection<?> comparisonValues) {
        this.key = ensureNotBlank(key, "key");
        Set<?> copy = new HashSet<>(ensureNotEmpty(comparisonValues, "comparisonValues with key '" + key + "'"));
        comparisonValues.forEach(value -> ensureNotNull(value, "comparisonValue with key '" + key + "'"));
        this.comparisonValues = unmodifiableSet(copy);
    }

    public String key() {
        return key;
    }

    public Collection<?> comparisonValues() {
        return comparisonValues;
    }

    @Override
    public boolean test(Object object) {
        if (!(object instanceof Metadata metadata)) {
            return false;
        }

        if (!metadata.containsKey(key)) {
            return false;
        }

        Object actualValue = metadata.toMap().get(key);
        ensureTypesAreCompatible(actualValue, comparisonValues.iterator().next(), key);

        if (comparisonValues.iterator().next() instanceof Number) {
            return containsAsBigDecimals(actualValue, comparisonValues);
        }
        if (comparisonValues.iterator().next() instanceof UUID) {
            return containsAsUUID(actualValue, comparisonValues);
        }

        return comparisonValues.contains(actualValue);
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof IsIn other)) return false;

        return Objects.equals(this.key, other.key)
                && Objects.equals(this.comparisonValues, other.comparisonValues);
    }

    public int hashCode() {
        return Objects.hash(key, comparisonValues);
    }


    public String toString() {
        return "IsIn(key=" + this.key + ", comparisonValues=" + this.comparisonValues + ")";
    }
}
