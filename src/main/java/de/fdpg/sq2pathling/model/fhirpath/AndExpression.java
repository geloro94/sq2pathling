package de.fdpg.sq2pathling.model.fhirpath;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import de.fdpg.sq2pathling.PrintContext;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Lorenz
 */
public record AndExpression(List<BooleanExpression> expressions) implements BooleanExpression {

  public static final int PRECEDENCE = 4;

  public AndExpression {
    expressions = List.copyOf(expressions);
  }

  public static AndExpression of(BooleanExpression e1, BooleanExpression e2) {
    if (e1 == BooleanExpression.TRUE || e2 == BooleanExpression.TRUE) {
      if (e1 == null || e2 == null) {
        return (AndExpression) BooleanExpression.FALSE;
      }
      return new AndExpression(List.of(e1 == BooleanExpression.TRUE ? requireNonNull(e2) : requireNonNull(e1)));
    }
    if (e1 instanceof AndExpression) {
      return new AndExpression(Stream.concat(((AndExpression) e1).expressions.stream(),
          Stream.of(requireNonNull(e2))).toList());
    } else {
      return new AndExpression(List.of(requireNonNull(e1), requireNonNull(e2)));
    }
  }

  @Override
  public String print(PrintContext printContext) {
    return printContext.parenthesize(PRECEDENCE, expressions.stream()
        .map(e -> e.print(printContext.withPrecedence(PRECEDENCE)))
        .collect(joining(expressions.size() > 1 ? " and\n" + printContext.getIndent() : "")));
  }
}
