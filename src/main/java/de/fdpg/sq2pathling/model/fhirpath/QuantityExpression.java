package de.fdpg.sq2pathling.model.fhirpath;

import static java.util.Objects.requireNonNull;

import de.fdpg.sq2pathling.PrintContext;
import java.math.BigDecimal;

public record QuantityExpression(BigDecimal value, String unit) implements Expression {

    public QuantityExpression {
        requireNonNull(value);
    }

    public static QuantityExpression of(BigDecimal value) {
        return new QuantityExpression(value, null);
    }

    public static QuantityExpression of(BigDecimal value, String unit) {
        return new QuantityExpression(value, requireNonNull(unit));
    }

    @Override
    public String print(PrintContext printContext) {
        return unit == null ? value.toString() : "%s '%s'".formatted(value, unit);
    }
}
