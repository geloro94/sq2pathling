package de.fdpg.sq2pathling.model.fhirpath;

import static java.util.Objects.requireNonNull;

import de.fdpg.sq2pathling.PrintContext;
import de.fdpg.sq2pathling.model.common.Comparator;

/**
 * @author Lorenz
 */
public record ComparatorExpression(Expression a, Comparator comparator, Expression b) implements BooleanExpression {

    public ComparatorExpression {
        requireNonNull(a);
        requireNonNull(comparator);
        requireNonNull(b);
    }

    public static ComparatorExpression of(Expression a, Comparator comparator, Expression b) {
        return new ComparatorExpression(a, comparator, b);
    }

    @Override
    public String print(PrintContext printContext) {
        var precedence = comparator.getPrecedence();
        var childPrintContext = printContext.withPrecedence(precedence);
        return printContext.parenthesize(precedence, "%s %s %s".formatted(a.print(childPrintContext), comparator,
                b.print(childPrintContext)));
    }
}
