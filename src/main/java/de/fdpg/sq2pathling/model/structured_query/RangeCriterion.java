package de.fdpg.sq2pathling.model.structured_query;

import static java.util.Objects.requireNonNull;

import de.fdpg.sq2pathling.model.Mapping;
import de.fdpg.sq2pathling.model.MappingContext;
import de.fdpg.sq2pathling.model.fhirpath.BetweenExpression;
import de.fdpg.sq2pathling.model.fhirpath.BooleanExpression;
import de.fdpg.sq2pathling.model.fhirpath.MemberInvocation;
import de.fdpg.sq2pathling.model.fhirpath.QuantityExpression;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * A {@code RangeCriterion} will select all patients that have at least one resource represented by
 * that concept and a range of numeric values.
 * <p>
 * Examples are {@code Observation} resources representing the concept of a numeric laboratory
 * value.
 */
public final class RangeCriterion extends AbstractCriterion {

  private final BigDecimal lowerBound;
  private final BigDecimal upperBound;
  private final String unit;

  private RangeCriterion(ContextualConcept concept, List<AttributeFilter> attributeFilters,
      TimeRestriction timeRestriction, BigDecimal lowerBound,
      BigDecimal upperBound, String unit) {
    super(concept, attributeFilters, timeRestriction);
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
    this.unit = unit;
  }

  public static RangeCriterion of(ContextualConcept concept, BigDecimal lowerBound,
      BigDecimal upperBound) {
    return new RangeCriterion(concept, List.of(), null, requireNonNull(lowerBound),
        requireNonNull(upperBound), null);
  }

  public static RangeCriterion of(ContextualConcept concept, BigDecimal lowerBound,
      BigDecimal upperBound,
      String unit,
      AttributeFilter... attributeFilters) {
    return new RangeCriterion(concept, List.of(attributeFilters), null, requireNonNull(lowerBound),
        requireNonNull(upperBound), requireNonNull(unit));
  }

  public static RangeCriterion of(ContextualConcept concept, BigDecimal lowerBound,
      BigDecimal upperBound,
      TimeRestriction timeRestriction,
      AttributeFilter... attributeFilters) {
    return new RangeCriterion(concept, List.of(attributeFilters), null, requireNonNull(lowerBound),
        requireNonNull(upperBound), null);
  }

  public static RangeCriterion of(ContextualConcept concept, BigDecimal lowerBound,
      BigDecimal upperBound,
      String unit, TimeRestriction timeRestriction,
      AttributeFilter... attributeFilters) {
    return new RangeCriterion(concept, List.of(attributeFilters), null, requireNonNull(lowerBound),
        requireNonNull(upperBound), requireNonNull(unit));
  }

  public BigDecimal getLowerBound() {
    return lowerBound;
  }

  public BigDecimal getUpperBound() {
    return upperBound;
  }

  public Optional<String> getUnit() {
    return Optional.ofNullable(unit);
  }

  @Override
  BooleanExpression valueExpr(MappingContext mappingContext, Mapping mapping) {
    if (mapping.key().termCode().equals(AgeUtils.AGE)) {
      return AgeUtils.translateAgeFromRange(mapping.valueFhirPath(), lowerBound.intValue(),
          upperBound.intValue(), AgeUnit.valueOf(unit));
    }
    var castExpr = MemberInvocation.of(mapping.valueFhirPath());
    return BetweenExpression.of(castExpr, quantityExpression(lowerBound, unit),
        quantityExpression(upperBound, unit));
  }


  private QuantityExpression quantityExpression(BigDecimal value, String unit) {
    return unit == null ? QuantityExpression.of(value) : QuantityExpression.of(value, unit);
  }
}
