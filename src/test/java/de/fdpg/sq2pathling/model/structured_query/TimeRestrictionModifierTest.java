package de.fdpg.sq2pathling.model.structured_query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.fdpg.sq2pathling.PrintContext;
import de.fdpg.sq2pathling.model.MappingContext;
import de.fdpg.sq2pathling.model.structured_query.TimeRestrictionModifier;
import org.junit.jupiter.api.Test;

class TimeRestrictionModifierTest {

  @Test
  void expression_before() {
    var timeRestriction = TimeRestrictionModifier.of("effective", null, "2021-01-01");
    var expression = timeRestriction.expression(MappingContext.of());

    assertEquals("""
        effective.dateTime < @2021-01-01 or
        effective.period.start < @2021-01-01""", PrintContext.ZERO.print(expression));
  }


  @Test
  void expression_after() {
    var timeRestriction = TimeRestrictionModifier.of("effective", "2020-01-01", null);
    var expression = timeRestriction.expression(MappingContext.of());

    assertEquals("""
        effective.dateTime > @2020-01-01 or
        effective.period.end > @2020-01-01""", PrintContext.ZERO.print(expression));
  }


  @Test
  void expression_beforeAfter() {
    var timeRestriction = TimeRestrictionModifier.of("effective", "2021-01-01", "2022-01-01");
    var expression = timeRestriction.expression(MappingContext.of());

    assertEquals("""
        effective.dateTime > @2021-01-01 and
        effective.dateTime < @2022-01-01 or
        effective.period.start > @2021-01-01 and
        effective.period.start < @2022-01-01 or
        effective.period.end > @2021-01-01 and
        effective.period.end < @2022-01-01""", PrintContext.ZERO.print(expression));
  }
}