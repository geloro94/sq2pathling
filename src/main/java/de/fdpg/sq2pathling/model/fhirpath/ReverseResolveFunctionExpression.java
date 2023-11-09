package de.fdpg.sq2pathling.model.fhirpath;

import static java.util.Objects.requireNonNull;

import de.fdpg.sq2pathling.PrintContext;

/**
 * @author Lorenz
 */
public record ReverseResolveFunctionExpression(Expression expression) implements Invocation {

  public ReverseResolveFunctionExpression {
    requireNonNull(expression);
  }

  public static ReverseResolveFunctionExpression of(Expression expression) {
    return new ReverseResolveFunctionExpression(expression);
  }

  @Override
  public String print(PrintContext printContext) {
    return "reverseResolve(%s)".formatted(expression.print(printContext));
  }

}
