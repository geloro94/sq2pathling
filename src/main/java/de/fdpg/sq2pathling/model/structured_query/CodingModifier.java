package de.fdpg.sq2pathling.model.structured_query;

import static de.fdpg.sq2pathling.model.structured_query.AbstractCriterion.identifyResourceByTermCode;

import de.fdpg.sq2pathling.model.MappingContext;
import de.fdpg.sq2pathling.model.common.Comparator;
import de.fdpg.sq2pathling.model.common.TermCode;
import de.fdpg.sq2pathling.model.fhirpath.BooleanExpression;
import de.fdpg.sq2pathling.model.fhirpath.ComparatorExpression;
import de.fdpg.sq2pathling.model.fhirpath.FunctionInvocation;
import de.fdpg.sq2pathling.model.fhirpath.IdentifierExpression;
import de.fdpg.sq2pathling.model.fhirpath.InvocationExpression;
import de.fdpg.sq2pathling.model.fhirpath.MemberInvocation;
import de.fdpg.sq2pathling.model.fhirpath.MembershipExpression;
import de.fdpg.sq2pathling.model.fhirpath.OrExpression;
import de.fdpg.sq2pathling.model.fhirpath.StringLiteralExpression;
import de.fdpg.sq2pathling.model.fhirpath.WhereFunction;
import java.util.List;
import java.util.Objects;

/**
 * @author Lorenz Rosenau
 */
public final class CodingModifier extends AbstractModifier {

    private final List<TermCode> concepts;

    private CodingModifier(String path, List<TermCode> concepts) {
        super(path);
        this.concepts = concepts;
    }

    public static CodingModifier of(String path, TermCode... concepts) {
        return new CodingModifier(path, List.of(concepts));
    }

    public BooleanExpression expression(MappingContext mappingContext) {
        return concepts.stream().map(termCode -> {
            var whereSystemFunction = WhereFunction.of(ComparatorExpression.of(
                IdentifierExpression.of("system"),
                Comparator.EQUAL,
                StringLiteralExpression.of("%s".formatted(termCode.system()))));
            var existsCodeInvocation = FunctionInvocation.of("exists", List.of(
                ComparatorExpression.of(
                    IdentifierExpression.of("code"),
                    Comparator.EQUAL,
                    StringLiteralExpression.of("%s".formatted(termCode.code())))));
            var systemAndCodeExpression = InvocationExpression.of(
                whereSystemFunction,
                existsCodeInvocation);
            return (BooleanExpression) InvocationExpression.of(
                InvocationExpression.of(
                    IdentifierExpression.of(path),
                    MemberInvocation.of("coding")),
                systemAndCodeExpression);
        }).reduce(BooleanExpression.FALSE, OrExpression::of);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodingModifier that = (CodingModifier) o;
        return path.equals(that.path) && concepts.equals(that.concepts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, concepts);
    }

}
