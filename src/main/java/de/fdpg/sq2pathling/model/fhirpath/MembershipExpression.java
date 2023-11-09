package de.fdpg.sq2pathling.model.fhirpath;

import static java.util.Objects.requireNonNull;

import de.fdpg.sq2pathling.PrintContext;

/**
 * @author Lorenz
 */
public record MembershipExpression(Expression a, String op, Expression b) implements BooleanExpression {

    public static final int PRECEDENCE = 5;

    public MembershipExpression {
        requireNonNull(a);
        requireNonNull(b);
    }

    public static MembershipExpression contains(Expression a, Expression b) {
        return new MembershipExpression(a, "contains", b);
    }

    public static MembershipExpression in(Expression a, Expression b) {
        return new MembershipExpression(a, "in", b);
    }

    @Override
    public String print(PrintContext printContext) {
        var childPrintContext = printContext.withPrecedence(PRECEDENCE);
        return printContext.parenthesize(PRECEDENCE, "%s %s %s".formatted(a.print(childPrintContext), op,
                b.print(childPrintContext)));
    }
}
