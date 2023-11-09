package de.fdpg.sq2pathling.model.fhirpath;

import static java.util.Objects.requireNonNull;

import de.fdpg.sq2pathling.PrintContext;

/**
 * @author Lorenz
 */
public record InvocationExpression(Expression expression, Invocation invocation) implements BooleanExpression, Invocation {

    public InvocationExpression {
        requireNonNull(expression);
        requireNonNull(invocation);
    }

    public static InvocationExpression of(Expression expression, Invocation invocation) {
        return new InvocationExpression(expression, invocation);
    }

    @Override
    public String print(PrintContext printContext) {
        return "%s.%s".formatted(expression.print(printContext), invocation.print(printContext));
    }
}
