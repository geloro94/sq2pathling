package de.fdpg.sq2pathling.model.structured_query;

import static java.util.Objects.requireNonNull;

import de.fdpg.sq2pathling.model.AttributeMapping;
import de.fdpg.sq2pathling.model.common.TermCode;
import java.math.BigDecimal;

public record RangeAttributeFilter(TermCode attributeCode,
                                   BigDecimal lowerBound,
                                   BigDecimal upperBound,
                                   String unit) implements AttributeFilter {

    public RangeAttributeFilter {
        requireNonNull(attributeCode);
        requireNonNull(lowerBound);
        requireNonNull(upperBound);
    }

    public static RangeAttributeFilter of(TermCode attributeCode, BigDecimal lowerBound, BigDecimal upperBound) {
        return new RangeAttributeFilter(attributeCode, lowerBound, upperBound, null);
    }

    public static RangeAttributeFilter of(TermCode attributeCode, BigDecimal lowerBound, BigDecimal upperBound, String unit) {
        return new RangeAttributeFilter(attributeCode, lowerBound, upperBound, requireNonNull(unit));
    }

    @Override
    public Modifier toModifier(AttributeMapping attributeMapping) {
        return RangeModifier.of(attributeMapping.path(), lowerBound, upperBound, unit);
    }
}
