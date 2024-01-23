package de.fdpg.sq2pathling.model.structured_query;

import static java.util.Objects.requireNonNull;

import de.fdpg.sq2pathling.Lists;
import de.fdpg.sq2pathling.PrintContext;
import de.fdpg.sq2pathling.model.AttributeMapping;
import de.fdpg.sq2pathling.model.Mapping;
import de.fdpg.sq2pathling.model.MappingContext;
import de.fdpg.sq2pathling.model.common.Comparator;
import de.fdpg.sq2pathling.model.common.TermCode;
import de.fdpg.sq2pathling.model.fhirpath.AndExpression;
import de.fdpg.sq2pathling.model.fhirpath.BooleanExpression;
import de.fdpg.sq2pathling.model.fhirpath.ComparatorExpression;
import de.fdpg.sq2pathling.model.fhirpath.FunctionInvocation;
import de.fdpg.sq2pathling.model.fhirpath.IdentifierExpression;
import de.fdpg.sq2pathling.model.fhirpath.InvocationExpression;
import de.fdpg.sq2pathling.model.fhirpath.MemberInvocation;
import de.fdpg.sq2pathling.model.fhirpath.OrExpression;
import de.fdpg.sq2pathling.model.fhirpath.ReverseResolveFunctionExpression;
import de.fdpg.sq2pathling.model.fhirpath.StringLiteralExpression;
import de.fdpg.sq2pathling.model.fhirpath.WhereFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Abstract criterion holding the concept, every non-static criterion has.
 */
public abstract class AbstractCriterion implements Criterion {

  final ContextualConcept concept;
  public List<AttributeFilter> attributeFilters;
  final TimeRestriction timeRestriction;

  AbstractCriterion(ContextualConcept concept, List<AttributeFilter> attributeFilters,
      TimeRestriction timeRestriction) {
    this.concept = requireNonNull(concept);
    this.attributeFilters = new ArrayList<>(attributeFilters);
    this.timeRestriction = timeRestriction;
  }

  /**
   * Returns the code selector expression according to the given term code.
   *
   * @param mappingContext the mapping context to determine the code system definition of the
   *                       concept
   * @param termCode       the term code to use
   * @return a BooleanExpression that checks the existence of the given term code
   */
  static BooleanExpression identifyResourceByTermCode(MappingContext mappingContext,
      ContextualTermCode termCode) {
    var mapping = mappingContext.findMapping(termCode);
    if (mapping.isEmpty()) {
      throw new MappingNotFoundException(termCode);
    }
    var term_code_path = mapping.get().termCodePath();
    if (term_code_path == null) {
      return BooleanExpression.TRUE;
    }
    var whereSystemFunction = WhereFunction.of(ComparatorExpression.of(
        IdentifierExpression.of("system"),
        Comparator.EQUAL,
        StringLiteralExpression.of("%s".formatted(termCode.termCode().system()))));
    var existsCodeInvocation = FunctionInvocation.of("exists", List.of(
        ComparatorExpression.of(
            IdentifierExpression.of("code"),
            Comparator.EQUAL,
            StringLiteralExpression.of("%s".formatted(termCode.termCode().code())))));
    var systemAndCodeExpression = InvocationExpression.of(
        whereSystemFunction,
        existsCodeInvocation);
    return InvocationExpression.of(
        IdentifierExpression.of(term_code_path),
        systemAndCodeExpression);
  }


  static BooleanExpression modifiersExpr(List<Modifier> modifiers,
      MappingContext mappingContext) {
    return modifiers.stream()
        .map(m -> m.expression(mappingContext))
        .reduce(BooleanExpression.TRUE, AndExpression::of);
  }

  @Override
  public ContextualConcept getConcept() {
    return concept;
  }

  @Override
  public BooleanExpression toFhirPathFilter(MappingContext mappingContext) {
    return fullExpr(mappingContext);
  }

  /**
   * Builds an OR-expression with an expression for each concept of the expansion of {@code
   * termCode}.
   */
  private BooleanExpression fullExpr(MappingContext mappingContext) {
    return mappingContext.expandConcept(concept)
        .map(termCode -> expr(mappingContext, termCode))
        .reduce(BooleanExpression.FALSE, OrExpression::of);
  }

  private BooleanExpression expr(MappingContext mappingContext, ContextualTermCode termCode) {
    var mapping = mappingContext.findMapping(termCode)
        .orElseThrow(() -> new MappingNotFoundException(termCode));
    if ("Patient".equals(mapping.resourceType())) {
      return valueAndModifierExpr(mappingContext, mapping);
    } else {
      var resourceType = mapping.resourceType();
      var reverseResolveFunction = ReverseResolveFunctionExpression.of(
          InvocationExpression.of(IdentifierExpression.of(resourceType),
              MemberInvocation.of("subject")));
      var identifier = identifyResourceByTermCode(mappingContext, termCode);
      var value_and_modifiers = valueAndModifierExpr(mappingContext, mapping);
      var existsInvocation = FunctionInvocation.of("exists", List.of(AndExpression.of(
          identifier,
          value_and_modifiers)));
      return InvocationExpression.of(reverseResolveFunction, existsInvocation);


    }
  }

  protected BooleanExpression valueAndModifierExpr(MappingContext mappingContext,
      Mapping mapping) {
    var valueExpr = valueExpr(mappingContext, mapping);
    var modifiers = Lists.concat(mapping.fixedCriteria(),
        resolveAttributeModifiers(mapping.attributeMappings()));
    if (Objects.nonNull(timeRestriction)) {
      modifiers = Lists.concat(modifiers, List.of(timeRestriction.toModifier(mapping)));
    }
    if (modifiers.isEmpty()) {
      // TODO: is this correct position to handle this?
      return valueExpr == null ? BooleanExpression.TRUE : valueExpr;
    } else {
      return AndExpression.of(valueExpr, modifiersExpr(modifiers, mappingContext));
    }
  }

  abstract BooleanExpression valueExpr(MappingContext mappingContext, Mapping mapping);

  private List<Modifier> resolveAttributeModifiers(
      Map<TermCode, AttributeMapping> attributeMappings) {
    return attributeFilters.stream().map(attributeFilter -> {
      var key = attributeFilter.attributeCode();
      var mapping = Optional.ofNullable(attributeMappings.get(key)).orElseThrow(() ->
          new AttributeMappingNotFoundException(key));
      return attributeFilter.toModifier(mapping);
    }).toList();
  }

  @Override
  public TimeRestriction timeRestriction() {
    return timeRestriction;
  }

  public void appendAttributeFilter(AttributeFilter attributeFilter) {
    attributeFilters.add(attributeFilter);
  }
}
