package de.fdpg.sq2pathling.model.fhirpath;

import static java.util.Objects.requireNonNull;

import de.fdpg.sq2pathling.PrintContext;

/**
 * @author Lorenz
 */
public record WhereFunction(Expression expression) implements Invocation {

  public WhereFunction {
    requireNonNull(expression);
  }

  public static WhereFunction of(Expression expression) {
    return new WhereFunction(expression);
  }

  @Override
  public String print(PrintContext printContext) {
    return "where(%s)".formatted(expression.print(printContext));
  }

}
