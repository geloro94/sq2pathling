package de.fdpg.sq2pathling.model.structured_query;

import de.fdpg.sq2pathling.model.MappingContext;
import de.fdpg.sq2pathling.model.common.Comparator;
import de.fdpg.sq2pathling.model.fhirpath.BooleanExpression;
import de.fdpg.sq2pathling.model.fhirpath.ComparatorExpression;
import de.fdpg.sq2pathling.model.fhirpath.Expression;
import de.fdpg.sq2pathling.model.fhirpath.IdentifierExpression;
import de.fdpg.sq2pathling.model.fhirpath.InvocationExpression;
import de.fdpg.sq2pathling.model.fhirpath.MemberInvocation;
import de.fdpg.sq2pathling.model.fhirpath.QuantityExpression;
import java.math.BigDecimal;
import java.util.Objects;

public class NumericModifier extends AbstractModifier {

    private final Comparator comparator;
    private final BigDecimal value;
    private final String unit;

    private NumericModifier(String path, Comparator comparator, BigDecimal value, String unit) {
        super(path);
        this.comparator = comparator;
        this.value = value;
        this.unit = unit;
    }

    public static NumericModifier of(String path, Comparator comparator, BigDecimal value, String unit) {
        return new NumericModifier(path, comparator, value, unit);
    }

    @Override
    public BooleanExpression expression(MappingContext mappingContext) {
        var memberInvocation = MemberInvocation.of(path);
        return ComparatorExpression.of(memberInvocation, comparator, quantityExpression(value, unit));
    }

    private Expression quantityExpression(BigDecimal value, String unit) {
        return unit == null ? QuantityExpression.of(value) : QuantityExpression.of(value, unit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumericModifier that = (NumericModifier) o;
        return path.equals(that.path) && comparator.equals(that.comparator)
                && value.equals(that.value) && unit.equals(that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, comparator, value, unit);
    }
}
