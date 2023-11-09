package de.fdpg.sq2pathling.model.structured_query;

import de.fdpg.sq2pathling.model.MappingContext;
import de.fdpg.sq2pathling.model.fhirpath.BetweenExpression;
import de.fdpg.sq2pathling.model.fhirpath.BooleanExpression;
import de.fdpg.sq2pathling.model.fhirpath.Expression;
import de.fdpg.sq2pathling.model.fhirpath.IdentifierExpression;
import de.fdpg.sq2pathling.model.fhirpath.InvocationExpression;
import de.fdpg.sq2pathling.model.fhirpath.MemberInvocation;
import de.fdpg.sq2pathling.model.fhirpath.QuantityExpression;
import java.math.BigDecimal;

public class RangeModifier extends AbstractModifier {

  private final BigDecimal lowerBound;
  private final BigDecimal upperBound;
  private final String unit;

  public RangeModifier(String path, BigDecimal lowerBound, BigDecimal upperBound, String unit) {
    super(path);
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
    this.unit = unit;
  }

  public static RangeModifier of(String path, BigDecimal lowerBound, BigDecimal upperBound,
      String unit) {
    return new RangeModifier(path, lowerBound, upperBound, unit);
  }

  @Override
  public BooleanExpression expression(MappingContext mappingContext) {
    var memberInvocation = MemberInvocation.of(path);
    return BetweenExpression.of(memberInvocation, quantityExpression(lowerBound, unit),
        quantityExpression(upperBound, unit));
  }

  private QuantityExpression quantityExpression(BigDecimal value, String unit) {
    return unit == null ? QuantityExpression.of(value) : QuantityExpression.of(value, unit);
  }
}
