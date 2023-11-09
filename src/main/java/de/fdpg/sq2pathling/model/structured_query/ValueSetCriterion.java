package de.fdpg.sq2pathling.model.structured_query;

import static de.fdpg.sq2pathling.model.common.Comparator.EQUAL;

import de.fdpg.sq2pathling.model.Mapping;
import de.fdpg.sq2pathling.model.MappingContext;
import de.fdpg.sq2pathling.model.common.Comparator;
import de.fdpg.sq2pathling.model.common.TermCode;
import de.fdpg.sq2pathling.model.fhirpath.BooleanExpression;
import de.fdpg.sq2pathling.model.fhirpath.ComparatorExpression;
import de.fdpg.sq2pathling.model.fhirpath.Expression;
import de.fdpg.sq2pathling.model.fhirpath.FunctionInvocation;
import de.fdpg.sq2pathling.model.fhirpath.IdentifierExpression;
import de.fdpg.sq2pathling.model.fhirpath.InvocationExpression;
import de.fdpg.sq2pathling.model.fhirpath.MemberInvocation;
import de.fdpg.sq2pathling.model.fhirpath.MembershipExpression;
import de.fdpg.sq2pathling.model.fhirpath.OrExpression;
import de.fdpg.sq2pathling.model.fhirpath.StringLiteralExpression;
import de.fdpg.sq2pathling.model.fhirpath.WhereFunction;
import java.util.List;

/**
 * A {@code ValueSetCriterion} will select all patients that have at least one resource represented
 * by that concept and coded value.
 * <p>
 * Examples are {@code Observation} resources representing the concept of a coded laboratory value.
 */
public final class ValueSetCriterion extends AbstractCriterion {

  private final List<TermCode> selectedConcepts;

  private ValueSetCriterion(ContextualConcept concept, List<AttributeFilter> attributeFilters,
      TimeRestriction timeRestriction, List<TermCode> selectedConcepts) {
    super(concept, attributeFilters, timeRestriction);
    this.selectedConcepts = selectedConcepts;
  }

  /**
   * Returns a {@code ValueSetCriterion}.
   *
   * @param concept          the concept the criterion represents
   * @param selectedConcepts at least one selected value concept
   * @return the {@code ValueSetCriterion}
   */
  public static ValueSetCriterion of(ContextualConcept concept, TermCode... selectedConcepts) {
    if (selectedConcepts == null || selectedConcepts.length == 0) {
      throw new IllegalArgumentException("empty selected concepts");
    }
    return new ValueSetCriterion(concept, List.of(), null, List.of(selectedConcepts));
  }

  /**
   * Returns a {@code ValueSetCriterion}.
   *
   * @param concept          the concept the criterion represents
   * @param selectedConcepts at least one selected value concept
   * @param attributeFilters additional filters on particular attributes
   * @return the {@code ValueSetCriterion}
   */
  public static ValueSetCriterion of(ContextualConcept concept, List<TermCode> selectedConcepts,
      AttributeFilter... attributeFilters) {
    if (selectedConcepts == null || selectedConcepts.isEmpty()) {
      throw new IllegalArgumentException("empty selected concepts");
    }
    return new ValueSetCriterion(concept, List.of(attributeFilters), null,
        List.copyOf(selectedConcepts));
  }

  /**
   * Returns a {@code ValueSetCriterion}.
   *
   * @param concept          the concept the criterion represents
   * @param timeRestriction  the timeRestriction applied to the concept
   * @param selectedConcepts at least one selected value concept
   * @param attributeFilters additional filters on particular attributes
   * @return the {@code ValueSetCriterion}
   */
  public static ValueSetCriterion of(ContextualConcept concept, List<TermCode> selectedConcepts,
      TimeRestriction timeRestriction,
      AttributeFilter... attributeFilters) {
    if (selectedConcepts == null || selectedConcepts.isEmpty()) {
      throw new IllegalArgumentException("empty selected concepts");
    }
    return new ValueSetCriterion(concept, List.of(attributeFilters), timeRestriction,
        List.copyOf(selectedConcepts));
  }

  public List<TermCode> getSelectedConcepts() {
    return selectedConcepts;
  }

  BooleanExpression valueExpr(MappingContext mappingContext, Mapping mapping) {
    var identifierExpression = IdentifierExpression.of(mapping.valueFhirPath());
    return valuePathExpr(identifierExpression, mapping);
  }

  private BooleanExpression valuePathExpr(IdentifierExpression identifierExpression, Mapping mapping) {
    if ("Coding".equals(mapping.valueType())) {
      var result = selectedConcepts.stream().map(termCode -> {
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
                identifierExpression,
                MemberInvocation.of("coding")),
            systemAndCodeExpression);
      }).reduce(BooleanExpression.FALSE, OrExpression::of);
      return result;
    }
    var codes = selectedConcepts.stream().map(TermCode::code).toList();
    if (codes.size() == 1) {
      return ComparatorExpression.of(
          identifierExpression, EQUAL,
          StringLiteralExpression.of("%s".formatted(codes.get(0))));
    } else {
      return codes.stream().map(c ->
              (BooleanExpression) ComparatorExpression.of(
                  identifierExpression, EQUAL, StringLiteralExpression.of("%s".formatted(c))))
          .reduce(BooleanExpression.FALSE,
              OrExpression::of);
    }

  }
}
