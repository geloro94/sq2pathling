package de.fdpg.sq2pathling.model.structured_query;

import static java.util.Objects.requireNonNull;

import de.fdpg.sq2pathling.model.Mapping;
import de.fdpg.sq2pathling.model.MappingContext;
import de.fdpg.sq2pathling.model.common.Comparator;
import de.fdpg.sq2pathling.model.fhirpath.BooleanExpression;
import de.fdpg.sq2pathling.model.fhirpath.ComparatorExpression;
import de.fdpg.sq2pathling.model.fhirpath.Expression;
import de.fdpg.sq2pathling.model.fhirpath.MemberInvocation;
import de.fdpg.sq2pathling.model.fhirpath.QuantityExpression;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * A {@code NumericCriterion} will select all patients that have at least one resource represented
 * by that concept and numeric value.
 * <p>
 * Examples are {@code Observation} resources representing the concept of a numeric laboratory
 * value.
 */
public final class NumericCriterion extends AbstractCriterion {

  private final Comparator comparator;
  private final BigDecimal value;
  private final String unit;

  private NumericCriterion(ContextualConcept concept, List<AttributeFilter> attributeFilters,
      TimeRestriction timeRestriction, Comparator comparator,
      BigDecimal value, String unit) {
    super(concept, attributeFilters, timeRestriction);
    this.value = value;
    this.comparator = comparator;
    this.unit = unit;
  }

  /**
   * Returns a {@code NumericCriterion}.
   *
   * @param concept          the concept the criterion represents
   * @param comparator       the comparator that should be used in the value comparison
   * @param value            the value that should be used in the value comparison
   * @param attributeFilters additional filters on particular attributes
   * @return the {@code NumericCriterion}
   */
  public static NumericCriterion of(ContextualConcept concept, Comparator comparator, BigDecimal value,
      AttributeFilter... attributeFilters) {
    return new NumericCriterion(concept, List.of(attributeFilters), null,
        requireNonNull(comparator),
        requireNonNull(value), null);
  }


  /**
   * Returns a {@code NumericCriterion}.
   *
   * @param concept    the concept the criterion represents
   * @param comparator the comparator that should be used in the value comparison
   * @param value      the value that should be used in the value comparison
   * @param unit       the unit of the value
   * @return the {@code NumericCriterion}
   */
  public static NumericCriterion of(ContextualConcept concept, Comparator comparator, BigDecimal value,
      String unit) {
    return new NumericCriterion(concept, List.of(), null, requireNonNull(comparator),
        requireNonNull(value),
        requireNonNull(unit));
  }


  /**
   * Returns a {@code NumericCriterion}.
   *
   * @param concept          the concept the criterion represents
   * @param comparator       the comparator that should be used in the value comparison
   * @param value            the value that should be used in the value comparison
   * @param attributeFilters additional filters on particular attributes
   * @return the {@code NumericCriterion}
   */
  public static NumericCriterion of(ContextualConcept concept, Comparator comparator, BigDecimal value,
      TimeRestriction timeRestriction,
      AttributeFilter... attributeFilters) {
    return new NumericCriterion(concept, List.of(attributeFilters), timeRestriction,
        requireNonNull(comparator),
        requireNonNull(value), null);
  }


  /**
   * Returns a {@code NumericCriterion}.
   *
   * @param concept          the concept the criterion represents
   * @param comparator       the comparator that should be used in the value comparison
   * @param value            the value that should be used in the value comparison
   * @param attributeFilters additional filters on particular attributes
   * @param unit             the unit of the value
   * @return the {@code NumericCriterion}
   */
  public static NumericCriterion of(ContextualConcept concept, Comparator comparator, BigDecimal value,
      String unit, TimeRestriction timeRestriction,
      AttributeFilter... attributeFilters) {
    return new NumericCriterion(concept, List.of(attributeFilters), timeRestriction,
        requireNonNull(comparator),
        requireNonNull(value), requireNonNull(unit));
  }


  public Comparator getComparator() {
    return comparator;
  }

  public BigDecimal getValue() {
    return value;
  }

  public Optional<String> getUnit() {
    return Optional.ofNullable(unit);
  }

  BooleanExpression valueExpr(MappingContext mappingContext, Mapping mapping) {
    if (mapping.key().termCode().equals(AgeUtils.AGE)) {
      return AgeUtils.translateAge(mapping.valueFhirPath(), comparator, value.intValue(), AgeUnit.valueOf(unit));
    }
    var memberInvocation = MemberInvocation.of(mapping.valueFhirPath());
    return ComparatorExpression.of(memberInvocation, comparator, quantityExpression(value, unit));
  }


  private Expression quantityExpression(BigDecimal value, String unit) {
    return unit == null ? QuantityExpression.of(value) : QuantityExpression.of(value, unit);
  }
}
