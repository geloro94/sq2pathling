package de.fdpg.sq2pathling.model.fhirpath;

import static java.util.Objects.requireNonNull;

import de.fdpg.sq2pathling.PrintContext;
import de.fdpg.sq2pathling.model.common.Comparator;

public record BetweenExpression(Expression expression, QuantityExpression lowerBound,
                                QuantityExpression upperBound) implements BooleanExpression {

  public static final int PRECEDENCE = 10;

  public BetweenExpression {
    requireNonNull(expression);
    requireNonNull(lowerBound);
    requireNonNull(upperBound);
  }

  public static BooleanExpression of(Expression expression, QuantityExpression lowerBound,
      QuantityExpression upperBound) {
    return AndExpression.of(
        ComparatorExpression.of(expression, Comparator.GREATER_EQUAL, lowerBound),
        ComparatorExpression.of(expression, Comparator.LESS_EQUAL, upperBound));
  }

  @Override
  public String print(PrintContext printContext) {
    return printContext.parenthesize(PRECEDENCE,
        "%s between %s and %s".formatted(expression.print(printContext.withPrecedence(PRECEDENCE)),
            lowerBound.print(printContext.withPrecedence(PRECEDENCE)),
            upperBound.print(printContext.withPrecedence(PRECEDENCE))));
  }
}
