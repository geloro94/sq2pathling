package de.fdpg.sq2pathling.model.structured_query;

import de.fdpg.sq2pathling.PrintContext;
import de.fdpg.sq2pathling.model.Mapping;
import de.fdpg.sq2pathling.model.MappingContext;
import de.fdpg.sq2pathling.model.common.TermCode;
import de.fdpg.sq2pathling.model.fhirpath.BooleanExpression;
import de.fdpg.sq2pathling.model.fhirpath.IdentifierExpression;
import de.fdpg.sq2pathling.model.fhirpath.InvocationExpression;
import de.fdpg.sq2pathling.model.fhirpath.MembershipExpression;
import java.util.List;

/**
 * A {@code ReferenceCriterion} will select all patients that have at least one resource represented
 * by that concept through a reference. Currently, ReferenceCriterion is not defined within the
 * structured query (its virtual)
 */
public final class ReferenceCriterion extends AbstractCriterion {

  private final TermCode referencedTermCode;

  private ReferenceCriterion(ContextualConcept concept, List<AttributeFilter> attributeFilters,
      TimeRestriction timeRestriction, TermCode referencedTermCode) {
    super(concept, attributeFilters, timeRestriction);
    this.referencedTermCode = referencedTermCode;

  }

  public static ReferenceCriterion of(AbstractCriterion criterion, TermCode referencedTermCode) {
    return new ReferenceCriterion(criterion.concept, criterion.attributeFilters,
        criterion.timeRestriction(), referencedTermCode);
  }

  @Override
  BooleanExpression valueExpr(MappingContext mappingContext, Mapping mapping) {
//    var retrieveExprContainer = identifyResourceByTermCode(mappingContext, referencedTermCode).map(
//        terminology -> RetrieveExpression.of("Medication", terminology));
//    var alias = retrieveExprContainer.getExpression().get().alias();
//    var returnClause = ReturnClause.of(
//        AdditionExpressionTerm.of(StringLiteralExpression.of("Medication/"),
//            InvocationExpression.of(alias, "id")));
//    var queryExprContainer = retrieveExprContainer.map(
//        retrieveExpr -> QueryExpression.of(SourceClause.of(retrieveExpr, retrieveExpr.alias()),
//            returnClause));
//    var valueExpr = InvocationExpression.of(identifier, mapping.valueFhirPath());
//    var membershipExprContainer = Container.<BooleanExpression>of(
//        MembershipExpression.in(valueExpr, referenceName(referencedTermCode)));
//    return membershipExprContainer.addReferenceDefinition(referenceName(referencedTermCode).print(
//            PrintContext.ZERO),
//        queryExprContainer);
//  }
    return BooleanExpression.TRUE;
  }
}
