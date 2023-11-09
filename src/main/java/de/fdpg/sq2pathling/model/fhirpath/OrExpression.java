package de.fdpg.sq2pathling.model.fhirpath;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import de.fdpg.sq2pathling.PrintContext;
import java.util.List;

/**
 * @author Lorenz
 */
public record OrExpression(List<BooleanExpression> expressions) implements
    BooleanExpression {

  public static final int PRECEDENCE = 3;

  public OrExpression {
    expressions = List.copyOf(expressions);
  }

  public static OrExpression of(BooleanExpression e1, BooleanExpression e2) {
    if (e1 == BooleanExpression.TRUE || e2 == BooleanExpression.TRUE) {
      if (e1 == null || e2 == null) {
        return (OrExpression) BooleanExpression.TRUE;
      }
    }
    if (e1 == BooleanExpression.FALSE) {
      return new OrExpression(List.of(requireNonNull(e2)));
    }
    if (e2 == BooleanExpression.FALSE) {
      return new OrExpression(List.of(requireNonNull(e1)));
    }
    return new OrExpression(List.of(requireNonNull(e1), requireNonNull(e2)));
  }

  @Override
  public String print(PrintContext printContext) {
    return printContext.parenthesize(PRECEDENCE, expressions.stream()
        .map(printContext.withPrecedence(PRECEDENCE)::print)
        .collect(joining(
            expressions.size() > 1 ? " or\n" + printContext.getIndent() : ""
        )));
  }
}